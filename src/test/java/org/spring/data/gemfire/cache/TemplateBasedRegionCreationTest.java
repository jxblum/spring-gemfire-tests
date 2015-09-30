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
import com.gemstone.gemfire.cache.Region;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The TemplateBasedRegionCreationTest class is a test suite of test cases testing the contract and functionality
 * of the Region attributes templating in Spring Data GemFire XML configuration meta-data.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class TemplateBasedRegionCreationTest {

  @Resource(name = "RegionOne")
  private Region<String, Integer> regionOne;

  @Resource(name = "RegionTwo")
  private Region<String, Integer> regionTwo;

  protected void assertCommonRegionAttributes(final Region<String, Integer> region, final String expectedRegionName) {
    assertNotNull("The Region cannot be null!", region);
    assertNotNull("The RegionAttributes cannot be null!", region.getAttributes());
    assertEquals(expectedRegionName, region.getName());
    assertEquals("/" + expectedRegionName, region.getFullPath());
    assertTrue(region.getAttributes().getCloningEnabled());
    assertTrue(region.getAttributes().getConcurrencyChecksEnabled());
    assertTrue(region.getAttributes().isDiskSynchronous());
    //assertEquals(600, region.getAttributes().getEntryIdleTimeout().getTimeout());
    //assertEquals(600, region.getAttributes().getEntryTimeToLive().getTimeout());
    assertEquals(String.class, region.getAttributes().getKeyConstraint());
    assertEquals(1024, region.getAttributes().getInitialCapacity());
    assertEquals(new Float(0.85f), new Float(region.getAttributes().getLoadFactor()));
    //assertEquals(600, region.getAttributes().getRegionIdleTimeout().getTimeout());
    //assertEquals(600, region.getAttributes().getRegionTimeToLive().getTimeout());
    assertTrue(region.getAttributes().getStatisticsEnabled());
    assertEquals(Integer.class, region.getAttributes().getValueConstraint());
  }

  protected void assertPartitionRegionAttributes(final Region<String, Integer> region, final String expectedRegionName) {
    assertNotNull("The Region cannot be null!", region);
    assertNotNull("The RegionAttributes cannot be null!", region.getAttributes());
    assertEquals(DataPolicy.PARTITION, region.getAttributes().getDataPolicy());
    assertCommonRegionAttributes(region, expectedRegionName);
    assertNotNull("The PartitionAttributes cannot be null!", region.getAttributes().getPartitionAttributes());
    assertEquals(256, region.getAttributes().getPartitionAttributes().getLocalMaxMemory());
    assertEquals(2, region.getAttributes().getPartitionAttributes().getRedundantCopies());
    assertEquals(2048, region.getAttributes().getPartitionAttributes().getTotalMaxMemory());
    assertEquals(227, region.getAttributes().getPartitionAttributes().getTotalNumBuckets());
    assertEquals(2, region.getAttributes().getPartitionAttributes().getPartitionListeners().length);
  }

  protected void assertReplicatedRegionAttributes(final Region<String, Integer> region, final String expectedRegionName) {
    assertNotNull("The Region cannot be null!", region);
    assertNotNull("The RegionAttributes cannot be null!", region.getAttributes());
    assertEquals(DataPolicy.REPLICATE, region.getAttributes().getDataPolicy());
    assertCommonRegionAttributes(region, expectedRegionName);
  }

  @Test
  public void testRegionCreation() {
    assertReplicatedRegionAttributes(regionOne, "RegionOne");
    assertPartitionRegionAttributes(regionTwo, "RegionTwo");
  }

}
