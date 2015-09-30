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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireTest;
import org.spring.data.gemfire.cache.partition.TestPartitionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The PartitionRegionWithPartitionListenerTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.spring.data.gemfire.AbstractGemFireTest
 * @since 1.3.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class PartitionRegionWithPartitionListenerTest extends AbstractGemFireTest {

  @Resource(name = "AppData")
  private Region region;

  @Test
  public void testRegionConfiguration() {
    assertNotNull(region);
    assertEquals("AppData", region.getName());
    assertNotNull(region.getAttributes().getPartitionAttributes().getPartitionListeners());
    assertEquals(1, region.getAttributes().getPartitionAttributes().getPartitionListeners().length);
    assertTrue(region.getAttributes().getPartitionAttributes().getPartitionListeners()[0]
      instanceof TestPartitionListener);
  }

}
