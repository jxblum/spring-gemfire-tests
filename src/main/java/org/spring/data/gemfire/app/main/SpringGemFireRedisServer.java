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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.CacheFactoryBean;

@ConfigurationProperties
@SpringBootApplication
@SuppressWarnings("unused")
public class SpringGemFireRedisServer {

  public static void main(final String[] args) {
    SpringApplication.run(SpringGemFireRedisServer.class, args);
  }

  @Bean
  Properties gemfireProperties(@Value("${spring.gemfire.log.level:config}") String logLevel,
                               @Value("${spring.gemfire.redis.bind-address:localhost}") String redisBindAddress,
                               @Value("${spring.gemfire.redis.port:1234}") String redisPort)
  {
    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", SpringGemFireRedisServer.class.getSimpleName());
    gemfireProperties.setProperty("mcast-port", "0");
    gemfireProperties.setProperty("log-level", logLevel);
    gemfireProperties.setProperty("redis-bind-address", redisBindAddress);
    gemfireProperties.setProperty("redis-port", redisPort);

    return gemfireProperties;
  }

  @Bean
  CacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) {
    CacheFactoryBean gemfireCache = new CacheFactoryBean();

    gemfireCache.setClose(true);
    gemfireCache.setLazyInitialize(true);
    gemfireCache.setProperties(gemfireProperties);
    gemfireCache.setUseBeanFactoryLocator(false);

    return gemfireCache;
  }

}
