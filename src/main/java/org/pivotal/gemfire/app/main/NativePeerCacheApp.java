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

package org.pivotal.gemfire.app.main;

import java.io.File;
import java.net.InetAddress;
import java.util.Scanner;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.DiskStore;
import org.apache.geode.cache.DiskStoreFactory;
import org.apache.geode.cache.EvictionAction;
import org.apache.geode.cache.EvictionAttributes;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.Scope;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.cache.wan.GatewaySender;
import org.apache.geode.cache.wan.GatewaySenderFactory;
import org.spring.data.gemfire.app.beans.Address;

/**
 * The NativePeerCacheApp class is a example test app using the GemFire public API to verify the correct behavior
 * of a GemFire Cache having a Partitioned Region (appData) with a persistent Gateway Sender (sender), where the
 * Gateway Sender's "Queue" has a Disk Store with name (gateway-sender-queue-disk-store).  As well, this example test
 * app verifies that a Local Region is allowed to have an Eviction Policy Action of LOCAL_DESTROY.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.DiskStore
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.wan.GatewaySender
 * @since 1.0.0
 * @link https://jira.springsource.org/browse/SGF-200
 * @link https://jira.springsource.org/browse/SGF-295
 */
public class NativePeerCacheApp {

  protected static final boolean DEFAULT_AUTO_COMPACT = true;
  protected static final boolean DEFAULT_MANUAL_START = false;
  protected static final boolean DEFAULT_PARALLEL = true;
  protected static final boolean DEFAULT_PERSISTENCE_ENABLED = true;

  protected static final int DEFAULT_COMPACTION_THRESHOLD = 75;
  protected static final int DEFAULT_DISPATCHER_THREADS = 1;
  protected static final int DEFAULT_QUEUE_SIZE = 50;
  protected static final int DEFAULT_SERVER_PORT = 12480;
  protected static final int REMOTE_DISTRIBUTED_SYSTEM_ID = 20;

  protected static final File DISK_STORE_DIRECTORY = new File("./gemfire/disk-stores");

  protected static final String GEMFIRE_APPLICATION_REGION_NAME = "Example";
  protected static final String GEMFIRE_GATEWAY_SENDER_NAME = "gateway-sender";
  protected static final String GEMFIRE_GATEWAY_SENDER_QUEUE_DISK_STORE_NAME = "gateway-sender-queue-disk-store";
  protected static final String GEMFIRE_HTTP_SERVICE_PORT = "0";
  protected static final String GEMFIRE_JMX_MANAGER = "true";
  protected static final String GEMFIRE_JMX_MANAGER_PORT = "1199";
  protected static final String GEMFIRE_JMX_MANAGER_START = "false";
  protected static final String GEMFIRE_LOG_LEVEL = "config";
  protected static final String GEMFIRE_LOCATORS = "localhost[11235]";
  protected static final String GEMFIRE_MCAST_PORT = "0";
  protected static final String GEMFIRE_MEMBER_NAME = "NativeGemFirePeerCache";
  protected static final String GEMFIRE_START_LOCATOR = GEMFIRE_LOCATORS;

  @SuppressWarnings("unused")
  public static void main(final String... args) throws Exception {
    Cache gemfireCache = createDefaultCache();

    //configurePartitionRegionWithGatewaySenderHavingQueueWithDiskStore(gemfireCache);
    //configureLocalRegionWithEvictionActionLocalDestroy(gemfireCache);
    configureReplicateRegion(gemfireCache);
    addCacheServer(gemfireCache);

    //waitOnInput();
  }

  protected static CacheServer addCacheServer(final Cache gemfireCache) throws Exception {
    CacheServer cacheServer = gemfireCache.addCacheServer();
    InetAddress localhost = InetAddress.getLocalHost();
    //cacheServer.setBindAddress(localhost.getHostAddress());
    cacheServer.setHostnameForClients(localhost.getHostName());
    cacheServer.setPort(DEFAULT_SERVER_PORT);
    cacheServer.start();
    return cacheServer;
  }

  protected static void assertDirectoryExists(final File directory) {
    assert (directory != null && directory.isDirectory()) : String.format("directory (%1$s) could not be found",
      directory);
  }

  protected static Cache createCache(final String name,
                                     final String gemfireHttpServicePort,
                                     final String gemfireJmxManager,
                                     final String gemfireJmxManagerPort,
                                     final String gemfireJmxManagerStart,
                                     final String locators,
                                     final String logLevel,
                                     final String mcastPort,
                                     final String startLocator)
  {
    return registerShutdownHook(new CacheFactory()
      .set("name", name)
      //.set("cache-xml-file", "cache-cqs.xml")
      .set("jmx-manager", gemfireJmxManager)
      .set("jmx-manager-http-port", gemfireHttpServicePort)
      .set("jmx-manager-port", gemfireJmxManagerPort)
      .set("jmx-manager-start", gemfireJmxManagerStart)
      .set("locators", locators)
      .set("log-level", logLevel)
      .set("mcast-port", mcastPort)
      .set("start-locator", startLocator)
      .create());
  }

  protected static Cache createDefaultCache() {
    return createCache(GEMFIRE_MEMBER_NAME,
      GEMFIRE_HTTP_SERVICE_PORT,
      GEMFIRE_JMX_MANAGER,
      GEMFIRE_JMX_MANAGER_PORT,
      GEMFIRE_JMX_MANAGER_START,
      GEMFIRE_LOCATORS,
      GEMFIRE_LOG_LEVEL,
      GEMFIRE_MCAST_PORT,
      GEMFIRE_START_LOCATOR);
  }

  protected static File createDirectory(final File directory) {
    assert directory != null : "the directory File reference must not be null";

    if (!(directory.isDirectory() || directory.mkdirs())) {
      System.err.printf("directory (%1$s) could not be created", directory);
    }

    return directory;
  }

  protected static DiskStore createDiskStore(final Cache gemfireCache, final String name, final File diskStoreDirectory) {
    DiskStoreFactory diskStoreFactory = gemfireCache.createDiskStoreFactory();

    diskStoreFactory.setAutoCompact(DEFAULT_AUTO_COMPACT);
    diskStoreFactory.setCompactionThreshold(DEFAULT_COMPACTION_THRESHOLD);
    diskStoreFactory.setQueueSize(DEFAULT_QUEUE_SIZE);
    diskStoreFactory.setDiskDirs(new File[] { diskStoreDirectory });

    DiskStore diskStore = diskStoreFactory.create(name);

    assert diskStore != null : "DiskStore creation failed";

    return diskStore;
  }

  protected static GatewaySender createGatewaySender(final Cache gemfireCache, final String name, final String diskStoreName) {
    GatewaySenderFactory gatewaySenderFactory = gemfireCache.createGatewaySenderFactory();

    gatewaySenderFactory.setDispatcherThreads(DEFAULT_DISPATCHER_THREADS);
    gatewaySenderFactory.setManualStart(DEFAULT_MANUAL_START);
    gatewaySenderFactory.setParallel(DEFAULT_PARALLEL);
    gatewaySenderFactory.setPersistenceEnabled(DEFAULT_PERSISTENCE_ENABLED);
    gatewaySenderFactory.setDiskStoreName(diskStoreName);

    GatewaySender gatewaySender = gatewaySenderFactory.create(name, REMOTE_DISTRIBUTED_SYSTEM_ID);

    assert gatewaySender != null : "GatewaySender creation failed";

    return gatewaySender;
  }

  protected static Region createLocalRegionWithEvictionActionLocalDestroy(final Cache gemfireCache, final String name) {
    return createRegion(gemfireCache, name, new RegionFactoryPostProcessor() {
      @Override public void postProcess(final RegionFactory regionFactory) {
        regionFactory.setDataPolicy(DataPolicy.NORMAL);
        regionFactory.setEvictionAttributes(EvictionAttributes.createLRUEntryAttributes(1000,
          EvictionAction.LOCAL_DESTROY));
        regionFactory.setScope(Scope.LOCAL);
      }
    });
  }

  protected static Region createPartitionRegionWithGatewaySender(final Cache gemfireCache, final String name, final GatewaySender gatewaySender) {
    return createRegion(gemfireCache, name, new RegionFactoryPostProcessor() {
      @Override public void postProcess(final RegionFactory regionFactory) {
        regionFactory.setDataPolicy(DataPolicy.PARTITION);
        regionFactory.addGatewaySenderId(gatewaySender.getId());
      }
    });
  }

  @SuppressWarnings("unchecked")
  protected static Region createReplicateRegion(final Cache gemfireCache, final String name) {
    return createRegion(gemfireCache, name, new RegionFactoryPostProcessor() {
      @Override public void postProcess(final RegionFactory regionFactory) {
        regionFactory.setDataPolicy(DataPolicy.REPLICATE);
        regionFactory.setKeyConstraint(String.class);
        regionFactory.setValueConstraint(Address.class);
        //regionFactory.setScope(Scope.DISTRIBUTED_ACK);
      }
    });
  }
  protected static Region createRegion(final Cache gemfireCache, final String name, final RegionFactoryPostProcessor regionFactoryPostProcessor) {
    RegionFactory regionFactory = gemfireCache.createRegionFactory();

    regionFactoryPostProcessor.postProcess(regionFactory);

    Region region = regionFactory.create(name);

    assert region != null : String.format("'%1$s' Region could not be created", name);

    return region;
  }

  @SuppressWarnings("unused")
  private static void configurePartitionRegionWithGatewaySenderHavingQueueWithDiskStore(final Cache gemfireCache) {
    assertDirectoryExists(createDirectory(DISK_STORE_DIRECTORY));

    createDiskStore(gemfireCache, GEMFIRE_GATEWAY_SENDER_QUEUE_DISK_STORE_NAME, DISK_STORE_DIRECTORY);

    GatewaySender gatewaySender = createGatewaySender(gemfireCache, GEMFIRE_GATEWAY_SENDER_NAME,
      GEMFIRE_GATEWAY_SENDER_QUEUE_DISK_STORE_NAME);

    createPartitionRegionWithGatewaySender(gemfireCache, GEMFIRE_APPLICATION_REGION_NAME, gatewaySender);
  }

  @SuppressWarnings("unused")
  private static void configureLocalRegionWithEvictionActionLocalDestroy(final Cache gemfireCache) {
    createLocalRegionWithEvictionActionLocalDestroy(gemfireCache, GEMFIRE_APPLICATION_REGION_NAME);
  }

  @SuppressWarnings("unused")
  private static void configureReplicateRegion(final Cache gemfireCache) {
    createReplicateRegion(gemfireCache, GEMFIRE_APPLICATION_REGION_NAME);
  }

  protected static Cache registerShutdownHook(final Cache gemfireCache) {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override public void run() {
        if (!gemfireCache.getCacheServers().isEmpty()) {
          for (CacheServer cacheServer : gemfireCache.getCacheServers()) {
            cacheServer.stop();
          }
        }

        gemfireCache.close();
      }
    }));

    return gemfireCache;
  }
  @SuppressWarnings("unused")
  protected static void waitOnInput() {
    System.out.printf("Press enter to exit...%n");
    new Scanner(System.in).next();
  }

  protected interface RegionFactoryPostProcessor {
    void postProcess(RegionFactory regionFactory);
  }

}
