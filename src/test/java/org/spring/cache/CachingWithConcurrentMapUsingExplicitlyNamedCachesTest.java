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
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.cache.CachingWithConcurrentMapUsingExplicitlyNamedCachesTest.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The CachingWithConcurrentMapUsingExplicitlyNamedCachesTest class is a test suite of test cases testing the contract
 * and functionality of Spring Cache Abstraction using the ConcurrentMap-based Cache Management Strategy
 * with explicitly named "Caches".
 *
 * NOTE: when the Cache(s) [is|are] explicitly named using the ConcurrentMapCacheManager, then "dynamic" is disabled
 * and corresponding the named Cache in the @Cacheable annotation of the cached service method must exist
 * (or be declared).  If no explicitly named Caches are provided to the ConcurrentMapManager constructor, then dynamic
 * is enabled and the Cache will be created at runtime, on the fly.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.cache.CachingWithConcurrentMapUsingExplicitlyNamedCachesTest.ApplicationConfiguration
 * @see org.springframework.cache.Cache
 * @see org.springframework.cache.CacheManager
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.cache.concurrent.ConcurrentMapCacheManager
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfiguration.class)
@SuppressWarnings("unused")
public class CachingWithConcurrentMapUsingExplicitlyNamedCachesTest {

  @Autowired
  private NumberCategoryService numberCategoryService;

  @Test
  public void numberCategoryCaching() {
    assertThat(numberCategoryService.isCacheMiss(), is(false));

    List<NumberCategory> twoCategories = numberCategoryService.classify(2.0);

    assertThat(twoCategories, is(notNullValue()));
    assertThat(twoCategories.size(), is(equalTo(3)));
    assertThat(twoCategories.containsAll(Arrays.asList(
      NumberCategory.EVEN, NumberCategory.POSITIVE, NumberCategory.WHOLE)), is(true));
    assertThat(numberCategoryService.isCacheMiss(), is(true));

    List<NumberCategory> twoCategoriesAgain = numberCategoryService.classify(2.0);

    assertThat(twoCategoriesAgain, is(sameInstance(twoCategories)));
    assertThat(numberCategoryService.isCacheMiss(), is(false));

    List<NumberCategory> negativeThreePointFiveCategories = numberCategoryService.classify(-3.5);

    assertThat(negativeThreePointFiveCategories, is(notNullValue()));
    assertThat(negativeThreePointFiveCategories.size(), is(equalTo(3)));
    assertThat(negativeThreePointFiveCategories.containsAll(Arrays.asList(
      NumberCategory.ODD, NumberCategory.NEGATIVE, NumberCategory.FLOATING)), is(true));
    assertThat(numberCategoryService.isCacheMiss(), is(true));
  }

  @Configuration
  @EnableCaching
  public static class ApplicationConfiguration {

    @Bean
    public CacheManager cacheManager() {
      return new ConcurrentMapCacheManager("Categories");
      //return new ConcurrentMapCacheManager("Temporary");
    }

    @Bean
    public NumberCategoryService numberCategoryService() {
      return new NumberCategoryService();
    }
  }

  @Service
  public static class NumberCategoryService {

    private volatile boolean cacheMiss;

    public boolean isCacheMiss() {
      boolean localCacheMiss = this.cacheMiss;
      this.cacheMiss = false;
      return localCacheMiss;
    }

    protected void setCacheMiss() {
      this.cacheMiss = true;
    }

    @Cacheable("Categories")
    public List<NumberCategory> classify(double number) {
      setCacheMiss();

      List<NumberCategory> categories = new ArrayList<>(3);

      categories.add(isEven(number) ? NumberCategory.EVEN : NumberCategory.ODD);
      categories.add(isPositive(number) ? NumberCategory.POSITIVE : NumberCategory.NEGATIVE);
      categories.add(isWhole(number) ? NumberCategory.WHOLE : NumberCategory.FLOATING);

      return categories;
    }

    protected boolean isEven(double number) {
      return (isWhole(number) && Math.abs(number) % 2 == 0);
    }

    protected boolean isFloating(double number) {
      return !isWhole(number);
    }

    protected boolean isNegative(double number) {
      return (number < 0);
    }

    protected boolean isOdd(double number) {
      return !isEven(number);
    }

    protected boolean isPositive(double number) {
      return (number > 0);
    }

    protected boolean isWhole(double number) {
      return (number == Math.floor(number));
    }
  }

  public enum NumberCategory {
    EVEN,
    FLOATING,
    NEGATIVE,
    ODD,
    POSITIVE,
    WHOLE
  }

}
