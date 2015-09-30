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

package org.spring.data.gemfire.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.gemfire.SubRegionFactoryBean;
import org.springframework.util.Assert;

/**
 * The ForceGemFireRegionCreationBeanPostProcessor class...
 *
 * @author John Blum
 * @see org.springframework.beans.factory.InitializingBean
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.ApplicationContextAware
 * @since 1.3.2
 * @deprecated The issue in Spring Data GemFire has been resolved in SGF-195!
 * @link https://jira.springsource.org/browse/SGF-195
 */
@Deprecated
@SuppressWarnings("unused")
public class ForceGemFireSubRegionCreationBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor, InitializingBean {

  private ApplicationContext applicationContext;

  @Override
  public final void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    Assert.notNull(applicationContext != null, "The reference to the ApplicationContext cannot be null!");
    this.applicationContext = applicationContext;
  }

  protected ApplicationContext getApplicationContext() {
    Assert.state(applicationContext != null, "The reference to the ApplicationContext was not initialized properly!");
    return applicationContext;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    getApplicationContext();
  }

  @Override
  @SuppressWarnings("deprecation")
  public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
    System.out.printf("Bean with name (%1$s) was instance of (%2$s)...%n", beanName, bean.getClass().getName());

    if (bean instanceof SubRegionFactoryBean) {
      try {
        ((SubRegionFactoryBean) bean).afterPropertiesSet();
      }
      catch (Exception e) {
        System.err.printf("Failed to initialized Bean with name (%1$s)%n", beanName);
      }
    }

    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
    return bean;
  }

}
