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

package org.pivotal.gemfire.cache.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.gemfire.util.CacheUtils;

/**
 * Integration tests testing the {@link DataPolicy} and {@link Scope} of a {@link ClientCache}
 * {@link ClientRegionShortcut#LOCAL} {@link Region}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @since 1.0.0
 */
public class ClientCacheWithLocalRegionDataPolicyScopeTests {

  protected static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";

  private ClientRegionShortcut clientRegionShortcut = ClientRegionShortcut.LOCAL;

  private Region<?, ?> example;

  @Before
  public void setup() {
    example = newRegion(gemfireCache(gemfireProperties()), "Example", clientRegionShortcut);
  }

  private Properties gemfireProperties() {
    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", applicationName());
    gemfireProperties.setProperty("log-level", logLevel());

    return gemfireProperties;
  }

  private String applicationName() {
    return ClientCacheWithLocalRegionDataPolicyScopeTests.class.getName();
  }

  private String logLevel() {
    return System.getProperty("gemfire.log.level", DEFAULT_GEMFIRE_LOG_LEVEL);
  }

  private ClientCache gemfireCache(Properties gemfireProperties) {
    return new ClientCacheFactory(gemfireProperties).create();
  }

  private <K, V> Region<K, V> newRegion(ClientCache gemfireCache, String name,
      ClientRegionShortcut clientRegionShortcut) {

    return gemfireCache.<K, V>createClientRegionFactory(clientRegionShortcut).create(name);
  }

  @After
  public void tearDown() {
    CacheUtils.closeClientCache();
  }

  @Test
  public void scopeOfClientCacheLocalRegionIsDataPolicyNormalAndScopeLocal() {
    assertThat(example).isNotNull();
    assertThat(example.getAttributes()).isNotNull();
    assertThat(example.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.NORMAL);
    assertThat(example.getAttributes().getScope()).isEqualTo(Scope.LOCAL);
  }
}
