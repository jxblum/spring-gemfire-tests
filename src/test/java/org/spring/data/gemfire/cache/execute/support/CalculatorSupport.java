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

package org.spring.data.gemfire.cache.execute.support;

import org.spring.data.gemfire.cache.execute.Calculator;

/**
 * The CalculatorSupport class is a abstract base class containing calculator operations.
 *
 * @author John Blum
 * @see org.spring.data.gemfire.cache.execute.Calculator
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class CalculatorSupport implements Calculator {

  public static final Calculator ADDITION_CALCULATOR = new AdditionCalculator();
  public static final Calculator DIVISION_CALCULATOR = new DivisionCalculator();
  public static final Calculator EXPONENTIAL_CALCULATOR = new ExponentialCalculator();
  public static final Calculator MULTIPLICATION_CALCULATOR = new MultiplicationCalculator();
  public static final Calculator SUBTRACTION_CALCULATOR = new SubtractionCalculator();

  public static Calculator addition() {
    return ADDITION_CALCULATOR;
  }

  public static Calculator division() {
    return DIVISION_CALCULATOR;
  }

  public static Calculator exponential() {
    return EXPONENTIAL_CALCULATOR;
  }

  public static Calculator multiplication() {
    return MULTIPLICATION_CALCULATOR;
  }

  public static Calculator subtraction() {
    return SUBTRACTION_CALCULATOR;
  }

  public static class AdditionCalculator extends CalculatorSupport {
    @Override public Integer calculate(final Integer leftOperand, final Integer rightOperand) {
      return (leftOperand + rightOperand);
    }
  }

  public static class DivisionCalculator extends CalculatorSupport {
    @Override public Integer calculate(final Integer leftOperand, final Integer rightOperand) {
      return (leftOperand / rightOperand);
    }
  }

  public static class ExponentialCalculator extends CalculatorSupport {
    @Override public Integer calculate(final Integer operand, final Integer exponent) {
      return Double.valueOf(Math.pow(operand, exponent)).intValue();
    }
  }

  public static class MultiplicationCalculator extends CalculatorSupport {
    @Override public Integer calculate(final Integer leftOperand, final Integer rightOperand) {
      return (leftOperand * rightOperand);
    }
  }

  public static class SubtractionCalculator extends CalculatorSupport {
    @Override public Integer calculate(final Integer leftOperand, final Integer rightOperand) {
      return (leftOperand - rightOperand);
    }
  }

}
