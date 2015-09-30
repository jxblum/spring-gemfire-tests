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

import com.gemstone.gemfire.distributed.LocatorLauncher;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The LocatorApp class uses Spring Data GemFire XML namespace configuration to construct, configure and initialize a
 * GemFire Locator.
 *
 * @author John Blum
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see com.gemstone.gemfire.distributed.LocatorLauncher
 * @since 1.0.0
 * @since 7.0.1 (GemFire)
 */
public class LocatorApp {

  public static final String LOCATOR_CONFIGURATION_FILE = "locator.xml";

  public static void main(final String... args) {
    ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(LOCATOR_CONFIGURATION_FILE);

    applicationContext.registerShutdownHook();

    LocatorLauncher locator = applicationContext.getBean("locator", LocatorLauncher.class);

    locator.start();

    System.out.printf("Starting Locator (%1$s) on port (%2$d)...%n", locator.getMember(), locator.getPort());

    locator.waitOnLocator();

    System.out.printf("Locator stopping...");
  }

}
