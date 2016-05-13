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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The CachingVarArgsServiceMethodTest class is a test suite of test case testing Spring's Cache Abstraction
 * with var args {@link org.springframework.cache.annotation.Cacheable} service methods.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.cache.AbstractSpringCacheAbstractionIntegrationTest
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see <a href="http://stackoverflow.com/questions/37194777/spring-cacheable-produces-arrayindexoutofboundsexception-with-varargs">Spring Cacheable produces ArrayIndexOutOfBoundsException with varArgs</a>
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class CachingVarArgsServiceMethodTest extends AbstractSpringCacheAbstractionIntegrationTest {

  @Autowired
  private MathService mathService;

  @Before
  public void setup() {
    mathService.wasCacheMiss();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void calculationsAreCacheable() {
    assertThat(mathService.wasCacheMiss(), is(false));
    assertThat(mathService.calculate(10l, multiplyBy(10l).andThen(subtract(50l)).andThen(divideBy(2l))),
      is(equalTo(25l)));
    assertThat(mathService.wasCacheMiss(), is(true));
    assertThat(mathService.calculate(10l), is(equalTo(25l)));
    assertThat(mathService.wasCacheMiss(), is(false));
    assertThat(mathService.calculate(10l, powerOf(3), divideBy(4), multiplyBy(2)), is(equalTo(25l))); // actually, 512!
    assertThat(mathService.wasCacheMiss(), is(false));
    assertThat(mathService.calculate(12l, divideBy(2l), multiplyBy(6l)), is(equalTo(36l)));
    assertThat(mathService.wasCacheMiss(), is(true));
  }

  @Test
  public void factorialsAreCacheable() {
    assertThat(mathService.wasCacheMiss(), is(false));
    assertThat(mathService.factorial(4l), is(equalTo(24l)));
    assertThat(mathService.wasCacheMiss(), is(true));
    assertThat(mathService.factorial(4l), is(equalTo(24l)));
    assertThat(mathService.wasCacheMiss(), is(false));
    assertThat(mathService.factorial(2l), is(equalTo(2l)));
    assertThat(mathService.wasCacheMiss(), is(true));
  }

  @Configuration
  @EnableCaching
  static class CachingApplicationConfiguration {

    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager();
    }

    @Bean
    MathService mathService() {
      return new MathService();
    }
  }
}
