/*
 * Copyright 2014-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spring.data.gemfire.cache;

import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Resource;

import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.LoaderHelper;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.execute.ResultCollector;
import org.apache.geode.cache.execute.ResultSender;
import org.apache.geode.distributed.DistributedMember;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.ReplicatedRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctions;
import org.springframework.data.gemfire.function.execution.GemfireOnRegionFunctionTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The PeerCacheFunctionExecutionResultStreamingIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class PeerCacheFunctionExecutionResultStreamingIntegrationTests {

  private static final int KEY_COUNT = 10;

  private static final long DELAY = 500; // milliseconds

  private GemfireOnRegionFunctionTemplate regionFunctionTemplate;

  @Resource(name = "RandomValues")
  private Region<Object, Object> randomValues;

  private static void log(String message, Object... args) {
    System.err.printf(message, args);
    System.err.flush();
  }

  @Before
  public void setup() {
    this.regionFunctionTemplate = new GemfireOnRegionFunctionTemplate(this.randomValues);
  }

  @Test
  public void streamingFunctionWithAggregatingBlockingResultCollector() {

    Set<Integer> keys = generateKeys();

    this.regionFunctionTemplate.setResultCollector(new AggregatingBlockingResultCollector<>());
    this.regionFunctionTemplate.executeWithNoResult("RandomValueStream", keys);

    ResultCollector<?, ?> resultCollector = this.regionFunctionTemplate.getResultCollector();

    int resultCount = 0;

    Object result;

    while ((result = resultCollector.getResult()) != null) {
      log("RESULT [%d] IS [%s]%n", ++resultCount, result);
    }

    assertThat(resultCount).isEqualTo(KEY_COUNT);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void streamingFunctionWithStreamingResultCollector() {

    Set<Integer> keys = generateKeys();

    AtomicInteger resultCount = new AtomicInteger(0);

    this.regionFunctionTemplate.setResultCollector(new StreamingResultCollector<>().with(
      (ResultHandler<Object>) result -> {
        log("%d - %s%n", resultCount.incrementAndGet(), result);
        return true;
      }));

    this.regionFunctionTemplate.executeWithNoResult("RandomValueStream", keys);

    assertThat(resultCount.get()).isEqualTo(KEY_COUNT);
  }

  private Set<Integer> generateKeys() {
    return IntStream.rangeClosed(1, KEY_COUNT).boxed().collect(Collectors.toSet());
  }

  public interface ResultHandler<T> {

    boolean handle(T result);

  }

  public static abstract class AbstractResultCollector<T, S> implements ResultCollector<T, S> {

    protected static final String NOT_IMPLEMENTED = "Not Implemented";

    private volatile boolean moreResults = true;

    protected boolean hasMoreResults() {
      return this.moreResults;
    }

    protected boolean hasNoMoreResults() {
      return !hasMoreResults();
    }

    @Override
    public void addResult(DistributedMember distributedMember, T result) {
      Optional.ofNullable(result).ifPresent(this::doAddResult);
    }

    protected abstract void doAddResult(T result);

    @Override
    public S getResult() throws FunctionException {
      throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public S getResult(long timeout, TimeUnit timeUnit) throws FunctionException, InterruptedException {
      throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void endResults() {
      this.moreResults = false;
    }

    @Override
    public void clearResults() {
    }
  }

  public static class AggregatingBlockingResultCollector<T> extends AbstractResultCollector<T, T> {

    private final BlockingQueue<T> results = newBlockingQueue();

    protected BlockingQueue<T> newBlockingQueue() {
      return new LinkedBlockingQueue<>();
    }

    @Override
    public boolean hasMoreResults() {
      return (!getResults().isEmpty() || super.hasMoreResults());
    }

    protected T handleResult(T result, Supplier<T> nullResultHandler) {
      return (result != null ? result : (super.hasMoreResults() ? nullResultHandler.get() : null));
    }

    protected BlockingQueue<T> getResults() {
      return this.results;
    }

    @Override
    protected void doAddResult(T result) {
      getResults().offer(result);
    }

    @Override
    public T getResult() throws FunctionException {

      try {
        return (hasMoreResults() ? handleResult(getResults().take(),
          () -> { throw new FunctionException("Failed to get result"); }) : null);
      }
      catch (InterruptedException cause) {
        return handleResult(null, () -> { throw new FunctionException("Failed to get result", cause); });
      }
    }

    @Override
    public T getResult(long timeout, TimeUnit timeUnit) throws FunctionException, InterruptedException {

      return (hasMoreResults() ? handleResult(getResults().poll(timeout, timeUnit),
        () -> { throw new FunctionException(String.format("Failed to get result in [%d] %s", timeout, timeUnit)); })
          : null);
    }

    @Override
    public void clearResults() {
      getResults().clear();
    }
  }

  public static class StreamingResultCollector<T> extends AbstractResultCollector<T, T> {

    private Set<ResultHandler<T>> resultHandlers = new CopyOnWriteArraySet<>();

    protected Set<ResultHandler<T>> getResultHandlers() {
      return this.resultHandlers;
    }

    @Override
    protected void doAddResult(T result) {
      getResultHandlers().forEach(resultHandler -> resultHandler.handle(result));
    }

    @SuppressWarnings("unchecked")
    public StreamingResultCollector<T> with(ResultHandler<T>... resultHandlers) {
      return with(Arrays.asList(resultHandlers));
    }

    public StreamingResultCollector<T> with(Iterable<ResultHandler<T>> resultHandlers) {
      getResultHandlers().addAll(stream(resultHandlers.spliterator(), false).collect(Collectors.toList()));
      return this;
    }
  }

  @PeerCacheApplication(name = "PeerCacheFunctionExecutionResultStreamingIntegrationTests", logLevel = "warning")
  @EnableGemfireFunctions
  public static class TestConfiguration {

    @Bean("RandomValues")
    public ReplicatedRegionFactoryBean<Object, Object> randomValuesRegion(GemFireCache gemfireCache) {

      ReplicatedRegionFactoryBean<Object, Object> randomValues = new ReplicatedRegionFactoryBean<>();

      randomValues.setCache(gemfireCache);
      randomValues.setCacheLoader(randomValueCacheLoader());
      randomValues.setClose(false);
      randomValues.setPersistent(false);

      return randomValues;
    }

    private CacheLoader<Object, Object> randomValueCacheLoader() {

      return new CacheLoader<Object, Object>() {

        private Random random = new Random(System.currentTimeMillis());

        @Override
        public Object load(LoaderHelper<Object, Object> loaderHelper) throws CacheLoaderException {
          return this.random.nextInt(KEY_COUNT);
        }

        @Override
        public void close() {
        }
      };
    }

    @SuppressWarnings("unchecked")
    @GemfireFunction(id = "RandomValueStream", hasResult = true)
    public void streamingFunction(FunctionContext functionContext) {

      RegionFunctionContext regionFunctionContext = asRegionFunctionContext(functionContext);

      Region<Object, Object> randomValues = regionFunctionContext.getDataSet();

      ResultSender<Object> resultSender = regionFunctionContext.getResultSender();

      for (Iterator<?> filterIterator = regionFunctionContext.getFilter().iterator(); filterIterator.hasNext(); ) {

        Object key = filterIterator.next();
        Object value = randomValues.get(key);

        if (filterIterator.hasNext()) {
          resultSender.sendResult(value);
        }
        else {
          resultSender.sendResult(value);
          //resultSender.lastResult(value);
        }

        delay(DELAY);
      }
    }

    private RegionFunctionContext asRegionFunctionContext(FunctionContext functionContext) {

      Assert.isInstanceOf(RegionFunctionContext.class, functionContext, () ->
        String.format("Function [RandomValueStream] must be invoked on a Region; FunctionContext type was [%s])",
          ObjectUtils.nullSafeClassName(functionContext)));

      return (RegionFunctionContext) functionContext;
    }

    private void delay(long milliseconds) {

      try {
        Thread.sleep(milliseconds);
      }
      catch (InterruptedException ignore) {
      }
    }
  }
}
