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

package org.spring.beans.factory.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.beans.factory.config.SpringInitializationCallbackWithBeanPostProcessorTests.TestConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The SpringInitializationCallbackWithBeanPostProcessorTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@SuppressWarnings("unused")
public class SpringInitializationCallbackWithBeanPostProcessorTests {

  private static final List<String> CALL_STACK = new ArrayList<>();

  @Autowired
  private TestBean bean;

  static void toSystemOut(Iterable<String> iterable) {
    for (String element : iterable) {
      System.out.println(element);
    }
  }

  @Test
  public void beanIsNotNull() {
    assertThat(bean).isNotNull();
    toSystemOut(CALL_STACK);
  }

  static class TestBean implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
      CALL_STACK.add(String.format("%s.afterPropertiesSet()", getClass().getSimpleName()));
    }
  }

  static class TestFactoryBean implements FactoryBean<TestBean>, InitializingBean {

    private TestBean testBean;

    @Override
    public void afterPropertiesSet() throws Exception {
      CALL_STACK.add(String.format("%s.afterPropertiesSet()", getClass().getSimpleName()));
      testBean = new TestBean();
      testBean.afterPropertiesSet();
    }

    @Override
    public TestBean getObject() throws Exception {
      CALL_STACK.add(String.format("%s.getObject()", getClass().getSimpleName()));
      return testBean;
    }

    @Override
    public Class<?> getObjectType() {
      return (testBean != null ? testBean.getClass() : TestBean.class);
    }

    @Override
    public boolean isSingleton() {
      return true;
    }
  }

  @Configuration
  static class TestConfiguration {

    @Bean
    TestFactoryBean testBean() {
      return new TestFactoryBean();
    }

    @Bean
    BeanPostProcessor testBeanPostProcessor() {
      return new BeanPostProcessor() {
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
          if (beanName.endsWith("testBean")) {
            CALL_STACK.add(String.format("BeanPostProcessor.postProcessBeforeInitialization(%1$s, %2$s)",
              bean.getClass().getSimpleName(), beanName));
          }

          return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
          if (beanName.endsWith("testBean")) {
            CALL_STACK.add(String.format("BeanPostProcessor.postProcessAfterInitialization(%1$s, %2$s)",
              bean.getClass().getSimpleName(), beanName));
          }

          return bean;
        }
      };
    }
  }
}
