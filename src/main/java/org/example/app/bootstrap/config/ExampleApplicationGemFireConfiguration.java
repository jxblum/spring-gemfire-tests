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

package org.example.app.bootstrap.config;

import java.util.Properties;

import com.gemstone.gemfire.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;

/**
 * The ExampleApplicationGemFireConfiguration class is a Spring @Configuration class
 * used by Spring's AnnotationConfigApplicationContext and classpath scanning functionality to locate
 * the GemFire specific XML configuration file in order to fully configure and initialize GemFire.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportResource
 * @see com.gemstone.gemfire.cache.Cache
 * @since 1.0.0
 */
@Configuration
@ImportResource("org/example/app/bootstrap/config/spring-gemfire-context.xml")
@SuppressWarnings("unused")
public class ExampleApplicationGemFireConfiguration {

  private static final String REGION_NAME = "ExampleSpringJavaConfigDefinedRegion";

  //@Bean
  public Properties gemfireProperties() {
    Properties gemfireProperties = new Properties();
    gemfireProperties.setProperty("name", "ExampleApplicationSpringGemFirePeerCache");
    gemfireProperties.setProperty("mcast-port", "0");
    gemfireProperties.setProperty("log-level", "config");
    return gemfireProperties;
  }

  //@Bean
  //@Autowired
  public CacheFactoryBean gemfireCache(Properties gemfireProperties) {
    CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();
    cacheFactoryBean.setProperties(gemfireProperties);
    return cacheFactoryBean;
  }

  @Bean
  @Autowired
  public PartitionedRegionFactoryBean<Object, Object> examplePartitionRegion(Cache gemfireCache) {
    PartitionedRegionFactoryBean<Object, Object> partitionedRegionFactoryBean = new PartitionedRegionFactoryBean<>();
    partitionedRegionFactoryBean.setCache(gemfireCache);
    partitionedRegionFactoryBean.setBeanName(REGION_NAME);
    partitionedRegionFactoryBean.setName(REGION_NAME);
    partitionedRegionFactoryBean.setPersistent(false);
    partitionedRegionFactoryBean.setRegionName(REGION_NAME);
    return partitionedRegionFactoryBean;
  }

}
