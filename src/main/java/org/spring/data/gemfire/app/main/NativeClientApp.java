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

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;

/**
 * The NativeClientApp class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class NativeClientApp {

  public static void main(String[] args) {
    ClientCache gemfireCache = new ClientCacheFactory()
      .set("name", NativeClientApp.class.getSimpleName())
      .set("log-level", "config")
      .addPoolServer("localhost", 40404)
      .create();

    ClientRegionFactory<Object, Object> exampleProxyRegionFactory =
      gemfireCache.createClientRegionFactory(ClientRegionShortcut.PROXY);

    Region<Object, Object> exampleProxyRegion = exampleProxyRegionFactory.create("Example");

    exampleProxyRegion.put("keyOne", "valueOne");

    assertThat(exampleProxyRegion.get("keyOne")).isEqualTo("valueOne");
  }
}
