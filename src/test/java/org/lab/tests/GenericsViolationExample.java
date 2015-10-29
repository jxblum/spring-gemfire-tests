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

import java.util.ArrayList;
import java.util.List;

/**
 * The GenericsViolationExample class is an example Java application using Generics and demonstrating how the Generic
 * type signature can be violated.
 *
 * @author John Blum
 * @since 1.0.0
 */
public class GenericsViolationExample {

  @SuppressWarnings("all")
  public static void main(final String[] args) {
    List<String> strings = new ArrayList<String>();

    strings.add("one");
    strings.add("two");
    strings.add("three");

    List numbers = strings;

    numbers.add(4);
    numbers.add(5);
    numbers.add(6);

    System.out.printf("List is (%1$s)%n", strings);
  }
}
