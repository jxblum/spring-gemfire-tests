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

import javax.annotation.Resource;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The PartitionedRegionPropertyPlaceholderTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @since 1.3.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class PartitionRegionWithPropertyPlaceholdersTest extends AbstractGemFireTest {

  @Resource(name = "Tagger")
  private Region taggerRegion;

  @Autowired
  private TestBean bean;

  @Test
  public void testPartitionedRegionAttributes() {
    assertNotNull(taggerRegion);

    // Standard Region Attributes
    assertNotNull(taggerRegion.getAttributes());
    assertEquals(DataPolicy.PERSISTENT_PARTITION, taggerRegion.getAttributes().getDataPolicy());
    assertFalse(taggerRegion.getAttributes().getMulticastEnabled());

    // Partitioned Region Attributes
    assertNotNull(taggerRegion.getAttributes().getPartitionAttributes());
    assertEquals(1000, taggerRegion.getAttributes().getPartitionAttributes().getRecoveryDelay());
    assertEquals(2, taggerRegion.getAttributes().getPartitionAttributes().getRedundantCopies());
  }

  @Test
  public void testBeanReplicationProperty() {
    assertNotNull(bean);
    assertEquals(2, bean.getReplication());
  }

  public static class TestBean {

    private int replication;

    public int getReplication() {
      return replication;
    }

    public void setReplication(final int replication) {
      this.replication = replication;
    }
  }

}
