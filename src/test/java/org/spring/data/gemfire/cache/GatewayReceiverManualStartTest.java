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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.geode.cache.wan.GatewayReceiver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The GatewayReceiverManualStartTest class is a test suite of test cases testing the contract and functionality
 * of GemFire GatewayReceiver's configured with SDG XML configuration meta-data manual-start property.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.data.gemfire.wan.GatewayReceiverFactoryBean
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see org.apache.geode.cache.wan.GatewayReceiver
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class GatewayReceiverManualStartTest {

  @Autowired
  private GatewayReceiver gatewayReceiver;

  @Test
  public void testGatewayReceiverHasNotStarted() {
    assertNotNull("'GatewayReceiver' must not be null!", gatewayReceiver);
    assertEquals("localhost", gatewayReceiver.getHost());
    assertEquals(1234, gatewayReceiver.getStartPort());
    assertEquals(4321, gatewayReceiver.getEndPort());
    assertTrue(gatewayReceiver.isManualStart());
    assertFalse(gatewayReceiver.isRunning());
  }

}
