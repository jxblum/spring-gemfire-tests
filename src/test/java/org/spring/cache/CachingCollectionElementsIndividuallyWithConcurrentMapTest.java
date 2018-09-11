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

package org.spring.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.spring.cache.CachingCollectionElementsIndividuallyWithConcurrentMapTest.CachingApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The CachingCollectionElementsIndividuallyWithConcurrentMapTest class is a test suite of test cases testing Spring's
 * Cache abstraction's ability to handle Collections of keys and values properly by customizing (decorating)
 * the {@link CacheManager}, and specifically, the {@link Cache} implementation out-of-the-box.
 *
 * WARNING this (example) implementation does not handle the putIfAbsent operation!
 * WARNING this (example) implementation does not (efficiently) handle partial Cache misses (i.e. any single key
 *   not present in the cache is considered a cache miss even if other keys are present)!
 *
 * @author John Blum
 * @author Stefan Schrass
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.cache.Cache
 * @see org.springframework.cache.CacheManager
 * @see org.springframework.cache.concurrent.ConcurrentMapCacheManager
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see <a href="http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#cache">Cache Abstraction</a>
 * @see <a href="http://stackoverflow.com/questions/33657881/what-strategies-exist-for-using-spring-cache-on-methods-that-take-an-array-or-co">What strategies exist for using Spring Cache on methods that take an array or collection parameter</a>
 * @see <a href="http://stackoverflow.com/questions/41966690/putting-all-returned-elements-into-a-spring-boot-cache-using-annotations">Putting all returned elements into a Spring-Boot cache using annotations</a>
 * @see <a href="https://stackoverflow.com/questions/44529029/spring-cache-with-collection-of-items-entities">Spring Cache with collection of items/entities</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = CachingApplicationConfiguration.class)
@SuppressWarnings("unused")
public class CachingCollectionElementsIndividuallyWithConcurrentMapTest {

  @Autowired
  private CalculatorService calculatorService;

  private void assertFactorials(List<Long> actualValues, Long... expectedValues) {

    assertThat(actualValues).isNotNull();
    assertThat(expectedValues).isNotNull();
    assertThat(actualValues).containsExactly(expectedValues);
  }

  @Test
  public void cacheHitsAndMisses() {

    assertThat(this.calculatorService.isCacheMiss()).isFalse();

    List<Long> results = this.calculatorService.factorials(Arrays.asList(1L, 2L, 3L, 4L, 5L));

    assertThat(this.calculatorService.isCacheMiss()).isTrue();
    assertFactorials(results, 1L, 2L, 6L, 24L, 120L);

    results = this.calculatorService.factorials(Arrays.asList(1L, 2L, 3L, 4L, 5L));

    assertThat(this.calculatorService.isCacheMiss()).isFalse();
    assertFactorials(results, 1L, 2L, 6L, 24L, 120L);

    results = this.calculatorService.factorials(Arrays.asList(6L, 7L, 8L, 9L));

    assertThat(this.calculatorService.isCacheMiss()).isTrue();
    assertFactorials(results, 720L, 5040L, 40320L, 362880L);
  }

  @Test
  public void partialMissesAreConsideredACompleteCacheMiss() {

    List<Long> results = this.calculatorService.factorials(Arrays.asList(2L, 4L, 8L, 16L));

    // in fact, the 16! == null, because it is not in the cache!!!
    assertThat(this.calculatorService.isCacheMiss()).isTrue();
    assertFactorials(results, 2L, 24L, 40320L, 20922789888000L);
  }

  @Configuration
  @EnableCaching
  protected static class CachingApplicationConfiguration {

    @Bean
    public CacheManager cacheManager() {

      return new ConcurrentMapCacheManager() {

        @Override
        protected Cache createConcurrentMapCache(String name) {
          return new ConcurrentMapCollectionHandlingDecoratedCache(super.createConcurrentMapCache(name));
        }
      };
    }

    @Bean
    public CalculatorService calculatorService() {
      return new CalculatorService();
    }
  }

  protected static class ConcurrentMapCollectionHandlingDecoratedCache extends CollectionHandlingDecoratedCache {

    protected ConcurrentMapCollectionHandlingDecoratedCache(final Cache cache) {
      super(cache);
    }

    @Override
    protected boolean areAllKeysPresentInCache(Iterable<?> keys) {

      ConcurrentMap nativeCache = (ConcurrentMap) getNativeCache();

      return StreamSupport.stream(keys.spliterator(), false).allMatch(nativeCache::containsKey);
    }
  }

  protected abstract static class CollectionHandlingDecoratedCache implements Cache {

    private final Cache cache;

    protected CollectionHandlingDecoratedCache(Cache cache) {

      Assert.notNull(cache, "Cache must not be null");

      this.cache = cache;
    }

    protected Cache getCache() {
      return this.cache;
    }

    @Override
    public String getName() {
      return getCache().getName();
    }

    @Override
    public Object getNativeCache() {
      return getCache().getNativeCache();
    }

    protected abstract boolean areAllKeysPresentInCache(Iterable<?> keys);

    @SuppressWarnings("unused")
    protected int sizeOf(Iterable<?> iterable) {
      return Long.valueOf(StreamSupport.stream(iterable.spliterator(), false).count()).intValue();
    }

    protected <T> List<T> toList(Iterable<T> iterable) {
      return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("all")
    public ValueWrapper get(Object key) {

      if (key instanceof Iterable) {

        Iterable<?> keys = (Iterable<?>) key;

        if (!areAllKeysPresentInCache(keys)) {
          return null;
        }

        Collection<Object> values = new ArrayList<>();

        for (Object singleKey : keys) {
          values.add(getCache().get(singleKey).get());
        }

        return () -> values;
      }

      return getCache().get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {

      if (key instanceof Iterable) {

        Assert.isAssignable(Iterable.class, type,
          String.format("Expected return type [%1$s] must be Iterable when querying multiple keys [%2$s]",
            type.getName(), key));

        return (T) Optional.ofNullable(get(key)).map(Cache.ValueWrapper::get).orElse(null);
      }

      return getCache().get(key, type);
    }

    @Override
    @SuppressWarnings("all")
    public <T> T get(Object key, Callable<T> valueLoader) {
      return (T) get(key, Object.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void put(@NonNull Object key, Object value) {

      if (key instanceof Iterable) {

        Assert.isInstanceOf(Iterable.class, value,
          String.format("Value [%1$s] must be an instance of Iterable when caching multiple keys [%2$s]",
            ObjectUtils.nullSafeClassName(value), key));

        pairsFromKeysAndValues(toList((Iterable<?>) key), toList((Iterable<?>) value))
          .forEach(pair -> getCache().put(pair.getFirst(), pair.getSecond()));
      }
      else {
        getCache().put(key, value);
      }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {

      if (key instanceof Iterable) {

        Assert.isInstanceOf(Iterable.class, value,
          String.format("Value [%1$s] must be an instance of Iterable when caching multiple keys [%2$s]",
            ObjectUtils.nullSafeClassName(value), key));

        return () -> pairsFromKeysAndValues(toList((Iterable<?>) key), toList((Iterable<?>) value)).stream()
          .map(pair -> getCache().putIfAbsent(pair.getFirst(), pair.getSecond()))
          .collect(Collectors.toList());
      }

      return getCache().putIfAbsent(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void evict(Object key) {

      if (key instanceof Iterable) {
        StreamSupport.stream(((Iterable) key).spliterator(), false).forEach(getCache()::evict);
      }
      else {
        getCache().evict(key);
      }
    }

    @Override
    public void clear() {
      getCache().clear();
    }

    private <K, V> List<Pair<K, V>> pairsFromKeysAndValues(List<K> keys, List<V> values) {

      final int keysSize = keys.size();

      Assert.isTrue(keysSize == values.size(),
        String.format("The number of values [%1$d] must match the number of keys [%2$d]",
          values.size(), keysSize));

      return IntStream.range(0, keysSize)
        .mapToObj(index -> Pair.of(keys.get(index), values.get(index)))
        .collect(Collectors.toList());

    }
  }

  @Service
  protected static class CalculatorService {

    private boolean cacheMiss;

    public synchronized boolean isCacheMiss() {

      boolean cacheMiss = this.cacheMiss;

      setCacheMiss(false);

      return cacheMiss;
    }

    public synchronized void setCacheMiss(boolean cacheMiss) {
      this.cacheMiss = cacheMiss;
    }

    // WARNING this implementation is purely for example purposes;
    // A properly implemented factorial algorithm should use BigInteger!
    @Cacheable("Factorials")
    public long factorial(long number) {

      Assert.isTrue(number >= 0,
        String.format("Number [%d] must be greater than equal to 0", number));

      setCacheMiss(true);

      if (number <= 2L) {
        return number == 2L ? 2L : 1L;
      }

      long result = number;

      while (--number > 0L) {
        result *= number;
      }

      return result;
    }

    @Cacheable("Factorials")
    public List<Long> factorials(List<Long> values) {

      setCacheMiss(true);

      List<Long> results = new ArrayList<>(values.size());

      results.addAll(values.stream().map(this::factorial).collect(Collectors.toList()));

      return results;
    }
  }
}
