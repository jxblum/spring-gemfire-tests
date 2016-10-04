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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
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
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.cache.Cache
 * @see org.springframework.cache.CacheManager
 * @see org.springframework.cache.concurrent.ConcurrentMapCacheManager
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @link http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#cache
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = CachingApplicationConfiguration.class)
@SuppressWarnings("unused")
public class CachingCollectionElementsIndividuallyWithConcurrentMapTest {

  @Autowired
  private CalculatorService calculatorService;

  protected void assertFactorials(List<Long> actualValues, long... expectedValues) {
    assertThat(actualValues, is(notNullValue()));
    assertThat(expectedValues, is(notNullValue()));
    assertThat(actualValues.size(), is(equalTo(expectedValues.length)));

    int index = 0;

    for (long actualValue : actualValues) {
      assertThat(actualValue, is(equalTo(expectedValues[index++])));
    }
  }

  @Test
  public void cacheHitsAndMisses() {
    assertThat(calculatorService.isCacheMiss(), is(false));

    List<Long> results = calculatorService.factorials(Arrays.asList(1l, 2l, 3l, 4l, 5l));

    assertThat(calculatorService.isCacheMiss(), is(true));
    assertFactorials(results, 1, 2, 6, 24, 120);

    results = calculatorService.factorials(Arrays.asList(1l, 2l, 3l, 4l, 5l));

    assertThat(calculatorService.isCacheMiss(), is(false));
    assertFactorials(results, 1, 2, 6, 24, 120);

    results = calculatorService.factorials(Arrays.asList(6l, 7l, 8l, 9l));

    assertThat(calculatorService.isCacheMiss(), is(true));
    assertFactorials(results, 720, 5040, 40320, 362880);
  }

  @Test
  public void partialMissesAreConsideredACompleteCacheMiss() {
    List<Long> results = calculatorService.factorials(Arrays.asList(2l, 4l, 8l, 16l));

    // in fact, the 16! == null, because it is not in the cache!!!
    assertThat(calculatorService.isCacheMiss(), is(true));
    assertFactorials(results, 2, 24, 40320, 20922789888000l);
  }

  @Configuration
  @EnableCaching
  protected static class CachingApplicationConfiguration {

    @Bean
    public CacheManager cacheManager() {
      return new ConcurrentMapCacheManager() {
        @Override protected Cache createConcurrentMapCache(final String name) {
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
    protected boolean areAllKeysPresentInCache(final Iterable<?> keys) {
      ConcurrentMap nativeCache = (ConcurrentMap) getNativeCache();

      boolean result = true;

      for (Object key : keys) {
        result &= nativeCache.containsKey(key);
      }

      return result;
    }
  }

  protected abstract static class CollectionHandlingDecoratedCache implements Cache {

    private final Cache cache;

    protected CollectionHandlingDecoratedCache(final Cache cache) {
      Assert.notNull(cache, "Cache must not be null");
      this.cache = cache;
    }

    protected Cache getCache() {
      return cache;
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
      int size = 0;

      for (Object element : iterable) {
        size++;
      }

      return size;
    }

    protected <T> List<T> toList(Iterable<T> iterable) {
      List<T> list = new ArrayList<>();

      for (T element : iterable) {
        list.add(element);
      }

      return list;
    }

    @Override
    @SuppressWarnings("all")
    public ValueWrapper get(final Object key) {
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
    public <T> T get(final Object key, final Class<T> type) {
      if (key instanceof Iterable) {
        Assert.isAssignable(Iterable.class, type, String.format(
          "Expected return type (%1$s) must be Iterable when querying multiple keys (%2$s)",
            type.getName(), key));

        return (T) get(key).get();
      }

      return getCache().get(key, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
      return (T) get(key, Object.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void put(final Object key, final Object value) {
      if (key instanceof Iterable) {
        Assert.isInstanceOf(Iterable.class, value, String.format(
          "Value (%1$s) must be an instance of Iterable when caching multiple keys (%2$s)",
            ObjectUtils.nullSafeClassName(value), key));

        Iterable<?> keys = (Iterable<?>) key;
        List<Object> values = toList((Iterable) value);

        int sizeOfKeys = sizeOf(keys);
        int sizeOfValues = values.size();

        Assert.isTrue(sizeOfValues == sizeOfKeys, String.format(
          "The number of values (%1$d) must match the number of keys (%2$d)",
            sizeOfValues, sizeOfKeys));

        int index = 0;

        for (Object singleKey : keys) {
          getCache().put(singleKey, values.get(index++));
        }
      }
      else {
        getCache().put(key, value);
      }
    }

    @Override
    public ValueWrapper putIfAbsent(final Object key, final Object value) {
      if (key instanceof Iterable) {
        throw new UnsupportedOperationException(String.format(
          "Cache (%1$s) wrapping (%2$s) does not currently support putIfAbsent for multiple key/values",
            ObjectUtils.nullSafeClassName(this), ObjectUtils.nullSafeClassName(getNativeCache())));
      }

      return getCache().putIfAbsent(key, value);
    }

    @Override
    public void evict(final Object key) {
      if (key instanceof Iterable) {
        for (Object singleKey : (Iterable) key) {
          getCache().evict(singleKey);
        }
      }
      else {
        getCache().evict(key);
      }
    }

    @Override
    public void clear() {
      getCache().clear();
    }
  }

  @Service
  protected static class CalculatorService {

    private boolean cacheMiss;

    public synchronized boolean isCacheMiss() {
      boolean localCacheMiss = cacheMiss;
      setCacheMiss(false);
      return localCacheMiss;
    }

    public synchronized void setCacheMiss(boolean value) {
      cacheMiss = value;
    }

    // WARNING this implementation is purely for example purposes; a properly implemented factorial algorithm
    // should use BigInteger!
    @Cacheable("Factorials")
    public long factorial(long value) {
      Assert.isTrue(value >= 0, String.format("value (%1$d) must be greater than equal to 0", value));

      setCacheMiss(true);

      if (value <= 2) {
        return (value == 2 ? 2 : 1);
      }

      long result = value;

      while (--value > 0) {
        result *= value;
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
