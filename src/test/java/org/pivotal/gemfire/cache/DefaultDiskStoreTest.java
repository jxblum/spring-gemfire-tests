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

package org.pivotal.gemfire.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.DiskStore;
import org.apache.geode.cache.DiskStoreFactory;
import org.apache.geode.cache.EvictionAction;
import org.apache.geode.cache.EvictionAttributes;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The DefaultDiskStoreTest class is a test suite of test cases testing the contract and functionality
 * of GemFire's public Java API used to configure GemFire's "DEFAULT" DiskStore.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.DataPolicy
 * @see org.apache.geode.cache.DiskStore
 * @see org.apache.geode.cache.Region
 * @since 1.0.0
 */
public class DefaultDiskStoreTest {

  private static DiskStore defaultDiskStore;

  @BeforeClass
  public static void setupGemFire() {
    Cache gemfireCache = new CacheFactory()
      .set(DistributionConfig.NAME_NAME, "DefaultDiskStoreTest")
      .set(DistributionConfig.MCAST_PORT_NAME, "0")
      .set(DistributionConfig.LOG_LEVEL_NAME, "config")
      .create();

    RegionFactory regionFactory = gemfireCache.createRegionFactory();

    regionFactory.setDataPolicy(DataPolicy.PERSISTENT_REPLICATE);
    regionFactory.setEvictionAttributes(EvictionAttributes.createLRUEntryAttributes(300,
      EvictionAction.OVERFLOW_TO_DISK));

    Region persistentReplicateRegion = regionFactory.create("Example");

    assertNotNull(persistentReplicateRegion);
    assertEquals("Example", persistentReplicateRegion.getName());
    assertEquals("/Example", persistentReplicateRegion.getFullPath());
    assertNotNull(persistentReplicateRegion.getAttributes());
    assertEquals(DataPolicy.PERSISTENT_REPLICATE, persistentReplicateRegion.getAttributes().getDataPolicy());

    DiskStoreFactory diskStoreFactory = gemfireCache.createDiskStoreFactory();

    diskStoreFactory.setAutoCompact(true);
    diskStoreFactory.setQueueSize(50);
    diskStoreFactory.setTimeInterval(9999l);

    defaultDiskStore = diskStoreFactory.create("DEFAULT"); // BOOM!
  }

  @Test
  public void testDefaultDiskStoreConfiguration() {
    assertNotNull("The 'DEFAULT' GemFire DiskStore reference was not properly configured and initialized!",
      defaultDiskStore);
    assertEquals("DEFAULT", defaultDiskStore.getName());
    assertTrue(defaultDiskStore.getAllowForceCompaction());
    assertTrue(defaultDiskStore.getAutoCompact());
    assertEquals(75, defaultDiskStore.getCompactionThreshold());
    assertEquals(50, defaultDiskStore.getQueueSize());
    assertEquals(10, defaultDiskStore.getMaxOplogSize());
    assertEquals(60000, defaultDiskStore.getTimeInterval());
    assertEquals(16384, defaultDiskStore.getWriteBufferSize());
  }

}
