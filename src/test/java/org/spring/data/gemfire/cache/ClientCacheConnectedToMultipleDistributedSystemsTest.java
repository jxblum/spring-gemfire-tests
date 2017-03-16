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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.LoaderHelper;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.Pool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.cache.ClientCacheConnectedToMultipleDistributedSystemsTest.ClientCacheConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.server.CacheServerFactoryBean;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The ClientCacheConnectedToMultipleDistributedSystemsTest class is a test suite of test cases testing the connection
 * of a GemFire {@link ClientCache} to two distinct GemFire Distributed Systems.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.DependsOn
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.data.gemfire.PartitionedRegionFactoryBean
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.client.PoolFactoryBean
 * @see org.springframework.data.gemfire.server.CacheServerFactoryBean
 * @see org.springframework.data.gemfire.support.ConnectionEndpoint
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.CacheLoader
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.Pool
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ClientCacheConfiguration.class)
@SuppressWarnings("all")
public class ClientCacheConnectedToMultipleDistributedSystemsTest {

  protected static final String LOG_LEVEL = "config";

  protected static int intValue(Long value) {
    return value.intValue();
  }

  protected static ConnectionEndpoint newConnectionEndpoint(String host, int port) {
    return new ConnectionEndpoint(host, port);
  }

  @Resource(name = "Echo")
  private Region<Long, Long> echo;

  @Resource(name = "Factorials")
  private Region<Long, Long> factorials;

  @Test
  public void exampleOneRegionValuesAreEchos() {
    assertThat(echo.get(0l), is(equalTo(0l)));
    assertThat(echo.get(1l), is(equalTo(1l)));
    assertThat(echo.get(2l), is(equalTo(2l)));
    assertThat(echo.get(3l), is(equalTo(3l)));
    assertThat(echo.get(4l), is(equalTo(4l)));
    assertThat(echo.get(5l), is(equalTo(5l)));
    assertThat(echo.get(6l), is(equalTo(6l)));
    assertThat(echo.get(7l), is(equalTo(7l)));
    assertThat(echo.get(8l), is(equalTo(8l)));
    assertThat(echo.get(9l), is(equalTo(9l)));
  }

  @Test
  public void exampleTwoRegionValuesAreFactorials() {
    assertThat(factorials.get(0l), is(equalTo(1l)));
    assertThat(factorials.get(1l), is(equalTo(1l)));
    assertThat(factorials.get(2l), is(equalTo(2l)));
    assertThat(factorials.get(3l), is(equalTo(6l)));
    assertThat(factorials.get(4l), is(equalTo(24l)));
    assertThat(factorials.get(5l), is(equalTo(120l)));
    assertThat(factorials.get(6l), is(equalTo(720l)));
    assertThat(factorials.get(7l), is(equalTo(5040l)));
    assertThat(factorials.get(8l), is(equalTo(40320l)));
    assertThat(factorials.get(9l), is(equalTo(362880l)));
  }

  @Configuration
  static class ClientCacheConfiguration {

    @Bean
    static Properties gemfireProperties() {
      Properties gemfireProperties = new Properties();
      gemfireProperties.setProperty("log-level", LOG_LEVEL);
      return gemfireProperties;
    }

    @Bean
    static ClientCacheFactoryBean gemfireCache() {
      ClientCacheFactoryBean gemfireCache = new ClientCacheFactoryBean();

      gemfireCache.setClose(true);
      gemfireCache.setKeepAlive(false);
      gemfireCache.setProperties(gemfireProperties());
      gemfireCache.setReadyForEvents(true);
      gemfireCache.setUseBeanFactoryLocator(false);

      return gemfireCache;
    }

    @Bean
    static PoolFactoryBean gemfirePoolOne() {
      PoolFactoryBean gemfirePool = new PoolFactoryBean();

      gemfirePool.setFreeConnectionTimeout(intValue(TimeUnit.SECONDS.toMillis(5)));
      gemfirePool.setIdleTimeout(TimeUnit.MINUTES.toMillis(2));
      gemfirePool.setKeepAlive(false);
      gemfirePool.setMaxConnections(50);
      gemfirePool.setMinConnections(1);
      gemfirePool.setMultiUserAuthentication(false);
      gemfirePool.setName("clusterOne");
      gemfirePool.setPingInterval(TimeUnit.SECONDS.toMillis(15));
      gemfirePool.setPrSingleHopEnabled(true);
      gemfirePool.setReadTimeout(intValue(TimeUnit.SECONDS.toMillis(30)));
      gemfirePool.setRetryAttempts(1);
      gemfirePool.setSubscriptionEnabled(true);
      gemfirePool.setSubscriptionRedundancy(1);
      gemfirePool.setThreadLocalConnections(false);

      gemfirePool.addServers(newConnectionEndpoint("localhost", 11235));

      return gemfirePool;
    }

    @Bean
    static PoolFactoryBean gemfirePoolTwo() {
      PoolFactoryBean gemfirePool = new PoolFactoryBean();

      gemfirePool.setFreeConnectionTimeout(intValue(TimeUnit.SECONDS.toMillis(5)));
      gemfirePool.setIdleTimeout(TimeUnit.MINUTES.toMillis(2));
      gemfirePool.setKeepAlive(false);
      gemfirePool.setMaxConnections(50);
      gemfirePool.setMinConnections(1);
      gemfirePool.setMultiUserAuthentication(false);
      gemfirePool.setName("clusterTwo");
      gemfirePool.setPingInterval(TimeUnit.SECONDS.toMillis(15));
      gemfirePool.setPrSingleHopEnabled(true);
      gemfirePool.setReadTimeout(intValue(TimeUnit.SECONDS.toMillis(30)));
      gemfirePool.setRetryAttempts(1);
      gemfirePool.setSubscriptionEnabled(true);
      gemfirePool.setSubscriptionRedundancy(1);
      gemfirePool.setThreadLocalConnections(false);

      gemfirePool.addServers(newConnectionEndpoint("localhost", 12480));

      return gemfirePool;
    }

    @Bean(name = "Echo")
    static ClientRegionFactoryBean<Long, Long> echoRegion(GemFireCache gemfireCache,
      @Qualifier("gemfirePoolOne") Pool clusterOne,
      RegionAttributes<Long, Long> regionAttributes)
    {
      ClientRegionFactoryBean<Long, Long> exampleOneRegion = new ClientRegionFactoryBean<>();

      exampleOneRegion.setAttributes(regionAttributes);
      exampleOneRegion.setCache(gemfireCache);
      exampleOneRegion.setClose(false);
      exampleOneRegion.setName("Echo");
      exampleOneRegion.setPool(clusterOne);
      exampleOneRegion.setShortcut(ClientRegionShortcut.PROXY);

      return exampleOneRegion;
    }

    @Bean(name = "Factorials")
    static ClientRegionFactoryBean<Long, Long> factorialsRegion(GemFireCache gemfireCache,
        @Qualifier("gemfirePoolTwo") Pool clusterTwo,
        RegionAttributes<Long, Long> regionAttributes)
    {
      ClientRegionFactoryBean<Long, Long> exampleOneRegion = new ClientRegionFactoryBean<>();

      exampleOneRegion.setAttributes(regionAttributes);
      exampleOneRegion.setCache(gemfireCache);
      exampleOneRegion.setClose(false);
      exampleOneRegion.setName("Factorials");
      exampleOneRegion.setPool(clusterTwo);
      exampleOneRegion.setShortcut(ClientRegionShortcut.PROXY);

      return exampleOneRegion;
    }

    @Bean
    @SuppressWarnings("unchecked")
    static RegionAttributesFactoryBean sharedRegionAttributes() {
      RegionAttributesFactoryBean regionAttributes = new RegionAttributesFactoryBean();

      regionAttributes.setKeyConstraint(Long.class);
      regionAttributes.setValueConstraint(Long.class);

      return regionAttributes;
    }
  }

  @Configuration
  static class GemFireServerApplicationConfiguration {

    @Bean
    PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
      return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    Properties gemfireProperties(@Value("${gemfire.log.level:config}") String logLevel) {
      Properties gemfireProperties = new Properties();

      gemfireProperties.setProperty("name", getClass().getSimpleName());
      gemfireProperties.setProperty("mcast-port", "0");
      gemfireProperties.setProperty("locators", "");
      gemfireProperties.setProperty("log-level", logLevel);

      return gemfireProperties;
    }

    @Bean
    CacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) {
      CacheFactoryBean gemfireCache = new CacheFactoryBean();

      gemfireCache.setClose(true);
      gemfireCache.setProperties(gemfireProperties);
      gemfireCache.setUseBeanFactoryLocator(false);

      return gemfireCache;
    }

    @Bean
    @SuppressWarnings("unchecked")
    RegionAttributesFactoryBean sharedRegionAttributes() {
      RegionAttributesFactoryBean regionAttributes = new RegionAttributesFactoryBean();

      regionAttributes.setKeyConstraint(Long.class);
      regionAttributes.setValueConstraint(Long.class);

      return regionAttributes;
    }
  }

  @Configuration
  @Import(GemFireServerApplicationConfiguration.class)
  static class GemFireServerAppOne {

    public static void main(final String[] args) {
      System.setProperty("gemfire.name", GemFireServerAppOne.class.getSimpleName());
      SpringApplication.run(GemFireServerAppOne.class, args);
    }

    @Bean
    CacheServerFactoryBean gemfireCacheServer(Cache gemfireCache,
        @Value("${gemfire.cache.server.bind-address:localhost}") String bindAddress,
        @Value("${gemfire.cache.server.hostname-for-clients:localhost}") String hostnameForClients,
        @Value("${gemfire.cache.server.port:11235}") int port,
        @Value("${gemfire.cache.server.max-connections:50}") int maxConnections)
    {
      CacheServerFactoryBean gemfireCacheServer = new CacheServerFactoryBean();

      gemfireCacheServer.setAutoStartup(true);
      gemfireCacheServer.setCache(gemfireCache);
      gemfireCacheServer.setBindAddress(bindAddress);
      gemfireCacheServer.setHostNameForClients(hostnameForClients);
      gemfireCacheServer.setMaxConnections(maxConnections);
      gemfireCacheServer.setMaxTimeBetweenPings(intValue(TimeUnit.MINUTES.toMillis(1)));
      gemfireCacheServer.setNotifyBySubscription(true);
      gemfireCacheServer.setPort(port);

      return gemfireCacheServer;
    }

    @Bean(name = "Echo")
    PartitionedRegionFactoryBean<Long, Long> echoRegion(Cache gemfireCache, RegionAttributes<Long, Long> regionAttributes) {
      PartitionedRegionFactoryBean<Long, Long> exampleOneRegion = new PartitionedRegionFactoryBean<>();

      exampleOneRegion.setAttributes(regionAttributes);
      exampleOneRegion.setCache(gemfireCache);
      exampleOneRegion.setCacheLoader(echoCacheLoader());
      exampleOneRegion.setClose(false);
      exampleOneRegion.setName("Echo");
      exampleOneRegion.setPersistent(false);

      return exampleOneRegion;
    }

    @Bean
    CacheLoader<Long, Long> echoCacheLoader() {
      return new CacheLoader<Long, Long>() {
        @Override public Long load(final LoaderHelper<Long, Long> helper) throws CacheLoaderException {
          return helper.getKey();
        }

        @Override public void close() {
        }
      };
    }
  }

  @Configuration
  @Import(GemFireServerApplicationConfiguration.class)
  static class GemFireServerAppTwo {

    public static void main(final String[] args) {
      System.setProperty("gemfire.name", GemFireServerAppTwo.class.getSimpleName());
      SpringApplication.run(GemFireServerAppTwo.class, args);
    }

    @Bean
    CacheServerFactoryBean gemfireCacheServer(Cache gemfireCache,
        @Value("${gemfire.cache.server.bind-address:localhost}") String bindAddress,
        @Value("${gemfire.cache.server.hostname-for-clients:localhost}") String hostnameForClients,
        @Value("${gemfire.cache.server.port:12480}") int port,
        @Value("${gemfire.cache.server.max-connections:50}") int maxConnections)
    {
      CacheServerFactoryBean gemfireCacheServer = new CacheServerFactoryBean();

      gemfireCacheServer.setAutoStartup(true);
      gemfireCacheServer.setCache(gemfireCache);
      gemfireCacheServer.setBindAddress(bindAddress);
      gemfireCacheServer.setHostNameForClients(hostnameForClients);
      gemfireCacheServer.setMaxConnections(maxConnections);
      gemfireCacheServer.setMaxTimeBetweenPings(intValue(TimeUnit.MINUTES.toMillis(1)));
      gemfireCacheServer.setNotifyBySubscription(true);
      gemfireCacheServer.setPort(port);

      return gemfireCacheServer;
    }

    @Bean(name = "Factorials")
    PartitionedRegionFactoryBean<Long, Long> factorialsRegion(Cache gemfireCache, RegionAttributes<Long, Long> regionAttributes) {
      PartitionedRegionFactoryBean<Long, Long> exampleOneRegion = new PartitionedRegionFactoryBean<>();

      exampleOneRegion.setAttributes(regionAttributes);
      exampleOneRegion.setCache(gemfireCache);
      exampleOneRegion.setCacheLoader(factorialsCacheLoader());
      exampleOneRegion.setClose(false);
      exampleOneRegion.setName("Factorials");
      exampleOneRegion.setPersistent(false);

      return exampleOneRegion;
    }

    @Bean
    CacheLoader<Long, Long> factorialsCacheLoader() {
      return new CacheLoader<Long, Long>() {
        @Override public Long load(final LoaderHelper<Long, Long> helper) throws CacheLoaderException {
          Long number = helper.getKey();

          assert number != null : "number must not be null";
          assert number > -1 : String.format("number [%1$d] must be greater than equal to 0", number);

          if (number <= 2l) {
            return (number < 2l ? 1l : 2l);
          }

          long result = number;

          while (--number > 1) {
            result *= number;
          }

          return result;
        }

        @Override public void close() {
        }
      };
    }
  }

}
