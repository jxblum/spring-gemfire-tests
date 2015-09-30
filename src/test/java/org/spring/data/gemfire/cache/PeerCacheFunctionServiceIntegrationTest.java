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

package org.spring.data.gemfire.cache;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.gemfire.function.execution.GemfireOnRegionFunctionTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The PeerCacheFunctionServiceIntegrationTest class is a test suite of test cases testing the contract
 * and functionality of GemFire Region Function Executions using SDG's FunctionService to defined a Function.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.FunctionServiceFactoryBean
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class PeerCacheFunctionServiceIntegrationTest {

  @Resource(name = "Factorials")
  private Region<Integer, Integer> factorials;

  @Before
  public void setup() {
    factorials.put(0, 0);
    factorials.put(1, 0);
    factorials.put(2, 0);
    factorials.put(3, 0);
    factorials.put(4, 0);
    factorials.put(5, 0);
    factorials.put(6, 0);
    factorials.put(7, 0);
    factorials.put(8, 0);
    factorials.put(9, 0);
  }

  @Test
  public void factorialFunctionComputation() {
    for (int index = 0; index < 10; index++) {
      assertEquals(0, factorials.get(index).intValue());
    }

    GemfireOnRegionFunctionTemplate functionTemplate = new GemfireOnRegionFunctionTemplate(factorials);

    functionTemplate.execute(FactorialFunction.ID);

    assertEquals(1, factorials.get(0).intValue());
    assertEquals(1, factorials.get(1).intValue());
    assertEquals(2, factorials.get(2).intValue());
    assertEquals(6, factorials.get(3).intValue());
    assertEquals(24, factorials.get(4).intValue());
    assertEquals(120, factorials.get(5).intValue());
    assertEquals(720, factorials.get(6).intValue());
    assertEquals(5040, factorials.get(7).intValue());
    assertEquals(40320, factorials.get(8).intValue());
    assertEquals(362880, factorials.get(9).intValue());
  }

  public static class FactorialFunction extends FunctionAdapter {

    public static final String ID = FactorialFunction.class.getSimpleName();

    @Override
    public String getId() {
      return ID;
    }

    @Override
    public void execute(final FunctionContext context) {
      if (context instanceof RegionFunctionContext) {
        Region<Integer, Integer> factorials = ((RegionFunctionContext) context).getDataSet();

        for (Integer key : factorials.keySet()) {
          factorials.put(key, factorial(key));
        }
      }

      context.getResultSender().lastResult(null);
    }

    protected int factorial(Integer value) {
      return factorial(value == null ? 0 : Math.abs(value.intValue()));
    }

    protected int factorial(int value) {
      if (value < 1) {
        return 1;
      }

      return (value * factorial(value - 1));
    }
  }

}
