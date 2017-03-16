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

import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.geode.cache.client.Pool;
import org.apache.geode.distributed.internal.InternalDistributedSystem;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The DistributedSystemValidationBeanPostProcessor class is a Spring BeanPostProcessor used to compare and validate
 * the GemFire DistributedSystem "created" by Spring Data GemFire's PoolFactoryBean in order to create a GemFire Pool
 * as well as the DistributedSystem "resolved" by the ClientCacheFactoryBean when the GemFire ClientCache is created.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.distributed.internal.InternalDistributedSystem
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class DistributedSystemValidationBeanPostProcessor implements BeanFactoryAware, BeanPostProcessor {

  private BeanFactory beanFactory;

  @Override
  public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  protected BeanFactory getBeanFactory() {
    Assert.notNull(beanFactory, "The Spring BeanFactory was not properly initialized");
    return beanFactory;
  }

  protected void log(String label, Properties properties) {
    Set<String> propertyNames = new TreeSet<>(properties.stringPropertyNames());

    StringBuilder builder  = new StringBuilder("[\n");

    int count = 0;

    for (String propertyName : propertyNames) {
      builder.append(++count > 1 ? ",\n" : "");
      builder.append(String.format("  %1$s = %2$s", propertyName, properties.getProperty(propertyName)));
    }

    builder.append("\n]");

    System.out.printf("%1$s (%2$s)%n", label, builder.toString());
  }

  protected void logDiff(String sourceLabel, Properties source, String targetLabel, Properties target) {
    for (String propertyName : target.stringPropertyNames()) {
      if (source.containsKey(propertyName)) {
        String sourcePropertyValue = StringUtils.trimWhitespace(source.getProperty(propertyName));
        String targetPropertyValue = StringUtils.trimWhitespace(target.getProperty(propertyName));

        if (!targetPropertyValue.equals(sourcePropertyValue)) {
          System.out.printf("**CONFIGURATION ERROR** Expected the %1$s value (%2$s) of property (%3$s); but %4$s value was (%5$s)%n",
            sourceLabel, sourcePropertyValue, propertyName, targetLabel, targetPropertyValue);
        }
      }
    }
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof Pool) {
      ClientCacheFactoryBean clientCacheFactoryBean = getBeanFactory().getBean(ClientCacheFactoryBean.class);

      Properties existingGemFireProperties = InternalDistributedSystem.getAnyInstance().getConfig().toProperties();

      log("GemFire Pool EXISTING (DistributedSystem) System Properties", existingGemFireProperties);

      Properties startupGemFireProperties = clientCacheFactoryBean.getProperties();

      log("GemFire ClientCache STARTUP (DistributedSystem) System Properties", startupGemFireProperties);

      logDiff("EXISTING", existingGemFireProperties, "STARTUP", startupGemFireProperties);
    }

    return bean;
  }
}
