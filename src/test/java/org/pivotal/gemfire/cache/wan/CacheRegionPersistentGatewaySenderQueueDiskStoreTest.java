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

package org.pivotal.gemfire.cache.wan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.DiskStore;
import org.apache.geode.cache.DiskStoreFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.wan.GatewaySender;
import org.apache.geode.cache.wan.GatewaySenderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The CacheRegionPersistentGatewaySenderQueueDiskStoreTest class is a test suite containing a single test case to test
 * and verify the correct behavior of a GemFire Cache having a Partitioned Region (appData) with a persistent
 * Gateway Sender (sender), where the Gateway Sender's "Queue" has a Disk Store with name
 * (gateway-sender-queue-disk-store) using GemFire's public API.
 *
 * This test is based on the JIRA issue SGF-200 (Extra unnecessary directory for disk store created when an embedded
 * sender starts up), filed for the Spring Data GemFire project.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.DiskStore
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.wan.GatewaySender
 * @link https://jira.springsource.org/browse/SGF-200
 * @since GemFire 7.0.1
 * @since Spring Data GemFire 1.3.3.BUILD-SNAPSHOT
 */
public class CacheRegionPersistentGatewaySenderQueueDiskStoreTest {

  private static final int REMOTE_DISTRIBUTED_SYSTEM_ID = 20;

  private static final File GEMFIRE_BASE_DIRECTORY = new File("./gemfire");
  private static final File DISK_STORE_DIRECTORY = new File(GEMFIRE_BASE_DIRECTORY, "gateway-disk-store");

  private static final String GEMFIRE_GATEWAY_SENDER_NAME = "sender";
  private static final String GEMFIRE_GATEWAY_SENDER_QUEUE_DISK_STORE_NAME = "gateway-sender-queue-disk-store";
  private static final String GEMFIRE_LOG_LEVEL = "config";
  private static final String GEMFIRE_LOCATORS = "localhost[11235]";
  private static final String GEMFIRE_MCAST_PORT = "0";
  private static final String GEMFIRE_MEMBER_NAME = "gemfirePeerCacheWithGatewaySender";
  private static final String GEMFIRE_PARTITION_REGION_APP_DATA = "appData";

  private Cache gemfireCache;

  private GatewaySender gatewaySender;

  protected static void assertDirectoryExists(final File directory) {
    assertTrue(String.format("The Directory (%1$s) could not be found!", directory),
      directory != null && directory.isDirectory());
  }

  protected static File createDirectory(final File directory) {
    assertNotNull(String.format("The Directory File reference cannot be null!"), directory);

    if (!directory.mkdirs()) {
      System.err.printf("The Directory (%1$s) could not be created!", directory);
    }

    return directory;
  }

  protected static void deleteRecursive(final File path) {
    if (path.isDirectory()) {
      for (File file : path.listFiles()) {
        deleteRecursive(file);
      }
    }

    path.delete();
  }

  @Before
  public void setup() {
    gemfireCache = new CacheFactory()
      .set("locators", GEMFIRE_LOCATORS)
      .set("log-level", GEMFIRE_LOG_LEVEL)
      .set("mcast-port", GEMFIRE_MCAST_PORT)
      .set("name", GEMFIRE_MEMBER_NAME)
      .set("start-locator", GEMFIRE_LOCATORS)
      .create();

    assertNotNull("The GemFire Cache cannot be null!", gemfireCache);

    assertDirectoryExists(createDirectory(DISK_STORE_DIRECTORY));

    DiskStoreFactory diskStoreFactory = gemfireCache.createDiskStoreFactory();

    diskStoreFactory.setAutoCompact(true);
    diskStoreFactory.setCompactionThreshold(75);
    diskStoreFactory.setQueueSize(50);
    diskStoreFactory.setDiskDirs(new File[] { DISK_STORE_DIRECTORY });

    DiskStore gatewaySenderQueueDiskStore = diskStoreFactory.create(GEMFIRE_GATEWAY_SENDER_QUEUE_DISK_STORE_NAME);

    assertNotNull("The Gateway Sender Queue's Disk Store cannot be null!", gatewaySenderQueueDiskStore);

    GatewaySenderFactory gatewaySenderFactory = gemfireCache.createGatewaySenderFactory();

    gatewaySenderFactory.setManualStart(false);
    gatewaySenderFactory.setParallel(true);
    gatewaySenderFactory.setPersistenceEnabled(true);
    gatewaySenderFactory.setDiskStoreName(GEMFIRE_GATEWAY_SENDER_QUEUE_DISK_STORE_NAME);

    gatewaySender = gatewaySenderFactory.create(GEMFIRE_GATEWAY_SENDER_NAME,
      REMOTE_DISTRIBUTED_SYSTEM_ID);

    assertNotNull("The Gateway Sender cannot be null!", gatewaySender);

    RegionFactory regionFactory = gemfireCache.createRegionFactory();

    regionFactory.setDataPolicy(DataPolicy.PARTITION);
    regionFactory.addGatewaySenderId(gatewaySender.getId());

    Region appDataRegion = regionFactory.create(GEMFIRE_PARTITION_REGION_APP_DATA);

    assertNotNull("The 'appData' Region cannot be null!", appDataRegion);
  }

  @After
  public void tearDown() {
    gatewaySender.stop();
    gatewaySender = null;
    gemfireCache.close();
    gemfireCache = null;
    deleteRecursive(GEMFIRE_BASE_DIRECTORY);
  }

  @Test
  public void testGatewaySenderQueueDiskStoreNameIsNotAnExistingFileOrDirectory() {
    assertTrue(gatewaySender.isRunning());
    assertFalse(String.format(
      "A directory based on the Persistent Gateway Sender Queue's Disk Store name (%1$s) should not exist!",
        GEMFIRE_GATEWAY_SENDER_QUEUE_DISK_STORE_NAME), new File(GEMFIRE_GATEWAY_SENDER_QUEUE_DISK_STORE_NAME).exists());
  }

}
