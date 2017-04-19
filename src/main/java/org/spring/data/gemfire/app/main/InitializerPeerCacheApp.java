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

import java.util.Collections;

import org.codeprimate.util.PropertyUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.gemfire.support.SpringContextBootstrappingInitializer;
import org.springframework.util.StringUtils;

/**
 * The InitializerPeerCacheApp class...
 *
 * @author John Blum
 * @see org.spring.data.gemfire.app.main.AbstractApp
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class InitializerPeerCacheApp extends AbstractApp {

  public static final String DEFAULT_CONTEXT_CONFIGURATION_FILE = "spring-gemfire-context-with-clusterconfig-and-cachexml-example.xml";

  public static void main(final String... args) {
    new InitializerPeerCacheApp(args).run();
  }

  public InitializerPeerCacheApp(String... args) {
    super(args);
  }

  @Override
  protected String[] getDefaultConfigurationFile() {
    return new String[] { DEFAULT_CONTEXT_CONFIGURATION_FILE };
  }

  @Override
  protected ConfigurableApplicationContext initApplicationContext(final String... args) {
    new SpringContextBootstrappingInitializer().init(PropertyUtils.createProperties(Collections.singletonMap(
      SpringContextBootstrappingInitializer.CONTEXT_CONFIG_LOCATIONS_PARAMETER, StringUtils.arrayToCommaDelimitedString(
        getConfigurationFile(args)))));

    return SpringContextBootstrappingInitializer.getApplicationContext();
  }
}
