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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.cache.CachingWithConcurrentMapUsingExplicitlyNamedCachesTest.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class CachingWithConcurrentMapUsingExplicitlyNamedCachesTest
    extends AbstractSpringCacheAbstractionIntegrationTest {

  @Autowired
  private NumberClassificationService numberClassificationService;

  @Test
  public void numberCategoryCaching() {
    assertThat(numberClassificationService.wasCacheMiss(), is(false));

    List<NumberClassification> twoCategories = numberClassificationService.classify(2.0);

    assertThat(twoCategories, is(notNullValue()));
    assertThat(twoCategories.size(), is(equalTo(3)));
    assertThat(twoCategories.containsAll(Arrays.asList(
      NumberClassification.EVEN, NumberClassification.POSITIVE, NumberClassification.WHOLE)), is(true));
    assertThat(numberClassificationService.wasCacheMiss(), is(true));

    List<NumberClassification> twoCategoriesAgain = numberClassificationService.classify(2.0);

    assertThat(twoCategoriesAgain, is(sameInstance(twoCategories)));
    assertThat(numberClassificationService.wasCacheMiss(), is(false));

    List<NumberClassification> negativeThreePointFiveCategories = numberClassificationService.classify(-3.5);

    assertThat(negativeThreePointFiveCategories, is(notNullValue()));
    assertThat(negativeThreePointFiveCategories.size(), is(equalTo(3)));
    assertThat(negativeThreePointFiveCategories.containsAll(Arrays.asList(
      NumberClassification.ODD, NumberClassification.NEGATIVE, NumberClassification.FLOATING)), is(true));
    assertThat(numberClassificationService.wasCacheMiss(), is(true));
  }

  @Configuration
  @EnableCaching
  static class ApplicationConfiguration {

    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager("Categories");
      //return new ConcurrentMapCacheManager("Temporary");
    }

    @Bean NumberClassificationService numberCategoryService() {
      return new NumberClassificationService();
    }
  }
}
