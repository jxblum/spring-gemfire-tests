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

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * The RunOutOfMemoryApp class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class RunOutOfMemoryApp {

  private static final List<Long> numbers = new LinkedList<Long>();

  private static void runIntoTheGround() {
    Thread runner = new Thread(new Runnable() {
      @Override public void run() {
        for (long number = 0; number < Long.MAX_VALUE; number++) {
          numbers.add(number);
        }
      }
    }, "Exhaust Memory Thread");

    runner.setDaemon(false);
    runner.setPriority(Thread.MAX_PRIORITY);
    runner.start();
  }

  public static void main(final String[] args) {
    runIntoTheGround();
    System.out.println("Press enter to exit!");
    Scanner in = new Scanner(System.in);
    in.next();
  }

}
