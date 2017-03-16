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

import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;

/**
 * The ClientCacheApp class is an Spring-based Java application running as a GemFire Cache client.
 *
 * @author John Blum
 * @see org.springframework.context.ConfigurableApplicationContext
 * @since 1.0.0
 * @since 7.0.1 (GemFire)
 */
@SuppressWarnings("unused")
public class ClientCacheApp extends AbstractApp {

  protected static final String DEFAULT_CLIENT_CONFIGURATION_FILE = "clientCache.xml";

  public ClientCacheApp(final String... args) {
    super(args);
  }

  @Override
  protected String[] getDefaultConfigurationFile() {
    return new String[] { DEFAULT_CLIENT_CONFIGURATION_FILE };
  }

  @Override
  public void run() {
    final ClientCache clientCache = getBean(DEFAULT_GEMFIRE_CACHE_BEAN_NAME, ClientCache.class);

    assert clientCache != null : String.format(
      "The GemFire ClientCache was not properly configured and initialized in the Spring context!");

    System.out.printf("Current GemFire Cache Servers are...%n");

    for (InetSocketAddress address : clientCache.getDefaultPool().getServers()) {
      System.out.printf("%1$s%n", address);
    }

    System.out.printf("Current GemFire Client Cache Regions are...%n");

    for (Region region : clientCache.rootRegions()) {
      System.out.printf("%1$s%n", region.getFullPath());
    }

    if (!clientCache.rootRegions().isEmpty()) {
      Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
        @Override public void run() {
          for (Region<?, ?> region : clientCache.rootRegions()) {
            System.out.printf("*** Region %1$s ***%n", region.getFullPath());
            for (Entry entry : region.entrySet()) {
              System.out.printf("%1$s = %2$s%n", entry.getKey(), entry.getValue());
            }
          }
        }
      }, 5, 15, TimeUnit.SECONDS);
    }

    super.run();
  }

  public static void main(final String... args) {
    new ClientCacheApp(args).run();
  }

}
