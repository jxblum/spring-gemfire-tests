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

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

/**
 * The AbstractApp class is an abstract base class for writing Java Spring application classes.
 *
 * @author John Blum
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.support.ClassPathXmlApplicationContext
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractApp implements Runnable {

  protected static final String DEFAULT_GEMFIRE_CACHE_BEAN_NAME = "gemfireCache";

  private final AtomicReference<ConfigurableApplicationContext> applicationContextReference = new AtomicReference<>();

  protected static void block() {
    System.out.printf("Press enter to exit...%n");
    new Scanner(System.in).next();
  }

  public AbstractApp(final String... args) {
    applicationContextReference.set(initApplicationContext(args));
  }

  protected ConfigurableApplicationContext getApplicationContext() {
    ConfigurableApplicationContext localApplicationContext = applicationContextReference.get();
    Assert.state(localApplicationContext != null, "The Spring ApplicationContext was not properly configured and initialized!");
    return localApplicationContext;
  }

  protected <T> T getBean(final String beanName, final Class<T> beanType) {
    return getApplicationContext().getBean(beanName, beanType);
  }

  protected String[] getConfigurationFile(final String... args) {
    return (args != null && args.length > 0 ? args : getDefaultConfigurationFile());
  }

  protected abstract String[] getDefaultConfigurationFile();

  protected ConfigurableApplicationContext initApplicationContext(final String... args) {
    ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(getConfigurationFile(args));
    applicationContext.registerShutdownHook();
    return applicationContext;
  }

  @Override
  public void run() {
    block();
  }

}
