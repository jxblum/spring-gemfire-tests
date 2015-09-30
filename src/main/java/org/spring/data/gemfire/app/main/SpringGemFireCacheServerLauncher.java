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

package org.spring.data.gemfire.app.main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gemstone.gemfire.cache.Cache;

import org.codeprimate.process.support.ProcessUtils;
import org.spring.data.gemfire.support.CacheUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

/**
 * The SpringGemFireCacheServerLauncher class is a launcher expecting and using Spring Data GemFire XML namespace
 * configuration meta-data to bootstrap a GemFire Cache Server.
 *
 * @author John Blum
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.support.ClassPathXmlApplicationContext
 * @see com.gemstone.gemfire.cache.Cache
 * @since 1.0.0
 * @since 7.0.1 (GemFire)
 */
@SuppressWarnings("unused")
public class SpringGemFireCacheServerLauncher implements Runnable {

  public static final String SERVER_PID_FILE_NAME = "server.pid";

  private static final long WAIT_TIMEOUT = 500l;

  private static final File SERVER_PID_FILE = new File(System.getProperty("user.dir"), SERVER_PID_FILE_NAME);

  private AtomicBoolean running = new AtomicBoolean(false);

  private Cache cache;

  private ConfigurableApplicationContext applicationContext;

  private final String[] configLocations;

  public SpringGemFireCacheServerLauncher(final String[] configLocations) {
    Assert.notEmpty(configLocations,
      "At least 1 location to a Spring GemFire ApplicationContext configuration file must be provided!");
    this.configLocations = configLocations;
  }

  protected ConfigurableApplicationContext getApplicationContext() {
    Assert.state(applicationContext != null, "The Spring Application Context was not initialized!");
    return applicationContext;
  }

  protected Cache getCache() {
    Assert.state(cache != null, "The GemFire Cache was not initialized!");
    return cache;
  }

  protected String[] getConfigLocations() {
    return configLocations;
  }

  public boolean isConnected() {
    return (cache != null && cache.getDistributedSystem().isConnected());
  }

  public boolean isRunning() {
    return running.get();
  }

  private boolean isWaiting() {
    return (isRunning() && isConnected());
  }

  private void initApplicationContext() {
    applicationContext = new ClassPathXmlApplicationContext(getConfigLocations());
    applicationContext.registerShutdownHook();
  }

  // NOTE this may be redundant with ConfigurableApplicationContext.registerShutdownHook()
  private void registerShutdownHook(final Cache cache) {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        CacheUtils.close(cache);
        running.set(false);
      }
    }));
  }

  private void waitOnServer() {
    while (isWaiting()) {
      try {
        synchronized (this) {
          wait(WAIT_TIMEOUT);
        }
      }
      catch (InterruptedException ignore) {
      }
    }
  }

  private void writeServerPidFile() {
    final int serverProcessId = ProcessUtils.currentProcessId();

    try {
      ProcessUtils.writePid(SERVER_PID_FILE, serverProcessId);
    }
    catch (IOException e) {
      System.err.printf("Failed to write GemFire Server process ID (%1$s) to file )(%2$s)!%n",
        serverProcessId, SERVER_PID_FILE);
    }
  }

  @Override
  public void run() {
    initApplicationContext();
    cache = getApplicationContext().getBean(Cache.class);
    registerShutdownHook(cache);
    running.set(true);
    writeServerPidFile();
    waitOnServer();
  }

  public void stop() {
    running.set(false);
  }

  public static void main(final String... args) {
    if (args.length == 0) {
      System.err.printf("$java org.spring.data.gemfire.app.main.SpringGemFireCacheServerLauncher <path-to-spring-xml-file>%n");
      System.exit(-1);
    }

    new SpringGemFireCacheServerLauncher(args).run();
  }

}
