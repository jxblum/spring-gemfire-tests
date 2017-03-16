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

import java.util.Collections;
import java.util.Map;

import org.apache.geode.cache.Region;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The RegionPutAllBeanPostProcessor class is a Spring BeanPostProcessor that enables a GemFire Cache Region to be
 * initialized with data (key/values) in Spring XML configuration meta-data.
 *
 * @author John Blum
 * @see java.util.Map
 * @see org.apache.geode.cache.Region
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @since 1.3.3 (Spring Data GemFire)
 * @since 7.0.1 (GemFire)
 */
@SuppressWarnings("unused")
public class RegionPutAllBeanPostProcessor implements BeanPostProcessor {

  private Map regionData;

  private String targetBeanName;

  protected Map getRegionData() {
    return (regionData != null ? regionData : Collections.emptyMap());
  }

  public void setRegionData(final Map regionData) {
    this.regionData = regionData;
  }

  protected String getTargetBeanName() {
    Assert.state(StringUtils.hasText(targetBeanName), "The target Spring context bean name was not properly specified!");
    return targetBeanName;
  }

  public void setTargetBeanName(final String targetBeanName) {
    Assert.hasText(targetBeanName, "The target Spring context bean name must be specified!");
    this.targetBeanName = targetBeanName;
  }

  @Override
  public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
    return bean;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
    if (beanName.equals(getTargetBeanName()) && bean instanceof Region) {
      ((Region) bean).putAll(getRegionData());
    }

    return bean;
  }

}
