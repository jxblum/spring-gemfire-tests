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

package org.pivotal.rti;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pivotal.rti.function.execution.RtiOnServersFunctionExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The SpringGemFireClientFunctionExecutionTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.pivotal.rti.function.execution.RtiOnServersFunctionExecution
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.5.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/org/pivotal/rti/rtiSpringGemFireFunctionClient.xml")
@SuppressWarnings("unused")
public class SpringGemFireClientFunctionExecutionTest {

  @Autowired
  private RtiOnServersFunctionExecution clientServerFunctionExecution;

  @Test
  public void testFunctionExecution() {
    String clientName = "me";

    String expected = String.format("hello '%1$s' from peer member function and hello '%1$s' from client-server function",
      clientName);
    //String invalidExpected = String.format("hello '%1$s' from client-server function", clientName);

    String actual = clientServerFunctionExecution.clientServerFunction(clientName);

    assertEquals(expected, actual);
  }

}
