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

package org.spring.cache;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.cache.LocalClientCachingWithGemFireIntegrationTest.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.data.gemfire.cache.GemfireCacheManager;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.config.xml.GemfireConstants;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ObjectUtils;

/**
 * The LocalClientCachingWithGemFireIntegrationTest class is a test suite of test cases testing the
 * use of Spring's Cache Abstraction and a GemFire local ClientCache as a provider.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.cache.CacheManager
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.client.PoolFactoryBean
 * @see org.springframework.data.gemfire.support.GemfireCacheManager
 * @see org.springframework.stereotype.Service
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfiguration.class)
@SuppressWarnings("all")
public class LocalClientCachingWithGemFireIntegrationTest {

  @Autowired
  @Qualifier("randomNumberGenerator")
  private CacheableRandomNumberGeneratorService randomNumberGeneratorService;

  @Resource(name = "Example")
  private Region<String, Long> example;

  @Before
  public void setup() {
    example.clear();
  }

  @Test
  public void randomNumberGeneratorServiceUsesCacheProperly() {
    long expectedNumber = randomNumberGeneratorService.generate("one");

    assertThat(randomNumberGeneratorService.wasCacheMiss(), is(true));

    long actualNumber = randomNumberGeneratorService.generate("one");

    assertThat(randomNumberGeneratorService.wasCacheMiss(), is(false));
    assertThat(actualNumber, is(equalTo(expectedNumber)));

    expectedNumber = randomNumberGeneratorService.generate("two");

    assertThat(randomNumberGeneratorService.wasCacheMiss(), is(true));
    assertThat(expectedNumber, is(not(equalTo(actualNumber))));

    actualNumber = randomNumberGeneratorService.generate("two");

    assertThat(randomNumberGeneratorService.wasCacheMiss(), is(false));
    assertThat(actualNumber, is(equalTo(expectedNumber)));
  }

  @Service
  public static class CacheableRandomNumberGeneratorService {

    private volatile boolean cacheMiss = false;

    private Random randomNumberGenerator = new Random(System.currentTimeMillis());

    @Cacheable("Example")
    public long generate(String key) {
      cacheMiss = true;
      return randomNumberGenerator.nextLong();
    }

    public boolean wasCacheMiss() {
      boolean localCacheMiss = this.cacheMiss;
      this.cacheMiss = false;
      return localCacheMiss;
    }
  }

  @Configuration
  @EnableCaching
  @Import(GemFireClientCacheConfiguration.class)
  public static class ApplicationConfiguration {

    @Bean
    public CacheManager cacheManager(GemFireCache gemfireCache) {
      GemfireCacheManager cacheManager = new GemfireCacheManager();
      // NOTE GemFire's sole GemFireCacheImpl class implements Cache, ClientCache and GemFireCache...
      cacheManager.setCache((Cache) gemfireCache);
      return cacheManager;
    }

    @Bean
    public CacheableRandomNumberGeneratorService randomNumberGenerator() {
      return new CacheableRandomNumberGeneratorService();
    }
  }

  @Configuration
  public static class GemFireClientCacheConfiguration {

    @Bean
    public Properties gemfireSettings() {
      Properties gemfireSettings = new Properties();

      gemfireSettings.setProperty("app.gemfire.default.log-level", "config");
      gemfireSettings.setProperty("app.gemfire.default.pool.free-connection-timeout",
        String.valueOf(TimeUnit.SECONDS.toMillis(30)));
      gemfireSettings.setProperty("app.gemfire.default.pool.hostname", "localhost");
      gemfireSettings.setProperty("app.gemfire.default.pool.idle-timeout",
        String.valueOf(TimeUnit.MINUTES.toMillis(2)));
      gemfireSettings.setProperty("app.gemfire.default.pool.min-connections", "1");
      gemfireSettings.setProperty("app.gemfire.default.pool.port", "11235");
      gemfireSettings.setProperty("app.gemfire.default.region.initial-capacity", "101");
      gemfireSettings.setProperty("app.gemfire.default.region.load-factor", "0.75");

      return gemfireSettings;
    }

    @Bean
    public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer(
        @Qualifier("gemfireSettings") Properties placeholders) {

      PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();

      propertyPlaceholderConfigurer.setProperties(placeholders);

      return propertyPlaceholderConfigurer;
    }

    @Bean
    public Properties gemfireProperties(@Value("${app.gemfire.default.log-level}") String logLevel) {
      Properties gemfireProperties = new Properties();

      gemfireProperties.setProperty("name", "SpringGemFireCacheClient");
      gemfireProperties.setProperty("log-level", logLevel);

      return gemfireProperties;
    }

    protected Collection<InetSocketAddress> asCollection(InetSocketAddress... socketAddresses) {
      return (!ObjectUtils.isEmpty(socketAddresses) ? Arrays.asList(socketAddresses)
        : Collections.<InetSocketAddress>emptyList());
    }

    @Bean(name = GemfireConstants.DEFAULT_GEMFIRE_POOL_NAME)
    public PoolFactoryBean gemfirePool(@Value("${app.gemfire.default.pool.hostname}") String hostname,
        @Value("${app.gemfire.default.pool.port}") int port,
        @Value("${app.gemfire.default.pool.min-connections}") int minConnections,
        @Value("${app.gemfire.default.pool.free-connection-timeout}") int freeConnectionTimeout,
        @Value("${app.gemfire.default.pool.idle-timeout}") long idleTimeout) {

      PoolFactoryBean poolFactoryBean = new PoolFactoryBean();

      poolFactoryBean.addLocators(new ConnectionEndpoint(hostname, port));
      poolFactoryBean.setFreeConnectionTimeout(freeConnectionTimeout);
      poolFactoryBean.setIdleTimeout(idleTimeout);
      poolFactoryBean.setMinConnections(minConnections);

      return poolFactoryBean;
    }

    @Bean
    public ClientCacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) {
      ClientCacheFactoryBean clientCacheFactoryBean = new ClientCacheFactoryBean();

      clientCacheFactoryBean.setProperties(gemfireProperties);

      return clientCacheFactoryBean;
    }

    @Bean(name = "Example")
    @DependsOn("gemfireCache")
    // NOTE The @DependsOn annotation is not needed if the "Example" Region bean is not injected into another
    // application component, such as my LocalClientCachingWithGemFireIntegrationTest class.
    public <K, V> ClientRegionFactoryBean<K, V> example(ClientCache gemfireCache, RegionAttributes<K, V> regionAttributes) {
      ClientRegionFactoryBean<K, V> clientRegionFactoryBean = new ClientRegionFactoryBean<>();

      clientRegionFactoryBean.setAttributes(regionAttributes);
      clientRegionFactoryBean.setCache(gemfireCache);
      clientRegionFactoryBean.setName("Example");
      clientRegionFactoryBean.setShortcut(ClientRegionShortcut.PROXY);

      return clientRegionFactoryBean;
    }

    @Bean
    @SuppressWarnings("unchecked")
    public RegionAttributesFactoryBean exampleRegionAttributes(
        @Value("${app.gemfire.default.region.initial-capacity}") int initialCapacity,
        @Value("${app.gemfire.default.region.load-factor}") float loadFactor) {

      RegionAttributesFactoryBean regionAttributesFactoryBean = new RegionAttributesFactoryBean();

      regionAttributesFactoryBean.setInitialCapacity(initialCapacity);
      regionAttributesFactoryBean.setLoadFactor(loadFactor);
      regionAttributesFactoryBean.setKeyConstraint(String.class);
      regionAttributesFactoryBean.setValueConstraint(Long.class);

      return regionAttributesFactoryBean;
    }
  }
}
