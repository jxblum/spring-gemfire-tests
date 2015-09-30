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

package org.lab.tests;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The AbstractThreadBasedTest class...
 *
 * @author John Blum
 * @see java.lang.Thread
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractThreadBasedTest {

  protected static final boolean DAEMON_THREAD = true;
  protected static final boolean NON_DAEMON_THREAD = false;

  protected static final boolean DEFAULT_THREAD_DAEMON = DAEMON_THREAD;

  protected static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;

  protected static final AtomicInteger THREAD_ID_SEQUENCE = new AtomicInteger(0);

  protected static Thread newThread(final String name, final Runnable task) {
    return newThread(name, DEFAULT_THREAD_DAEMON, DEFAULT_THREAD_PRIORITY, task);
  }

  protected static Thread newThread(final String name, final boolean daemon, final int priority, final Runnable task) {
    Thread thread = new Thread(task, String.format("%1$s-thread:id(%2$d)", name, THREAD_ID_SEQUENCE.incrementAndGet()));
    thread.setDaemon(daemon);
    thread.setPriority(priority);
    return thread;
  }

  protected static Thread startThread(final Thread thread) {
    thread.start();
    return thread;
  }

}
