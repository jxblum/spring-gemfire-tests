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

import java.io.File;
import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.gemfire.DiskStoreFactoryBean.DiskDir;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The DiskStoreBeanPostProcessor class post processes any GemFire Disk Store Spring beans in the application context
 * to ensure that the Disk Store directory (disk-dir) actually exists before creating the Disk Store.
 *
 * @author John Blum
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @since 7.5
 */
@SuppressWarnings("unused")
public class DiskStoreBeanPostProcessor implements BeanPostProcessor {

  @Override
  public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
    System.out.printf("Post Processing Bean (%1$s) of Type (%2$s) with Name (%3$s)...%n", bean,
      ObjectUtils.nullSafeClassName(bean), beanName);

    if (bean instanceof DiskDir) {
      createIfNotExists((DiskDir) bean);
    }

    return bean;
  }

  private void createIfNotExists(final DiskDir diskDirectory) {
    String location = readField(diskDirectory, "location");
    File diskDirectoryFile = new File(location);

    Assert.isTrue(diskDirectoryFile.isDirectory() || diskDirectoryFile.mkdirs(),
      String.format("Failed to create Disk Directory (%1$s)%n!", location));

    System.out.printf("The Disk Directory exists @ Location (%1$s).%n", location);
  }

  @SuppressWarnings("unchecked")
  private <T> T readField(final Object obj, final String fieldName) {
    try {
      Class type = obj.getClass();
      Field field;

      do {
        field = type.getDeclaredField(fieldName);
        type = type.getSuperclass();
      }
      while (field == null && !Object.class.equals(type));

      if (field == null) {
        throw new NoSuchFieldException(String.format("Field (%1$s) does not exist on Object of Class type (%2$s)!",
          fieldName, ObjectUtils.nullSafeClassName(obj)));
      }

      field.setAccessible(true);

      return (T) field.get(obj);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
    return bean;
  }

}
