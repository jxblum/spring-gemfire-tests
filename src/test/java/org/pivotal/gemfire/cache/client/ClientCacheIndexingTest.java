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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolFactory;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.query.Index;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.distributed.ServerLauncher;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;

import org.codeprimate.io.FileSystemUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spring.data.gemfire.AbstractGemFireIntegrationTest;

/**
 * The ClientCacheIndexingTest class is a test suite of test cases testing the creation and application of indexes
 * on client Regions of a GemFire ClientCache using the GemFire API exclusively (i.e. no Spring Data GemFire).
 *
 * @author John Blum
 * @see com.gemstone.gemfire.cache.GemFireCache
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.cache.client.ClientCache
 * @see com.gemstone.gemfire.cache.client.Pool
 * @see com.gemstone.gemfire.cache.query.Index
 * @see com.gemstone.gemfire.cache.query.QueryService
 * @see com.gemstone.gemfire.distributed.ServerLauncher
 * @see org.spring.data.gemfire.AbstractGemFireIntegrationTest
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ClientCacheIndexingTest extends AbstractGemFireIntegrationTest {

  private static final String INDEX_NAME = "TestIndex";

  private static ServerLauncher serverLauncher;

  private ClientCache clientCache;

  @BeforeClass
  public static void setupGemFireServer() throws IOException {
    String cacheXmlPathname = "gemfire-server-cache.xml";

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty(DistributionConfig.NAME_NAME, ClientCacheIndexingTest.class.getSimpleName());
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
    clientCache = configureGemFireClientCacheWithApi(new ClientCacheFactory()
      .set("durable-client-id", "TestDurableClientId")
        //.set(DistributionConfig.CACHE_XML_FILE_NAME, "client-region-index-cache.xml")
      .create());
  }

  protected ClientCache configureGemFireClientCacheWithApi(final ClientCache clientCache) throws Exception {
    PoolFactory poolFactory = PoolManager.createFactory();

    poolFactory.setMaxConnections(2);
    poolFactory.setMinConnections(1);
    poolFactory.setReadTimeout(Long.valueOf(TimeUnit.SECONDS.toMillis(5)).intValue());
    poolFactory.setSubscriptionEnabled(false);
    poolFactory.addServer("localhost", 12480);

    Pool pool = poolFactory.create("serverConnectionPool");

    assertNotNull("The 'serverConnectionPool' was not properly configured and initialized!", pool);

    ClientRegionFactory<?, ?> clientRegionFactory = clientCache.createClientRegionFactory(ClientRegionShortcut.LOCAL);

    clientRegionFactory.setPoolName(pool.getName());

    Region<?, ?> example = clientRegionFactory.create("Example");

    assertNotNull("The '/Example' Region was not properly configured and initialized!", example);

    //configureIndexWithClientApi(clientCache);
    configureIndexWithServerApi(clientCache, pool);

    return clientCache;
  }

  private void configureIndexWithClientApi(final ClientCache clientCache) throws Exception {
    QueryService queryService = clientCache.getLocalQueryService();
    Index index = queryService.createIndex(INDEX_NAME, "id", "/Example");
    assertNotNull("An Index could not be created on the '/Example' client Region in the GemFire ClientCache!", index);
  }

  /**
   * Throws the following UnsupportedOperationException on queryService.createIndex(..)...
   *
   * java.lang.UnsupportedOperationException: Index creation on the server is not supported from the client.
   *   at com.gemstone.gemfire.cache.query.internal.DefaultQueryService.createIndex(DefaultQueryService.java:177)
   *   at com.gemstone.gemfire.cache.query.internal.DefaultQueryService.createIndex(DefaultQueryService.java:143)
   *   at org.pivotal.gemfire.cache.client.ClientCacheIndexingTest.configureIndexWithServerApi(ClientCacheIndexingTest.java:140)
   *   at org.pivotal.gemfire.cache.client.ClientCacheIndexingTest.configureGemFireClientCacheWithApi(ClientCacheIndexingTest.java:117)
   *   at org.pivotal.gemfire.cache.client.ClientCacheIndexingTest.setupGemFireClient(ClientCacheIndexingTest.java:90)
   */
  private void configureIndexWithServerApi(final ClientCache clientCache, final Pool pool) throws Exception {
    //QueryService queryService = clientCache.getQueryService(pool.getName());
    QueryService queryService = clientCache.getQueryService();
    Index index = queryService.createIndex(INDEX_NAME, "id", "/Example");
    assertNotNull("An Index could not be created on the '/Example' client Region in the GemFire ClientCache!", index);
  }

  @After
  public void shutdownGemFireClient() {
    if (clientCache != null) {
      clientCache.close();
    }

    clientCache = null;
  }

  protected Index getIndex(final GemFireCache gemfireCache, final String indexName) {
    QueryService queryService = (gemfireCache instanceof ClientCache
      ? ((ClientCache) gemfireCache).getLocalQueryService() : gemfireCache.getQueryService());

    for (Index index : queryService.getIndexes()) {
      if (index.getName().equals(indexName)) {
        return index;
      }
    }

    return null;
  }

  @Test
  public void testIndexByName() {
    assertNotNull("The GemFire ClientCache was not properly configured and initialized!", clientCache);

    Index actualIndex = getIndex(clientCache, INDEX_NAME);

    assertNotNull(actualIndex);
    assertEquals(INDEX_NAME, actualIndex.getName());
  }

}
