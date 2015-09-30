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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.internal.concurrent.ConcurrentHashSet;

import org.spring.data.gemfire.cache.execute.OnMembersCreateRegionFunctionExecution;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.gemfire.function.execution.GemfireOnMembersFunctionTemplate;

/**
 * The CacheableAnnotationDynamicRegionCreationBeanPostProcessor class is a Spring BeanPostProcessor processing Spring beans
 * and application components on startup that are annotated with @Cacheable in order to dynamically create
 * GemFire Regions corresponding for the target "cache" identified in @Cacheable.
 *
 * @author John Blum
 * @see org.spring.data.gemfire.cache.execute.OnMembersCreateRegionFunctionExecution
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.data.gemfire.function.execution.GemfireOnMembersFunctionTemplate
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.cache.execute.FunctionService
 * @since 1.7.0
 */
@SuppressWarnings("unused")
public class CacheableAnnotationDynamicRegionCreationBeanPostProcessor implements BeanPostProcessor {

  public static final String CREATE_REGION_FUNCTION_ID = "createRegion";

  protected static final Set<String> IGNORED_PACKAGE_NAMES;

  static {
    Set<String> ignoredPackageNames = new HashSet<>(2);
    ignoredPackageNames.add("com.gemstone.gemfire");
    ignoredPackageNames.add("org.springframework");
    IGNORED_PACKAGE_NAMES = Collections.unmodifiableSet(ignoredPackageNames);
  }

  private volatile Cache gemfireCache;

  private Set<String> storedCacheNames = new ConcurrentHashSet<>();

  //@Autowired
  private OnMembersCreateRegionFunctionExecution createRegionFunctionExecution;

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (isProcessableBean(bean)) {
      processCacheableAnnotation(bean.getClass().getAnnotation(Cacheable.class));

      for (Method method : bean.getClass().getMethods()) {
        processCacheableAnnotation(method.getAnnotation(Cacheable.class));
      }
    }

    return bean;
  }

  protected boolean isProcessableBean(Object bean) {
    for (String packageName : IGNORED_PACKAGE_NAMES) {
      if (bean.getClass().getPackage().getName().startsWith(packageName)) {
        return false;
      }
    }

    return true;
  }

  protected boolean processCacheableAnnotation(Cacheable cacheableAnnotation) {
    boolean result = false;

    if (cacheableAnnotation != null) {
      result = true;

      for (String cacheName : cacheableAnnotation.value()) {
        result &= (isRegionCreationPossible() ? createRegion(cacheName) : store(cacheName));
      }
    }

    return result;
  }

  protected boolean isRegionCreationPossible() {
    return (isCachePresent() && FunctionService.isRegistered(CREATE_REGION_FUNCTION_ID));
  }

  protected boolean isCachePresent() {
    return (gemfireCache != null);
  }

  protected boolean createRegion(String regionName) {
    return createRegion(regionName, DataPolicy.PARTITION);
  }

  protected boolean createRegion(String regionName, DataPolicy dataPolicy) {
    return getCreateRegionFunctionExecution().createRegion(regionName, dataPolicy);
  }

  protected OnMembersCreateRegionFunctionExecution getCreateRegionFunctionExecution() {
    createRegionFunctionExecution = (createRegionFunctionExecution != null ? createRegionFunctionExecution
      : newRegionCreationFunctionExecution());

    return createRegionFunctionExecution;
  }

  protected OnMembersCreateRegionFunctionExecution newRegionCreationFunctionExecution() {
    return new OnMembersCreateRegionFunctionExecution() {

      private GemfireOnMembersFunctionTemplate functionTemplate = new GemfireOnMembersFunctionTemplate();

      @Override
      public boolean createRegion(String regionName, DataPolicy dataPolicy) {
        return Boolean.valueOf(String.valueOf(functionTemplate.execute(CREATE_REGION_FUNCTION_ID, regionName,
          dataPolicy)));
      }
    };
  }

  protected boolean store(String cacheName) {
    return storedCacheNames.add(cacheName);
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    gemfireCache = (bean instanceof Cache ? (Cache) bean : gemfireCache);
    processStoredCacheNames();
    return bean;
  }

  protected boolean processStoredCacheNames() {
    boolean result = isRegionCreationPossible();

    if (result) {
      for (String cacheName : storedCacheNames) {
        if (createRegion(cacheName)) {
          storedCacheNames.remove(cacheName);
          result &= true;
        }
        else {
          result = false;
        }
      }
    }

    return result;
  }

}
