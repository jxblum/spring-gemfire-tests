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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Supplier;
import javax.annotation.Resource;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.cache.SpringTestContextRollbackAnnotationIntegrationTest.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.ReplicatedRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.transaction.config.EnableGemfireCacheTransactions;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * The SpringTestContextRollbackAnnotationIntegrationTest class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@SuppressWarnings("unused")
public class SpringTestContextRollbackAnnotationIntegrationTest {

  @Resource(name = "Example")
  private Region<Object, Object> example;

  @Autowired
  private TestTransactionalService transactionalService;

  private <T> T doInTransaction(Supplier<T> supplier) {

    try {
      return supplier.get();
    }
    catch (Throwable ignore) {
      return null;
    }
  }

  @Before
  public void setup() {
    assertThat(this.example).isEmpty();
  }

  @Test
  @Rollback
  @Transactional
  public void testMethodWithRollback() {

    doInTransaction(() -> this.transactionalService.transactionalMethod(1, "test"));

    assertThat(this.example.get(1)).isEqualTo("test");

    doInTransaction(() -> this.transactionalService.transactionalMethod(2, "anotherTest"));

    assertThat(this.example.get(2)).isEqualTo("anotherTest");
  }

  @After
  public void tearDown() {
    assertThat(this.example).isEmpty();
  }

  @Configuration
  @Import(GemFireConfiguration.class)
  static class TestConfiguration {

    @Bean
    TestTransactionalService transactionalService() {
      return new TestTransactionalService();
    }
  }

  @Service
  static class TestTransactionalService {

    @Resource(name = "Example")
    private Region<Object, Object> example;

    @Transactional
    Object transactionalMethod(Object key, Object value) {
      return this.example.put(key, value);
    }

    @Transactional
    Object anotherTransactionalMethod(Object key, Object value) {

      Object returnValue = this.example.put(key, value);

      if (this.example.size() > 1) {
        throw new RuntimeException("TEST ROLLBACK");
      }

      return returnValue;
    }
  }

  @PeerCacheApplication(name = "SpringTestContextRollbackAnnotationIntegrationTest")
  @EnableGemfireCacheTransactions
  static class GemFireConfiguration {

    @Bean("Example")
    ReplicatedRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

      ReplicatedRegionFactoryBean<Object, Object> example = new ReplicatedRegionFactoryBean<>();

      example.setCache(gemfireCache);
      example.setClose(false);
      example.setPersistent(false);

      return example;
    }
  }
}
