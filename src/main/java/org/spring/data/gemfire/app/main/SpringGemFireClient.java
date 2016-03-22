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

package org.spring.data.gemfire.app.main;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.support.ConnectionEndpoint;

/**
 * The SpringGemFireClient class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class SpringGemFireClient {

  public static void main(final String[] args) {
    SpringApplication.run(SpringGemFireClient.class, args);
  }

  int intValue(Long value) {
    return value.intValue();
  }

  @Bean
  PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  Properties gemfireProperties(@Value("${gemfire.log.level:config}") String logLevel) {
    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", SpringGemFireClient.class.getSimpleName());
    gemfireProperties.setProperty("log-level", logLevel);

    return gemfireProperties;
  }

  @Bean
  ClientCacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) {
    ClientCacheFactoryBean gemfireCache = new ClientCacheFactoryBean();

    gemfireCache.setClose(true);
    gemfireCache.setKeepAlive(false);
    gemfireCache.setPoolName("gemfirePool");
    gemfireCache.setProperties(gemfireProperties);
    gemfireCache.setReadyForEvents(true);
    gemfireCache.setUseBeanFactoryLocator(false);

    return gemfireCache;
  }

  @Bean
  PoolFactoryBean gemfirePool(@Value("${gemfire.client.server.host:localhost}") String serverHost,
    @Value("${gemfire.client.server.port:40404}") int serverPort,
    @Value("${gemfire.client.server.max-connections:50}") int maxConnections)
  {
    PoolFactoryBean gemfirePool = new PoolFactoryBean();

    gemfirePool.setFreeConnectionTimeout(intValue(TimeUnit.SECONDS.toMillis(30)));
    gemfirePool.setIdleTimeout(TimeUnit.MINUTES.toMillis(2));
    gemfirePool.setKeepAlive(false);
    gemfirePool.setMaxConnections(maxConnections);
    gemfirePool.setPingInterval(TimeUnit.SECONDS.toMillis(15));
    gemfirePool.setReadTimeout(intValue(TimeUnit.SECONDS.toMillis(20)));
    gemfirePool.setRetryAttempts(1);
    gemfirePool.setPrSingleHopEnabled(true);
    gemfirePool.setThreadLocalConnections(false);

    gemfirePool.addServers(new ConnectionEndpoint(serverHost, serverPort));

    return gemfirePool;
  }

}
