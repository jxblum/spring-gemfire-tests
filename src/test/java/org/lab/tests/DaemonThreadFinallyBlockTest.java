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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The DaemonThreadFinallyBlocksTest class...
 *
 * @author John Blum
 * @see java.lang.Thread
 * @see AbstractThreadBasedTest
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class DaemonThreadFinallyBlockTest extends AbstractThreadBasedTest {

  protected static final AtomicInteger VOLLEY_COUNT = new AtomicInteger(0);

  protected static final AtomicReference<String> VOLLEY = new AtomicReference<String>("pong");

  protected static Runnable newRunnable(final String receiveValue, final String returnValue) {
    return new Runnable() {
      @Override public void run() {
        try {
          while (true) {
            while (!VOLLEY.compareAndSet(receiveValue, returnValue)) {
              try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
              }
              catch (InterruptedException ignore) {
              }
            }
            System.out.printf("%1$s-%2$d%n", returnValue, (VOLLEY_COUNT.getAndIncrement() / 2));

          }
        }
        finally {
          System.out.printf("%1$s exiting...%n", Thread.currentThread().getName());
        }
      }
    };
  }

  public static void main(final String... args) throws Exception {
    Thread ping = startThread(newThread("ping", newRunnable("pong", "ping")));
    Thread pong = startThread(newThread("pong", newRunnable("ping", "pong")));

    pong.join(TimeUnit.SECONDS.toMillis(10));
  }

}
