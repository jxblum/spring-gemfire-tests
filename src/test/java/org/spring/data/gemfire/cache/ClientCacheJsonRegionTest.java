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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireTest;
import org.spring.data.gemfire.app.beans.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The ClientCacheUsingJsonRegionTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.AbstractGemFireTest
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.4.0.BUILD-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class ClientCacheJsonRegionTest extends AbstractGemFireTest {

  @Autowired
  private ClientCache cache;

  @Resource(name = "Example")
  private Region<String, Object> example;

  @Test
  public void testPutGetJson() {
    Customer expectedJonDoe = new Customer("Jon", "Doe");

    example.put("1", expectedJonDoe);

    /*
    Customer actualJonDoe = (Customer) example.get("1");

    assertNotNull(actualJonDoe);
    assertNotSame(expectedJonDoe, actualJonDoe);
    assertEquals(expectedJonDoe, actualJonDoe);
    */

    Object actualJonDoe = example.get("1");

    assertTrue(actualJonDoe instanceof String);

    System.out.printf("Got value (%1$s) for key (1)!%n", actualJonDoe);

    Region<String, Object> exampleRegion = cache.getRegion("Example");

    assertNotNull(exampleRegion);
    assertEquals("Example", exampleRegion.getName());

    Object value = exampleRegion.get("1");

    assertNotNull(value);

    System.out.printf("Get value (%1$s) of type (%2$s)!%n", value, value.getClass());
  }

}
