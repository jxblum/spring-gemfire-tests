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

import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.EvictionAction;
import com.gemstone.gemfire.cache.EvictionAlgorithm;
import com.gemstone.gemfire.cache.Region;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The PersistentPartitionRegionTemplateTest class is a test suite of test cases testing the functionality of
 * Spring Data GemFire's Region templates with a 'persistent', PARTITION Region configuration.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.data.gemfire.PartitionedRegionFactoryBean
 * @link https://jira.spring.io/browse/SGF-384
 * @since 1.6.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class PersistentPartitionRegionTemplateTest {

  @Resource(name = "Example")
  private Region<?, ?> example;

  @Test
  public void testExampleTemplatedPersistentPartitionRegion() {
    assertNotNull("The '/Example' PARTITION Region was not properly configured and initialized!", example);
    assertEquals("Example", example.getName());
    assertEquals("/Example", example.getFullPath());
    assertNotNull(example.getAttributes());
    assertEquals(DataPolicy.PERSISTENT_PARTITION, example.getAttributes().getDataPolicy());
    assertNotNull(example.getAttributes().getEvictionAttributes());
    assertEquals(EvictionAlgorithm.LRU_HEAP, example.getAttributes().getEvictionAttributes().getAlgorithm());
    assertEquals(EvictionAction.OVERFLOW_TO_DISK, example.getAttributes().getEvictionAttributes().getAction());
    assertNotNull(example.getAttributes().getPartitionAttributes());
    assertEquals(1, example.getAttributes().getPartitionAttributes().getRedundantCopies());
    assertEquals(163, example.getAttributes().getPartitionAttributes().getTotalNumBuckets());
  }

}
