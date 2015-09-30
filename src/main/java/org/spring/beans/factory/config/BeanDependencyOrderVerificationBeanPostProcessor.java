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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.spring.beans.TestBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.Assert;

/**
 * The OrderVerificationBeanPostProcessor class verifies the order of the Spring container's bean construction based on
 * dependency order.
 *
 * @author John Blum
 * @see org.spring.beans.TestBean
 * @see org.springframework.beans.factory.InitializingBean
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 */
@SuppressWarnings("unused")
public class BeanDependencyOrderVerificationBeanPostProcessor implements BeanPostProcessor, InitializingBean {

  private final AtomicInteger index = new AtomicInteger(0);

  private List<String> beanNameOrderList;

  public final void setBeanNameOrderList(final List<String> beanNameOrderList) {
    Assert.notNull(beanNameOrderList, "The beanOrderList property cannot be null!");
    this.beanNameOrderList = beanNameOrderList;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(beanNameOrderList);
    Assert.isTrue(2 == beanNameOrderList.size());
    System.out.printf("Bean Name List is (%1$s)%n", beanNameOrderList);
  }

  @Override
  public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
    if (bean instanceof TestBean) {
      System.out.printf("Post Processing Bean with Name (%1$s)%n", beanName);
      Assert.isTrue(beanNameOrderList.get(index.getAndIncrement()).equals(beanName));
    }

    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
    return bean;
  }

}
