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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.InterestResultPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.gemstone.gemfire.distributed.ServerLauncher;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;

import org.codeprimate.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spring.data.gemfire.AbstractGemFireIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;

/**
 * The DurableClientCacheIntegrationTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.cache.client.ClientCache
 * @see com.gemstone.gemfire.distributed.ServerLauncher
 * @since 1.0.0
 */
public class DurableClientCacheIntegrationTest extends AbstractGemFireIntegrationTest {

  private static final AtomicInteger RUN_COUNT = new AtomicInteger(1);

  private static final int SERVER_PORT = 12480;

  private static final String SERVER_HOST = "localhost";

  private static ServerLauncher serverLauncher;

  private ClientCache clientCache;

  private List<Integer> regionCacheListenerEventValues = Collections.synchronizedList(new ArrayList<Integer>(5));

  private Region<String, Integer> example;

  @BeforeClass
  public static void setupGemFireServer() throws IOException {
    String cacheXmlPathname = "register-interests-server-cache.xml";

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty(DistributionConfig.NAME_NAME, DurableClientCacheIntegrationTest.class
      .getSimpleName().concat("Server"));
    gemfireProperties.setProperty(DistributionConfig.HTTP_SERVICE_PORT_NAME, "0");
    gemfireProperties.setProperty(DistributionConfig.JMX_MANAGER_NAME, "false");
    gemfireProperties.setProperty(DistributionConfig.LOG_LEVEL_NAME, "config");
    gemfireProperties.setProperty(DistributionConfig.MCAST_PORT_NAME, "0");
    gemfireProperties.setProperty(DistributionConfig.USE_CLUSTER_CONFIGURATION_NAME, "false");

    serverLauncher = startGemFireServer(TimeUnit.SECONDS.toMillis(30), cacheXmlPathname, gemfireProperties);
  }

  @AfterClass
  public static void tearDownGemFireServer() {
    stopGemFireServer(serverLauncher);
    serverLauncher = null;
  }

  @Before
  public void setup() {
    clientCache = registerInterests(createClientCache(DurableClientCacheIntegrationTest.class
      .getSimpleName().concat("Client")));

    //assertThat(clientCache.getDefaultPool().getPendingEventCount(), is(equalTo((RUN_COUNT.get() == 1 ? -2 : 2))));

    example = clientCache.getRegion(toRegionPath("Example"));

    assertRegion(example, "Example", DataPolicy.NORMAL);
  }

  @After
  public void tearDown() {
    close(clientCache, true);
    regionCacheListenerEventValues.clear();
    clientCache = null;
    example = null;

    if (RUN_COUNT.get() == 1) {
      runClientCacheProducer();
      RUN_COUNT.incrementAndGet();
    }
  }

  protected ClientCache createClientCache(final String durableClientId) {
    ClientCacheFactory clientCacheFactory = new ClientCacheFactory()
      .addPoolServer(SERVER_HOST, SERVER_PORT)
      .setPoolSubscriptionEnabled(true)
      .set("log-level", "config")
      .set("mcast-port", "0");

    if (StringUtils.hasText(durableClientId)) {
      clientCacheFactory.set("durable-client-id", durableClientId);
      //clientCacheFactory.set("durable-client-timeout", "300");
    }

    ClientCache clientCache = clientCacheFactory.create();

    assertThat(clientCache, is(notNullValue()));

    ClientRegionFactory<String, Integer> clientRegionFactory = clientCache.createClientRegionFactory(
      ClientRegionShortcut.CACHING_PROXY);

    clientRegionFactory.setKeyConstraint(String.class);
    clientRegionFactory.setValueConstraint(Integer.class);

    Region<String, Integer> example = clientRegionFactory.create("Example");

    assertRegion(example, "Example", DataPolicy.NORMAL);

    return clientCache;
  }

  protected void close(final ClientCache clientCache, final boolean keepAlive) {
    if (clientCache != null) {
      clientCache.close(keepAlive);
    }

    assertThat(clientCache == null || clientCache.isClosed(), is(true));
  }

  protected ClientCache registerInterests(final ClientCache clientCache) {
    Region<String, Integer> example = clientCache.getRegion(toRegionPath("Example"));

    assertRegion(example, "Example", DataPolicy.NORMAL);

    example.getAttributesMutator().addCacheListener(new CacheListenerAdapter<String, Integer>() {
      @Override public void afterCreate(final EntryEvent<String, Integer> event) {
        regionCacheListenerEventValues.add(event.getNewValue());
      }

      @Override public void afterUpdate(final EntryEvent<String, Integer> event) {
        regionCacheListenerEventValues.add(event.getNewValue());
      }
    });

    example.registerInterestRegex(".*", InterestResultPolicy.KEYS_VALUES, true);

    clientCache.readyForEvents();

    return clientCache;
  }

  protected void runClientCacheProducer() {
    ClientCache localClientCache = null;

    try {
      localClientCache = createClientCache(null);

      Region<String, Integer> example = localClientCache.getRegion(toRegionPath("Example"));

      assertRegion(example, "Example");

      example.put("four", 4);
      example.put("five", 5);
    }
    finally {
      close(localClientCache, false);
    }
  }

  protected void assertRegionContents(Region<?, ?> region, Object... values) {
    assertThat(region.size(), is(equalTo(values.length)));

    for (Object value : values) {
      assertThat(region.containsValue(value), is(true));
    }
  }

  @Test
  @DirtiesContext
  public void durableClientGetsInitializedWithDataOnServer() {
    assumeThat(RUN_COUNT.get(), is(equalTo(1)));
    assertRegionContents(example, 1, 2, 3);
    assertThat(regionCacheListenerEventValues.isEmpty(), is(true));
  }

  @Test
  public void durableClientGetsUpdatesFromServerWhileClientWasOffline() {
    assumeThat(RUN_COUNT.get(), is(equalTo(2)));
    assertRegionContents(example, 1, 2, 3, 4, 5);
    assertThat(regionCacheListenerEventValues.size(), is(equalTo(2)));
    assertThat(regionCacheListenerEventValues, is(equalTo(Arrays.asList(4, 5))));
  }

  public static class RegionDataLoadingInitializer implements Declarable {

    @Override
    public void init(final Properties props) {
      Cache gemfireCache = CacheFactory.getAnyInstance();

      assertThat(gemfireCache, is(notNullValue()));

      Region<String, Integer> example = gemfireCache.getRegion(toRegionPath("Example"));

      assertRegion(example, "Example", DataPolicy.REPLICATE);

      example.put("one", 1);
      example.put("two", 2);
      example.put("three", 3);
    }
  }

}
