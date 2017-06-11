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

package org.spring.data.gemfire.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.LoaderHelper;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolFactory;
import org.apache.geode.cache.client.PoolManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.ReplicatedRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.client.Interest;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.client.RegexInterest;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.data.gemfire.util.PropertiesBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

/**
 * Integration tests with GemFire's {@link ClientCache} Register Interests using both the GemFire API
 * and Spring Data GemFire configuration to demonstrate the key differences.
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
//@ActiveProfiles("gemfire-api")
@ActiveProfiles("spring-data-gemfire")
@SuppressWarnings("unused")
public class ClientCacheRegisterInterestsTests {

  private static final int GEMFIRE_CACHE_SERVER_PORT = 40404;

  private static final String GEMFIRE_LOG_LEVEL = "config";

  @Resource(name = "Factorials")
  @SuppressWarnings("all")
  private Region<Long, Long> factorials;

  @Test
  public void factorialsAreCorrect() {
    assertThat(this.factorials.get(0L)).isEqualTo(1L);
    assertThat(this.factorials.get(2L)).isEqualTo(2L);
    assertThat(this.factorials.get(3L)).isEqualTo(6L);
    assertThat(this.factorials.get(4L)).isEqualTo(24L);
    assertThat(this.factorials.get(5L)).isEqualTo(120L);
  }

  @Configuration
  @Profile("gemfire-api")
  static class GemFireApiCacheClientConfiguration {

    @Bean
    ClientCache gemfireCache() {

      return new ClientCacheFactory()
        .set("name", "GemFireApiCacheClientApp")
        .set("log-level", GEMFIRE_LOG_LEVEL)
        .create();
    }

    @Bean(name = "Factorials")
    Region<Object, Object> factorialsRegion(ClientCache clientCache) {

      ClientRegionFactory<Object, Object> factorialsRegionFactory =
        clientCache.createClientRegionFactory(ClientRegionShortcut.CACHING_PROXY); // Near Cache

      factorialsRegionFactory.setPoolName("factorialsPool");

      Region<Object, Object> factorialsRegion = factorialsRegionFactory.create("Factorials");

      factorialsRegion.registerInterestRegex(".*");

      return factorialsRegion;
    }

    @Bean
    Pool factorialsPool() {

      PoolFactory poolFactory = PoolManager.createFactory();

      poolFactory.setMaxConnections(10);
      poolFactory.setPingInterval(TimeUnit.SECONDS.toMillis(15));
      poolFactory.setReadTimeout(Long.valueOf(TimeUnit.SECONDS.toMillis(5)).intValue());
      poolFactory.setRetryAttempts(1);
      poolFactory.setSubscriptionEnabled(true);
      poolFactory.addServer("localhost", GEMFIRE_CACHE_SERVER_PORT);

      return poolFactory.create("factorialsPool");
    }
  }

  @Configuration
  @Profile("spring-data-gemfire")
  static class SpringDataGemFireCacheClientConfiguration {

    @Bean
    ClientCacheFactoryBean gemfireCache() {

      ClientCacheFactoryBean clientCache = new ClientCacheFactoryBean();

      clientCache.setClose(true);
      clientCache.setProperties(PropertiesBuilder.create()
        .setProperty("name", "SpringDataGemFireCacheClientApp")
        .setProperty("log-level", GEMFIRE_LOG_LEVEL)
        .build());

      return clientCache;
    }

    @Bean(name = "Factorials")
    @SuppressWarnings("unchecked")
    ClientRegionFactoryBean factorialsRegion(GemFireCache gemfireCache) {

      ClientRegionFactoryBean factorialsRegionFactory = new ClientRegionFactoryBean();

      factorialsRegionFactory.setCache(gemfireCache);
      factorialsRegionFactory.setPoolName("factorialsPool");
      factorialsRegionFactory.setInterests(new Interest[] { new RegexInterest(".*") });
      factorialsRegionFactory.setShortcut(ClientRegionShortcut.CACHING_PROXY); // Near Cache

      return factorialsRegionFactory;
    }

    @Bean
    PoolFactoryBean factorialsPool() {

      PoolFactoryBean poolFactory = new PoolFactoryBean();

      poolFactory.setMaxConnections(10);
      poolFactory.setPingInterval(TimeUnit.SECONDS.toMillis(15));
      poolFactory.setReadTimeout(Long.valueOf(TimeUnit.SECONDS.toMillis(5)).intValue());
      poolFactory.setRetryAttempts(1);
      poolFactory.setSubscriptionEnabled(true);
      poolFactory.addServers(new ConnectionEndpoint("localhost", GEMFIRE_CACHE_SERVER_PORT));

      return poolFactory;
    }
  }

  @Profile("gemfire-server")
  @CacheServerApplication(name = "SpringDataGemFireCacheServer", logLevel = GEMFIRE_LOG_LEVEL,
    port = GEMFIRE_CACHE_SERVER_PORT)
  static class SpringDataGemFireCacheServer {

    public static void main(String[] args) {
      SpringApplication.run(SpringDataGemFireCacheServer.class, args);
    }

    @Bean(name = "Factorials")
    ReplicatedRegionFactoryBean<Long, Long> factorialsRegion(GemFireCache gemfireCache) {

      ReplicatedRegionFactoryBean<Long, Long> factorialsRegionFactory =
        new ReplicatedRegionFactoryBean<>();

      factorialsRegionFactory.setCache(gemfireCache);
      factorialsRegionFactory.setCacheLoader(factorialsLoader());
      factorialsRegionFactory.setClose(false);
      factorialsRegionFactory.setPersistent(false);

      return factorialsRegionFactory;
    }

    CacheLoader<Long, Long> factorialsLoader() {

      return new CacheLoader<Long, Long>() {

        @Override
        public Long load(LoaderHelper<Long, Long> helper) throws CacheLoaderException {

          Long key = helper.getKey();

          Assert.isTrue(key >= 0L, String.format("Number [%d] must be greater than equal to 0", key));

          if (key <= 2L) {
            return (key < 2L ? 1L : 2L);
          }

          long result = key;

          while (--key > 1L) {
            result *= key;
          }

          return result;
        }

        @Override
        public void close() {
        }
      };
    }
  }
}
