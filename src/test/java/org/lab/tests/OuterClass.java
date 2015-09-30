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

/**
 * The OuterClass class is an example of how to construct an inner "instance" class of OuterClass.
 *
 * @author John Blum
 * @since 1.0.0
 */
public class OuterClass {

  public static void main(final String[] args) {
    Object instance = new OuterClass().new InnerClass();

    System.out.printf("DEBUG (%1$s)%n", instance);

    assert InnerClass.class.getName().equals(instance) : String.format("Expected (%1$s); but was (%2$s)!",
      InnerClass.class.getName(), instance);

    System.out.println("PASSED!");
  }

  @Override
  public String toString() {
    return OuterClass.class.getName();
  }

  public class InnerClass extends OuterClass {

    @Override
    public String toString() {
      return InnerClass.class.getName();
    }
  }

}
