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

package org.spring.data.gemfire.app.main.server;

import java.util.Properties;

import org.apache.geode.cache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.server.CacheServerFactoryBean;

/**
 * The SpringGemFireServer class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class SpringGemFireServer {

  public static void main(String[] args) {
    SpringApplication.run(SpringGemFireServer.class, args);
  }

  @Bean
  PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  Properties gemfireProperties(@Value("${gemfire.log.level:config}") String logLevel,
      @Value("${gemfire.locator.host-port:localhost[10334]}") String locatorHostPort,
      @Value("${gemfire.manager.port:1099}") String managerPort) {

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", SpringGemFireServer.class.getSimpleName());
    gemfireProperties.setProperty("mcast-port", "0");
    gemfireProperties.setProperty("log-level", logLevel);
    gemfireProperties.setProperty("jmx-manager", "true");
    gemfireProperties.setProperty("jmx-manager-port", managerPort);
    gemfireProperties.setProperty("jmx-manager-start", "true");
    gemfireProperties.setProperty("start-locator", locatorHostPort);

    return gemfireProperties;
  }

  @Bean
  CacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) {
    CacheFactoryBean gemfireCache = new CacheFactoryBean();

    gemfireCache.setClose(true);
    gemfireCache.setPdxIgnoreUnreadFields(true);
    gemfireCache.setPdxReadSerialized(true);
    gemfireCache.setProperties(gemfireProperties);

    return gemfireCache;
  }

  @Bean
  CacheServerFactoryBean gemfireCacheServer(Cache gemfireCache,
      @Value("${gemfire.cache.server.bind-address:localhost}") String bindAddress,
      @Value("${gemfire.cache.server.hostname-for-clients:localhost}") String hostnameForClients,
      @Value("${gemfire.cache.server.port:40404}") int port,
      @Value("${gemfire.cache.server.max-connections:50}") int maxConnections) {

    CacheServerFactoryBean gemfireCacheServer = new CacheServerFactoryBean();

    gemfireCacheServer.setCache(gemfireCache);
    gemfireCacheServer.setAutoStartup(true);
    gemfireCacheServer.setBindAddress(bindAddress);
    gemfireCacheServer.setHostNameForClients(hostnameForClients);
    gemfireCacheServer.setPort(port);
    gemfireCacheServer.setMaxConnections(maxConnections);

    return gemfireCacheServer;
  }

  @Bean(name = "Example")
  PartitionedRegionFactoryBean<String, Object> exampleRegion(Cache gemfireCache) {
    PartitionedRegionFactoryBean<String, Object> exampleRegion = new PartitionedRegionFactoryBean<>();

    exampleRegion.setCache(gemfireCache);
    exampleRegion.setClose(false);
    exampleRegion.setPersistent(false);

    return exampleRegion;
  }
}
