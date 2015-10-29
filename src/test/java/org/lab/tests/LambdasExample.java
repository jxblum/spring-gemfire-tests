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
 * The LambdasExample class is an example Java application using Java 8 Lambdas.
 *
 * @author John Blum
 * @since 1.0.0
 */
public class LambdasExample {

  @SuppressWarnings("all")
  public static void main(final String[] args) {
    Calculator addition = (int x, int y) -> { return x + y; };

    System.out.printf("add(%1$d, %2$d) = %3$d%n", 3, 4, calculate(3, 4, addition));

    Calculator multiplication = new Calculator() {
      @Override public int calculate(final int leftOperand, final int rightOperand) {
        return (leftOperand * rightOperand);
      }
    };

    System.out.printf("multiplication(%1$d, %2$d) = %3$d%n", 3, 4, calculate(3, 4, multiplication));
  }

  private static int calculate(int leftOperand, int rightOperand, final Calculator calculator) {
    return calculator.calculate(leftOperand, rightOperand);
  }

  public interface Calculator {
    int calculate(int leftOperand, int rightOperand);
  }

}
