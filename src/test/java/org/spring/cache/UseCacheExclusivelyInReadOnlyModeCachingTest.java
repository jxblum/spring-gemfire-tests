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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.spring.cache.UseCacheExclusivelyInReadOnlyModeCachingTest.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * The UseCacheExclusivelyInReadOnlyModeCachingTest class...
 *
 * @author John Blum
 * @see org.junit.FixMethodOrder
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.junit.runners.MethodSorters#NAME_ASCENDING
 * @see org.aspectj.lang.annotation.Aspect
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @link http://stackoverflow.com/questions/34370107/using-spring-cache-read-only-how-set-spring-cache-redis-read-only/34407309
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = ApplicationConfiguration.class)
@SuppressWarnings("all")
public class UseCacheExclusivelyInReadOnlyModeCachingTest {

  @Autowired
  private FactorialService factorialService;

  @BeforeClass
  public static void setupBeforeClass() {
    System.setProperty("app.mode.read-only", "true");
  }

  @Before
  public void setup() {
    System.setProperty("app.mode.read-only", "false");
  }

  @Test
  @DirtiesContext
  public void readOnlyCaching() {
    assertThat(factorialService.isCacheMiss(), is(false));
    assertThat(factorialService.factorial(5l), is(equalTo(120l)));
    assertThat(factorialService.isCacheMiss(), is(false));
    assertThat(factorialService.factorial(10l), is(nullValue()));
    assertThat(factorialService.isCacheMiss(), is(false));
  }

  @Test
  public void readWriteCaching() {
    assertThat(factorialService.isCacheMiss(), is(false));
    assertThat(factorialService.factorial(5l), is(equalTo(120l)));
    assertThat(factorialService.isCacheMiss(), is(false));
    assertThat(factorialService.factorial(10l), is(equalTo(3628800l)));
    assertThat(factorialService.isCacheMiss(), is(true));
    assertThat(factorialService.factorial(10l), is(equalTo(3628800l)));
    assertThat(factorialService.isCacheMiss(), is(false));
  }

  @Configuration
  @EnableAspectJAutoProxy
  @EnableCaching(order = Ordered.HIGHEST_PRECEDENCE)
  public static class ApplicationConfiguration {

    @Bean
    public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
      return new PropertyPlaceholderConfigurer();
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
      ConcurrentMap<Object, Object> factorialsStore = new ConcurrentHashMap<>();
      factorialsStore.put(0l, 1l);
      factorialsStore.put(1l, 1l);
      factorialsStore.put(2l, 2l);
      factorialsStore.put(3l, 6l);
      factorialsStore.put(4l, 24l);
      factorialsStore.put(5l, 120l);
      factorialsStore.put(6l, 720l);
      factorialsStore.put(7l, 5040l);
      factorialsStore.put(8l, 40320l);
      factorialsStore.put(9l, 362880l);
      return factorialsStore;
    }

    @Bean
    public FactorialService factorialService() {
      return new FactorialService();
    }

    @Bean
    public UseCacheExclusivelyInReadOnlyModeAspect readOnlyAspect(@Value("${app.mode.read-only:false}") boolean readOnly) {
      return new UseCacheExclusivelyInReadOnlyModeAspect(readOnly);
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
    @Cacheable(value = "Factorials", unless = "T(java.lang.System).getProperty('app.mode.read-only', 'false')")
    public Long factorial(long number) {
      System.err.printf("factorial(%1$d)%n", number);

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

  @Aspect
  @Order(Ordered.HIGHEST_PRECEDENCE + 1)
  public static class UseCacheExclusivelyInReadOnlyModeAspect {

    private final boolean readOnly;

    public UseCacheExclusivelyInReadOnlyModeAspect(final boolean readOnly) {
      this.readOnly = readOnly;
    }

    protected boolean isReadOnly() {
      return readOnly;
    }

    protected boolean isNotReadOnly() {
      return !isReadOnly();
    }

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object handleReadOnlyMode(ProceedingJoinPoint joinPoint) throws Throwable {
      System.err.printf("**Around Advice Invoked (read-only = %1$s)**%n", isReadOnly());
      if (isNotReadOnly()) {
        System.err.printf("**Proceeding with Joint Point Execution - %1$s(%2$s)**%n",
          joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        return joinPoint.proceed();
      }

      return null;
    }
  }

}
