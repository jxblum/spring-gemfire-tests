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

import java.util.concurrent.TimeUnit;

/**
 * The SystemOutNonDaemonThreadInterruptResponsiveTest class...
 *
 * @author John Blum
 * @see java.lang.Thread
 * @see AbstractThreadBasedTest
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class NonDaemonThreadSystemOutInterruptTest extends AbstractThreadBasedTest {

  protected static Runnable newRunnable() {
    return new Runnable() {
      @Override public void run() {
        System.out.printf("Printing from Thread (%1$s)!%n", Thread.currentThread().getName());
      }
    };
  }

  public static void main(final String... args) throws Exception {
    Thread print = newThread("print", NON_DAEMON_THREAD, Thread.NORM_PRIORITY, newRunnable());

    synchronized (System.out) {
      print = startThread(print);
      System.out.println("Started 'print' Thread; waiting for 'print' Thread to finish...");
      print.join(TimeUnit.SECONDS.toMillis(5));
      System.out.println(print.isAlive() ? "'print' Thread did not finish; interrupting 'print' Thread!"
        : "'print' Thread finished!");
      print.interrupt();
      System.out.println("'print' Thread interrupted; program should exit!");
      print.join();
    }
  }

}
