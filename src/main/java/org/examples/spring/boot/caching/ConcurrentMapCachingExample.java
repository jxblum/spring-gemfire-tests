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

package org.examples.spring.boot.caching;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * The ConcurrentMapCachingExample class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@EnableCaching
@SuppressWarnings("unused")
public class ConcurrentMapCachingExample implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(ConcurrentMapCachingExample.class, args);
  }

  @Autowired
  private ExampleCacheableService exampleService;

  @Override
  public void run(String... args) throws Exception {
    assertThat(exampleService.isCacheMiss()).isFalse();
    assertThat(exampleService.computeValue("one").intValue()).isEqualTo(1);
    assertThat(exampleService.isCacheMiss()).isTrue();
    assertThat(exampleService.computeValue("one").intValue()).isEqualTo(1);
    assertThat(exampleService.isCacheMiss()).isFalse();
  }

  @Bean
  ConcurrentMapCacheManager cacheManager() {
    return new ConcurrentMapCacheManager();
  }
}

@Service
class ExampleCacheableService {

  boolean cacheMiss;

  boolean isCacheMiss() {
    boolean cacheMiss = this.cacheMiss;
    this.cacheMiss = false;
    return cacheMiss;
  }

  @Cacheable("Example")
  public Short computeValue(String cacheKey) {
    System.out.printf("Computing value for [%s]%n", cacheKey);
    cacheMiss = true;
    return 1;
  }
}
