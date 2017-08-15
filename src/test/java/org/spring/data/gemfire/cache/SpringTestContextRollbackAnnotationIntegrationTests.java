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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Resource;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.TransactionEvent;
import org.apache.geode.cache.TransactionListener;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.spring.data.gemfire.cache.SpringTestContextRollbackAnnotationIntegrationTests.TestConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer;
import org.springframework.data.gemfire.transaction.config.EnableGemfireCacheTransactions;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * The SpringTestContextRollbackAnnotationIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = TestConfiguration.class)
public class SpringTestContextRollbackAnnotationIntegrationTests {

  @Resource(name = "Example")
  private Region<Object, Object> example;

  @Autowired
  private TestTransactionalService transactionalService;

  private static void log(String message, Object... args) {
    System.err.printf(message, args);
    System.err.flush();
  }

  @Before
  public void setup() {
    assertThat(this.example).isEmpty();
  }

  @SuppressWarnings("all")
  protected <T> T doInTransaction(Supplier<T> transactionalOperation) {
    try {
      return transactionalOperation.get();
    }
    catch (RuntimeException ignore) {
      return null;
    }
  }

  @Test
  //@Ignore
  @Rollback
  @Transactional
  public void firstTransactionalMethodWithKeyValueRollsback() {

    doInTransaction(() -> this.transactionalService.transactionalMethod(1, "testOne"));

    assertThat(this.example.get(1)).isEqualTo("testOne");

    doInTransaction(() -> this.transactionalService.transactionalMethod(2, "testTwo"));

    assertThat(this.example.get(2)).isEqualTo("testTwo");
  }

  @Test
  @Ignore
  //@Transactional
  public void secondTransactionalMethodWithKeyValueThrowingRuntimeExceptionRollsback() {

    doInTransaction(() -> this.transactionalService.transactionalMethodThrowingRuntimeException(1, "test"));
    doInTransaction(() -> this.transactionalService.transactionalMethodThrowingRuntimeException(2, "anotherTest"));
    doInTransaction(() -> this.transactionalService.transactionalMethodThrowingRuntimeException(3, "yetAnotherTest"));

    verify(this.example, times(1)).put(eq(1), eq("test"));
    verify(this.example, times(1)).put(eq(2), eq("anotherTest"));
    verify(this.example, times(1)).put(eq(3), eq("yetAnotherTest"));
  }

  @Test
  @Ignore
  //@Transactional
  public void thirdTransactionalMethodWithMapThrowingRuntimeExceptionRollsback() {

    Map<Object, Object> map = new HashMap<>();

    map.put(1, "andTest");
    map.put(2, "andAnotherTest");
    map.put(3, "andYetAnotherTest");

    doInTransaction(() -> this.transactionalService.transactionalMethodThrowingRuntimeException(map));

    map.forEach((key, value) -> verify(this.example, times(1)).put(eq(key), eq(value)));
    //verify(this.example, times(1)).putAll(eq(map));
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
  public static class TestTransactionalService {

    @Resource(name = "Example")
    private Region<Object, Object> example;

    @Transactional
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object transactionalMethod(Object key, Object value) {
      return this.example.put(key, value);
    }

    @Transactional
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object transactionalMethodThrowingRuntimeException(Object key, Object value) {

      Object returnValue = this.example.put(key, value);

      assertThat(this.example.get(key)).isEqualTo(value);

      throwRuntimeExceptionIfNotEmpty();

      return returnValue;
    }

    @Transactional
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object transactionalMethodThrowingRuntimeException(Map<Object, Object> map) {

      map.forEach(this::transactionalMethod); // 1. Nested Transaction Scope
      //map.forEach((key, value) -> this.example.put(key, value)); // 2. Multiple Region.put(key, value)
      //this.example.putAll(map); // 3. Region.putAll(:Map)

      map.forEach((key, value) -> assertThat(this.example.get(key)).isEqualTo(value));

      throwRuntimeExceptionIfNotEmpty();

      return null;
    }

    public void throwRuntimeExceptionIfNotEmpty() {

      if (!this.example.isEmpty()) {
        throw new RuntimeException("TEST ROLLBACK");
      }
    }
  }

  @PeerCacheApplication(name = "SpringTestContextRollbackAnnotationIntegrationTests", logLevel = "warning")
  @EnableGemfireCacheTransactions
  static class GemFireConfiguration {

    @Bean
    PeerCacheConfigurer transactionListenerRegisteringConfigurer() {

      return (beanName, cacheFactoryBean) -> {
        cacheFactoryBean.setTransactionListeners(Collections.singletonList(loggingTransactionListener()));
      };
    }

    private TransactionListener loggingTransactionListener() {

      return new TransactionListener() {

        @Override
        public void afterCommit(TransactionEvent event) {
          log("Committing TX [%s]%n", event.getTransactionId());
        }

        @Override
        public void afterFailedCommit(TransactionEvent event) {
          log("Failed to Commit TX [%s]%n", event.getTransactionId());
        }

        @Override
        public void afterRollback(TransactionEvent event) {
          log("Rolling back TX [%s]%n", event.getTransactionId());
        }

        @Override
        public void close() {
        }
      };
    }

    @Bean("Example")
    LocalRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

      LocalRegionFactoryBean<Object, Object> example = new LocalRegionFactoryBean<>();

      example.setCache(gemfireCache);
      example.setClose(false);
      example.setPersistent(false);

      return example;
    }

    @Bean
    BeanPostProcessor spyOnRegionBeanPostProcessor() {

      return new BeanPostProcessor() {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
          return (bean instanceof Region && "Example".equals(beanName) ? spy(bean) : bean);
        }
      };
    }
  }
}
