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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.cache.ConditionalCachingTest.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * The ConditionalCachingTest class is a test suite of test cases testing the contract and functionality
 * of Spring's Cache Abstraction using conditional caching with the 'conditional' and 'unless' attributes
 * on the @Cacheable annotation.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfiguration.class)
@SuppressWarnings("all")
public class ConditionalCachingTest {

  @Autowired
  private FactorialService factorialService;

  @Resource(name = "factorialsStore")
  private ConcurrentMap<Object, Object> factorials;

  @Test
  public void conditionalCaching() {
    assertThat(factorialService.isCacheMiss(), is(false));
    assertThat(factorials.isEmpty(), is(true));
    assertThat(factorialService.factorial(0l), is(equalTo(1l)));
    assertThat(factorialService.factorial(1l), is(equalTo(1l)));
    assertThat(factorialService.factorial(2l), is(equalTo(2l)));
    assertThat(factorialService.isCacheMiss(), is(true));
    assertThat(factorials.size(), is(equalTo(0)));
    assertThat(factorialService.factorial(5l), is(equalTo(120l)));
    assertThat(factorialService.isCacheMiss(), is(true));
    assertThat(factorials.size(), is(equalTo(1)));
    assertThat(factorials.get(5l), is(equalTo(120l)));
    assertThat(factorialService.factorial(5l), is(equalTo(120l)));
    assertThat(factorialService.isCacheMiss(), is(false));
    assertThat(factorialService.factorial(3l), is(equalTo(6l)));
    assertThat(factorialService.isCacheMiss(), is(true));
    // WRONG!!! 'condition' attribute also prevents the (@Cacheable method) result from being cached
    // assertThat(factorials.size(), is(equalTo(2)));
    assertThat(factorials.size(), is(equalTo(1)));
    // assertThat(factorials.get(3l), is(equalTo(6l)));
    assertThat(factorials.get(5l), is(equalTo(120l)));
    assertThat(factorialService.factorial(3l), is(equalTo(6l)));
    // assertThat(factorialService.isCacheMiss(), is(false));
    assertThat(factorialService.isCacheMiss(), is(true));
    assertThat(factorials.size(), is(equalTo(1)));
    assertThat(factorials.get(5l), is(equalTo(120l)));
  }

  @Configuration
  @EnableCaching
  public static class ApplicationConfiguration {

    @Bean
    public FactorialService factorialService() {
      return new FactorialService();
    }

    @Bean
    public SimpleCacheManager cacheManager(Cache factorials) {
      SimpleCacheManager cacheManager = new SimpleCacheManager();
      cacheManager.setCaches(Collections.singletonList(factorials));
      return cacheManager;
    }

    @Bean
    public ConcurrentMapCacheFactoryBean factorials() {
      ConcurrentMapCacheFactoryBean cacheFactoryBean = new ConcurrentMapCacheFactoryBean();
      cacheFactoryBean.setName("Factorials");
      cacheFactoryBean.setStore(factorialsStore());
      return cacheFactoryBean;
    }

    @Bean
    public ConcurrentMap<Object, Object> factorialsStore() {
      return new ConcurrentHashMap<>();
    }
  }

  @Service
  public static class FactorialService {

    private volatile boolean cacheMiss = false;

    public boolean isCacheMiss() {
      boolean cacheMiss = this.cacheMiss;
      this.cacheMiss = false;
      return cacheMiss;
    }

    // NOTE this is a naive implementation of the Factorial algorithm!
    @Cacheable(value = "Factorials", condition = "#number > 4", unless = "#result < 3")
    public long factorial(long number) {
      cacheMiss = true;

      Assert.isTrue(number >= 0, String.format("Number (%1$d) must be greater than equal to 0", number));

      if (number <= 2l) {
        return (number < 2l ? 1l : 2l);
      }

      long result = number;

      while (--number > 0) {
        result *= number;
      }

      return result;
    }
  }

}
