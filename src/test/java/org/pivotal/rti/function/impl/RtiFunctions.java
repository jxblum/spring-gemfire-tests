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

package org.pivotal.rti.function.impl;

import org.pivotal.rti.function.execution.RtiOnMembersFunctionExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;

/**
 * The RtiFunctions class...
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.function.annotation.GemfireFunction
 * @since 1.5.1
 */
@SuppressWarnings("unused")
public class RtiFunctions {

  @Autowired
  private RtiOnMembersFunctionExecution peerMemberFunctionExecution;

  @GemfireFunction
  public String peerMemberFunction(final String clientName) {
    return String.format("hello '%1$s' from peer member function", clientName);
  }

  @GemfireFunction
  public String clientServerFunction(final String clientName) {
    return String.format("%1$s and hello '%2$s' from client-server function",
      peerMemberFunctionExecution.peerMemberFunction(clientName), clientName);
  }

}
