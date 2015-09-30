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

package org.spring.data.gemfire.cache.execute;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.gemfire.LazyWiringDeclarableSupport;

/**
 * The CalculatorFunction class is a GemFire/Geode Function that performs a general mathematical calculation
 * determined by the Spring configured/managed Calculator bean.
 *
 * @author John Blum
 * @see org.spring.data.gemfire.cache.execute.Calculator
 * @see org.springframework.data.gemfire.LazyWiringDeclarableSupport
 * @see com.gemstone.gemfire.cache.execute.Function
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class CalculatorFunction extends LazyWiringDeclarableSupport implements Function, Calculator {

  public static final String ID = "calculate";

  @Autowired
  @Qualifier("addition")
  private Calculator calculator;

  protected static void assertState(boolean valid, String message, Object... arguments) {
    if (!valid) {
      throw new IllegalArgumentException(String.format(message, arguments));
    }
  }

  protected Calculator getCalculator() {
    assertState(calculator != null, "Calculator was not properly initialized");
    return calculator;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public Integer calculate(final Integer operandOne, final Integer operandTwo) {
    return getCalculator().calculate(operandOne, operandTwo);
  }

  @Override
  public void execute(final FunctionContext context) {
    try {
      Object[] arguments = (Object[]) context.getArguments();

      Integer operandOne = (Integer) arguments[0];
      Integer operandTwo = (Integer) arguments[1];

      // calculate the result using operandOne and operandTwo based on the "operation" defined
      // by the autowired/injected Spring configured and managed Calculator bean
      Integer result = calculate(operandOne, operandTwo);

      context.getResultSender().lastResult(result);
    }
    catch (Exception e) {
      context.getResultSender().sendException(e);
    }
  }

  @Override
  public boolean hasResult() {
    return true;
  }

  @Override
  public boolean isHA() {
    return false;
  }

  @Override
  public boolean optimizeForWrite() {
    return false;
  }

}
