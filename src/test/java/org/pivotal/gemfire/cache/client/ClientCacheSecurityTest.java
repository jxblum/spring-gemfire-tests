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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The ClientCacheSecurityTest class is a test suite of test cases testing the interaction (Region.get(key)) between
 * a GemFire Cache client and a GemFire Cache server over a secure connection (SSLSocket).
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ClientCacheSecurityTest {

  protected static final int LOCATOR_PORT = 11235;
  protected static final int SERVER_PORT = 12480;

  protected static final String EXPECTED_VALUE = "TestValue";
  protected static final String KEY = "TestKey";
  protected static final String KEYSTORE_LOCATION = "/Users/jblum/vmdev/spring-data-gemfire-tests-workspace/spring-data-gemfire-tests/etc/gemfire/security/trusted.keystore";
  protected static final String LOCATOR_HOST = "jblum-mbpro.local";
  protected static final String SERVER_HOST = LOCATOR_HOST;

  private ClientCache clientCache;

  private Region<Object, Object> example;

  @Before
  public void setupGemFireClient() throws Exception {
    clientCache = configureGemFireClientCacheWithApi(new ClientCacheFactory()
      .setPoolMaxConnections(1)
      .setPoolMinConnections(1)
      .setPoolMultiuserAuthentication(false)
      .setPoolReadTimeout(Long.valueOf(TimeUnit.SECONDS.toMillis(30)).intValue())
      .setPoolSubscriptionEnabled(false)
      .addPoolLocator(LOCATOR_HOST, LOCATOR_PORT)
      .set("durable-client-id", "TestDurableClientId")
      .set("log-level", "config")
      .set("cluster-ssl-enabled", "true")
      .set("cluster-ssl-require-authentication", "false")
      .set("cluster-ssl-protocols", "any")
      .set("cluster-ssl-ciphers", "any")
      .set("cluster-ssl-keystore", KEYSTORE_LOCATION)
      .set("cluster-ssl-keystore-password", "s3cr3t")
      .set("cluster-ssl-keystore-type", "JKS")
      .set("cluster-ssl-truststore", KEYSTORE_LOCATION)
      .set("cluster-ssl-truststore-password", "s3cr3t")
      .create());
  }

  protected ClientCache configureGemFireClientCacheWithApi(final ClientCache clientCache) throws Exception {
    ClientRegionFactory<Object, Object> clientRegionFactory = clientCache.createClientRegionFactory(
      ClientRegionShortcut.PROXY);

    clientRegionFactory.setPoolName(clientCache.getDefaultPool().getName());

    example = clientRegionFactory.create("Example");

    assertThat("The '/Example' Region was not properly configured and initialized!", example, is(not(nullValue())));
    assertThat(example.getName(), is(equalTo("Example")));
    assertThat(example.getFullPath(), is(equalTo(String.format("%1$s%2$s", Region.SEPARATOR, "Example"))));

    return clientCache;
  }

  @After
  public void shutdownGemFireClient() {
    if (clientCache != null) {
      clientCache.close();
      clientCache = null;
    }
  }

  @Test
  public void exampleRegionTestKeyValue() {
    assertThat(String.valueOf(example.get(KEY)), is(equalTo(EXPECTED_VALUE)));
  }

}
