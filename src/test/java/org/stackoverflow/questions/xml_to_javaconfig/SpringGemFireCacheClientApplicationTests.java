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

package org.stackoverflow.questions.xml_to_javaconfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.Pool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.config.xml.GemfireConstants;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.stackoverflow.questions.xml_to_javaconfig.SpringGemFireCacheClientApplicationTests.GemFireCacheClientApplicationConfiguration;

/**
 * Test suite of test cases testing the configuration of a GemFire cache client application
 * using Spring Java configuration meta-data.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.Pool
 * @link http://stackoverflow.com/questions/38431885/java-config-for-spring-gemfire-xml
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = GemFireCacheClientApplicationConfiguration.class)
public class SpringGemFireCacheClientApplicationTests {

  protected static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";

  @Resource(name = "Echo")
  @SuppressWarnings("all")
  private Region<Object, Object> echo;

  @Test
  public void echoRegionGetsEchoTheKeyAsTheValue() {
    assertThat(echo.get("test")).isEqualTo("test");
    assertThat(echo.get(1)).isEqualTo(1);
  }

  @Configuration
  @SuppressWarnings("unused")
  static class GemFireCacheClientApplicationConfiguration {

    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
      return new PropertySourcesPlaceholderConfigurer();
    }

    static int intValue(Number value) {
      return value.intValue();
    }

    Properties gemfireProperties() {
      Properties gemfireProperties = new Properties();
      gemfireProperties.setProperty("log-level", logLevel());
      return gemfireProperties;
    }

    String logLevel() {
      return System.getProperty("gemfire.log-level", DEFAULT_GEMFIRE_LOG_LEVEL);
    }

    @Bean
    ClientCacheFactoryBean gemfireCache() {
      ClientCacheFactoryBean gemfireCache = new ClientCacheFactoryBean();

      gemfireCache.setClose(true);
      gemfireCache.setProperties(gemfireProperties());

      return gemfireCache;
    }

    @Bean(name = GemfireConstants.DEFAULT_GEMFIRE_POOL_NAME)
    PoolFactoryBean gemfirePool(@Value("${gemfire.locator.host:localhost}") String host,
        @Value("${gemfire.locator.port:10334}") int port) {

      PoolFactoryBean gemfirePool = new PoolFactoryBean();

      gemfirePool.setKeepAlive(false);
      gemfirePool.setMinConnections(1);
      gemfirePool.setPingInterval(TimeUnit.SECONDS.toMillis(15));
      gemfirePool.setReadTimeout(intValue(TimeUnit.SECONDS.toMillis(20)));
      gemfirePool.setRetryAttempts(1);
      gemfirePool.setSubscriptionEnabled(true);
      gemfirePool.setThreadLocalConnections(false);

      gemfirePool.addLocators(new ConnectionEndpoint(host, port));

      return gemfirePool;
    }

    @Bean(name = "Echo")
    ClientRegionFactoryBean echoRegion(ClientCache gemfireCache, Pool gemfirePool) {
      ClientRegionFactoryBean echoRegion = new ClientRegionFactoryBean();

      echoRegion.setCache(gemfireCache);
      echoRegion.setPool(gemfirePool);
      echoRegion.setShortcut(ClientRegionShortcut.PROXY);

      return echoRegion;
    }
  }
}
