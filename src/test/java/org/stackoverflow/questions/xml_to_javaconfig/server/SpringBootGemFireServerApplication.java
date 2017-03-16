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

package org.stackoverflow.questions.xml_to_javaconfig.server;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.LoaderHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.server.CacheServerFactoryBean;

/**
 * The SpringBootGemFireServerApplication class is a Spring Boot application that configures and bootstraps
 * a GemFire Server.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.apache.geode.cache.Cache
 * @link http://stackoverflow.com/questions/38431885/java-config-for-spring-gemfire-xml
 * @since 1.0.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class SpringBootGemFireServerApplication {

  protected static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";

  public static void main(String[] args) {
    SpringApplication.run(SpringBootGemFireServerApplication.class, args);
  }

  int intValue(Long value) {
    return value.intValue();
  }

  String applicationName() {
    return SpringBootGemFireServerApplication.class.getSimpleName();
  }

  String logLevel() {
    return System.getProperty("gemfire.log-level", DEFAULT_GEMFIRE_LOG_LEVEL);
  }

  @Bean
  Properties gemfireProperties(@Value("${gemfire.locator.host-port:localhost[10334]}") String locatorHostPort,
      @Value("${gemfire.manager.port:1099}") String managerPort) {

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", applicationName());
    gemfireProperties.setProperty("mcast-port", "0");
    gemfireProperties.setProperty("log-level", logLevel());
    gemfireProperties.setProperty("jmx-manager", "true");
    gemfireProperties.setProperty("jmx-manager-port", managerPort);
    gemfireProperties.setProperty("start-locator", locatorHostPort);

    return gemfireProperties;
  }

  @Bean
  CacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) {
    CacheFactoryBean gemfireCache = new CacheFactoryBean();

    gemfireCache.setClose(true);
    gemfireCache.setProperties(gemfireProperties);

    return gemfireCache;
  }

  @Bean
  CacheServerFactoryBean gemfireCacheServer(Cache gemfireCache,
      @Value("${gemfire.cache.server.bind-address:localhost}") String bindAddress,
      @Value("${gemfire.cache.server.hostname-for-clients:localhost}") String hostnameForClients,
      @Value("${gemfire.cache.server.port:40404}") int port) {

    CacheServerFactoryBean gemfireCacheServer = new CacheServerFactoryBean();

    gemfireCacheServer.setCache(gemfireCache);
    gemfireCacheServer.setAutoStartup(true);
    gemfireCacheServer.setBindAddress(bindAddress);
    gemfireCacheServer.setHostNameForClients(hostnameForClients);
    gemfireCacheServer.setMaxConnections(50);
    gemfireCacheServer.setMaxTimeBetweenPings(intValue(TimeUnit.SECONDS.toMillis(60)));
    gemfireCacheServer.setPort(port);

    return gemfireCacheServer;
  }

  @Bean(name = "Echo")
  PartitionedRegionFactoryBean<Object, Object> echoRegion(Cache gemfireCache) {
    PartitionedRegionFactoryBean<Object, Object> echoRegion = new PartitionedRegionFactoryBean<>();

    echoRegion.setCache(gemfireCache);
    echoRegion.setCacheLoader(echoCacheLoader());
    echoRegion.setClose(false);
    echoRegion.setPersistent(false);

    return echoRegion;
  }

  CacheLoader<Object, Object> echoCacheLoader() {
    return new CacheLoader<Object, Object>() {

      @Override
      public Object load(LoaderHelper<Object, Object> helper) throws CacheLoaderException {
        return helper.getKey();
      }

      @Override
      public void close() {
      }
    };
  }
}
