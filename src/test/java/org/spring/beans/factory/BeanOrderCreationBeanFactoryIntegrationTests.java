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

package org.spring.beans.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests testing the order of bean instantiation, configuration and initialization
 * using the Spring container and the effects of applying ordering.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.core.annotation.Order
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class BeanOrderCreationBeanFactoryIntegrationTests {

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  public void creationOrderIsEstablishedByRegistrationOrder() {

    TestBean beanA = this.applicationContext.getBean("B", TestBean.class);

    assertThat(beanA).isNotNull();
    assertThat(beanA.getOrder()).isEqualTo(1);

    TestBean beanB = this.applicationContext.getBean("A", TestBean.class);

    assertThat(beanB).isNotNull();
    assertThat(beanB.getOrder()).isEqualTo(2);
  }

  @Configuration
  static class TestConfiguration {

    @Bean("B")
    @Order(2)
    TestBean beanOne() {
      return new TestBean();
    }

    @Bean("A")
    @Order(1)
    TestBean beanTwo() {
      return new TestBean();
    }
  }

  static class TestBean {

    private static final AtomicInteger globalOrder = new AtomicInteger(0);

    private final int order;

    TestBean() {
      this.order = globalOrder.incrementAndGet();
    }

    int getOrder() {
      return this.order;
    }

    @Override
    public String toString() {
      return String.valueOf(getOrder());
    }
  }
}
