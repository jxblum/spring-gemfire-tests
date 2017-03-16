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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.InterestPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.cache.SubscriptionAttributes;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.distributed.ServerLauncher;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.codeprimate.io.FileSystemUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spring.data.gemfire.AbstractGemFireIntegrationTest;

/**
 * The ClientCacheRegionSubscriptionTest class is a test suite of test cases testing the functionality of GemFire's
 * Subscription configuration on a client Region.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.spring.data.gemfire.AbstractGemFireIntegrationTest
 * @see org.apache.geode.cache.InterestPolicy
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.RegionAttributes
 * @see org.apache.geode.cache.SubscriptionAttributes
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.distributed.ServerLauncher
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ClientCacheRegionSubscriptionTest extends AbstractGemFireIntegrationTest {

  private static ServerLauncher serverLauncher;

  private ClientCache clientCache;

  @BeforeClass
  public static void setupGemFireServer() throws IOException {
    String cacheXmlPathname = "gemfire-server-cache.xml";

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty(DistributionConfig.NAME_NAME, ClientCacheRegionSubscriptionTest.class.getSimpleName());
    gemfireProperties.setProperty(DistributionConfig.ENABLE_CLUSTER_CONFIGURATION_NAME, "false");
    gemfireProperties.setProperty(DistributionConfig.HTTP_SERVICE_PORT_NAME, "0");
    gemfireProperties.setProperty(DistributionConfig.JMX_MANAGER_NAME, "false");
    gemfireProperties.setProperty(DistributionConfig.JMX_MANAGER_PORT_NAME, "1199");
    gemfireProperties.setProperty(DistributionConfig.JMX_MANAGER_START_NAME, "false");
    gemfireProperties.setProperty(DistributionConfig.LOCATORS_NAME, "localhost[11235]");
    gemfireProperties.setProperty(DistributionConfig.LOG_LEVEL_NAME, "config");
    gemfireProperties.setProperty(DistributionConfig.MCAST_PORT_NAME, "0");
    gemfireProperties.setProperty(DistributionConfig.START_LOCATOR_NAME, "localhost[11235]");

    serverLauncher = startGemFireServer(TimeUnit.SECONDS.toMillis(30), cacheXmlPathname, gemfireProperties);
  }

  @AfterClass
  public static void shutdownGemFireServer() {
    stopGemFireServer(serverLauncher);
    deleteServerWorkingDirectory();
    serverLauncher = null;
  }

  private static void deleteServerWorkingDirectory() {
    if (serverLauncher != null) {
      FileSystemUtils.deleteRecursive(FileSystemUtils.createFile(serverLauncher.getWorkingDirectory()));
    }
  }

  @Before
  public void setupGemFireClient() throws Exception {
    clientCache = new ClientCacheFactory()
      .set(DistributionConfig.CACHE_XML_FILE_NAME, "client-region-subscription-cache.xml")
      .create();
  }

  @Test
  public void clientCacheRegionSubscriptionPolicy() {
    assertNotNull("The GemFire Client Cache was not properly configured and initialized!", clientCache);

    Region<?, ?> example = clientCache.getRegion("/Example");

    assertRegion(example, "Example");

    RegionAttributes<?, ?> exampleAttributes = example.getAttributes();

    assertNotNull(exampleAttributes);
    assertEquals(DataPolicy.NORMAL, exampleAttributes.getDataPolicy());
    assertEquals("serverPool", exampleAttributes.getPoolName());
    //assertEquals(Scope.LOCAL, exampleAttributes.getScope());

    SubscriptionAttributes subscriptionAttributes = exampleAttributes.getSubscriptionAttributes();

    assertNotNull(subscriptionAttributes);
    assertEquals(InterestPolicy.CACHE_CONTENT, subscriptionAttributes.getInterestPolicy());
  }

}
