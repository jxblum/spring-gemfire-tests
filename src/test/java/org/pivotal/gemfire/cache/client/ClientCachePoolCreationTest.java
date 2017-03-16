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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.LoaderHelper;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.PoolFactory;
import org.apache.geode.cache.client.PoolManager;
import org.apache.geode.cache.server.CacheServer;
import org.codeprimate.lang.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spring.data.gemfire.AbstractGemFireIntegrationTest;

/**
 * The ClientCachePoolCreationTest class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class ClientCachePoolCreationTest extends AbstractGemFireIntegrationTest {

  protected static Process gemfireServer;

  protected static final int MAX_CONNECTIONS = 50;

  protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-dd-MM-mm-ss");

  protected static final File JAVA_HOME = new File(System.getProperty("java.home"));
  protected static final File JAVA_EXE = new File(new File(JAVA_HOME, "bin"), "java");

  protected static final String LOG_LEVEL = "config";
  protected static final String REGION_NAME = "Factorials";

  private Region<Long, Long> factorials;

  @BeforeClass
  public static void setupGemFireServer() throws IOException {
    gemfireServer = runProcess(buildJavaCommandLine(GemFireServerLauncher.class),
      new File(String.format("gemfire-server-%1$s", DATE_FORMAT.format(new Date()))));
    waitOnServer(TimeUnit.SECONDS.toMillis(30));
  }

  static String[] buildJavaCommandLine(Class type, String... args) {
    List<String> javaCommandLine = new ArrayList<String>();

    javaCommandLine.add(JAVA_EXE.getAbsolutePath());
    javaCommandLine.add("-server");
    javaCommandLine.add("-ea");
    javaCommandLine.add("-classpath");
    javaCommandLine.add(System.getProperty("java.class.path"));
    javaCommandLine.add(type.getName());
    javaCommandLine.addAll(Arrays.asList(args));

    return javaCommandLine.toArray(new String[javaCommandLine.size()]);
  }

  static Process runProcess(String[] commandLine, File workingDirectory) throws IOException {
    assertThat(workingDirectory != null && (workingDirectory.isDirectory() || workingDirectory.mkdirs()), is(true));
    return new ProcessBuilder(commandLine).directory(workingDirectory).start();
  }

  @SuppressWarnings("all")
  static boolean waitOnCondition(final long duration, Condition condition) {
    final long timeout = (System.currentTimeMillis() + duration);

    try {
      while (System.currentTimeMillis() < timeout && !condition.evaluate()) {
        synchronized (condition) {
          TimeUnit.MILLISECONDS.timedWait(condition, Math.min(500l, (timeout - System.currentTimeMillis())));
        }
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    return condition.evaluate();
  }

  static boolean waitOnServer(final long duration) {
    final AtomicBoolean connected = new AtomicBoolean(false);

    return waitOnCondition(duration, () -> {
      Socket socket = null;

      try {
        if (!connected.get()) {
          socket = new Socket(GemFireServerLauncher.CACHE_SERVER_BIND_ADDRESS, GemFireServerLauncher.DEFAULT_CACHE_SERVER_PORT);
          connected.set(true);
        }
      }
      catch (IOException ignore) {
      }
      finally {
        close(socket);
      }

      return connected.get();
    });
  }

  static boolean close(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
      return true;
    }
    catch (IOException e) {
      return false;
    }
  }

  @AfterClass
  public static void shutdownGemFireServer() {
    if (gemfireServer != null) {
      gemfireServer.destroyForcibly();
      gemfireServer = null;
    }
  }

  @Before
  public void setupGemFireClient() {
    ClientCache clientCache = new ClientCacheFactory()
      .set("name", ClientCachePoolCreationTest.class.getSimpleName())
      .set("log-level", LOG_LEVEL)
      .create();

    PoolFactory poolFactory = PoolManager.createFactory();

    poolFactory.setFreeConnectionTimeout(5000); // 5 seconds
    poolFactory.setIdleTimeout(TimeUnit.MINUTES.toMillis(2));
    poolFactory.setMaxConnections(MAX_CONNECTIONS);
    poolFactory.setPingInterval(TimeUnit.SECONDS.toMillis(15));
    poolFactory.setPRSingleHopEnabled(true);
    poolFactory.setReadTimeout(2000); // 2 seconds
    poolFactory.setRetryAttempts(1);
    poolFactory.setThreadLocalConnections(false);
    poolFactory.addServer(GemFireServerLauncher.CACHE_SERVER_BIND_ADDRESS,
      GemFireServerLauncher.DEFAULT_CACHE_SERVER_PORT);
    poolFactory.create("testPool");

    ClientRegionFactory<Long, Long> regionFactory = clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY);

    regionFactory.setKeyConstraint(Long.class);
    regionFactory.setValueConstraint(Long.class);
    regionFactory.setPoolName("testPool");
    factorials = regionFactory.create(REGION_NAME);
  }

  @After
  public void shutdownGemFireClient() {
    ClientCache clientCache = ClientCacheFactory.getAnyInstance();

    if (clientCache != null) {
      clientCache.close();
    }
  }

  @Test
  public void computeFactorials() {
    assertThat(factorials.get(0l), is(equalTo(1l)));
    assertThat(factorials.get(1l), is(equalTo(1l)));
    assertThat(factorials.get(2l), is(equalTo(2l)));
    assertThat(factorials.get(3l), is(equalTo(6l)));
    assertThat(factorials.get(4l), is(equalTo(24l)));
    assertThat(factorials.get(5l), is(equalTo(120l)));
    assertThat(factorials.get(6l), is(equalTo(720l)));
    assertThat(factorials.get(7l), is(equalTo(5040l)));
    assertThat(factorials.get(8l), is(equalTo(40320l)));
    assertThat(factorials.get(9l), is(equalTo(362880l)));
    assertThat(factorials.get(10l), is(equalTo(3628800l)));
  }

  public static final class GemFireServerLauncher {

    protected static final int DEFAULT_CACHE_SERVER_PORT = CacheServer.DEFAULT_PORT;

    protected static final String CACHE_SERVER_BIND_ADDRESS = "localhost";
    protected static final String CACHE_SERVER_HOSTNAME_FOR_CLIENTS = CACHE_SERVER_BIND_ADDRESS;

    public static void main(final String[] args) throws Exception {
      addCacheServer(createRegion(registerShutdownHook(createCache("GemFireServerLauncher"))));
    }

    static Cache createCache(String name) {
      return new CacheFactory()
        .set("name", name)
        .set("mcast-port", "0")
        .set("log-level", LOG_LEVEL)
        .create();
    }

    static Cache createRegion(Cache cache) {
      return createRegion(cache, REGION_NAME);
    }

    static Cache createRegion(Cache cache, String regionName) {
      RegionFactory<Long, Long> regionFactory = cache.createRegionFactory(RegionShortcut.REPLICATE);

      regionFactory.setCacheLoader(factorialCacheLoader());
      regionFactory.setKeyConstraint(Long.class);
      regionFactory.setValueConstraint(Long.class);
      regionFactory.create(regionName);

      return cache;
    }

    static CacheLoader<Long, Long> factorialCacheLoader() {
      return new CacheLoader<Long, Long>() {
        public Long load(final LoaderHelper<Long, Long> helper) throws CacheLoaderException {
          Long number = helper.getKey();

          Assert.notNull(number, String.format("factorial(%1$s) is not valid", number));

          if (number < 3l) {
            return (number < 2l ? 1l : 2l);
          }

          long result = number;

          while (number-- > 1) {
            result *= number;
          }

          return result;
        }

        public void close() {
        }
      };
    }

    static Cache addCacheServer(Cache cache) throws IOException {
      return addCacheServer(cache, DEFAULT_CACHE_SERVER_PORT);
    }

    static Cache addCacheServer(Cache cache, int port) throws IOException {
      CacheServer cacheServer = cache.addCacheServer();

      cacheServer.setBindAddress(CACHE_SERVER_BIND_ADDRESS);
      cacheServer.setHostnameForClients(CACHE_SERVER_HOSTNAME_FOR_CLIENTS);
      cacheServer.setPort(port);
      cacheServer.setMaxConnections(MAX_CONNECTIONS);
      cacheServer.start();

      return cache;
    }

    static Cache registerShutdownHook(Cache cache) {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        if (cache != null) {
          cache.close();
        }
      }, "GemFire Server Shutdown Hook"));

      return cache;
    }
  }

  interface Condition {
    boolean evaluate();
  }

}
