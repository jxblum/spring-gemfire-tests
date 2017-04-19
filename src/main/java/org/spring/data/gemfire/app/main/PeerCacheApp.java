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

/**
 * The PeerCacheApp class is an application running as a peer GemFire Server/data node.
 *
 * @author John Blum
 * @see org.springframework.context.ConfigurableApplicationContext
 * @since 1.0.0
 * @since 7.0.1 (GemFire)
 */
@SuppressWarnings("unused")
public class PeerCacheApp extends AbstractApp {

  public static final String DEFAULT_SERVER_CONFIGURATION_FILE = "peerCache.xml";

  public static void main(String[] args) {
    new PeerCacheApp(args).run();
  }

  public PeerCacheApp(String... args) {
    super(args);
  }

  @Override
  protected String[] getDefaultConfigurationFile() {
    return new String[] { DEFAULT_SERVER_CONFIGURATION_FILE };
  }
}
