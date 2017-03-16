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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.EvictionAction;
import org.apache.geode.cache.EvictionAlgorithm;
import org.apache.geode.cache.EvictionAttributes;
import org.apache.geode.cache.ExpirationAction;
import org.apache.geode.cache.ExpirationAttributes;
import org.apache.geode.cache.PartitionAttributes;
import org.apache.geode.cache.Region;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.cache.SpringGemFireJavaConfigTest.ApplicationConfiguration;
import org.spring.data.gemfire.config.GemFireConfiguration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The SpringGemFireJavaConfigTest class is a test suite of test cases testing Spring GemFire configuration using
 * Spring's JavaConfig approach.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfiguration.class)
//@ContextConfiguration(classes = GemFireConfiguration.class)
@SuppressWarnings("unused")
public class SpringGemFireJavaConfigTest {

  @Autowired
  private ApplicationDao appDao;

  @Resource(name = "&ExampleLocal")
  private FactoryBean<Region<?, ?>> exampleLocalRegionFactoryBean;

  @Resource(name = "ExampleLocal")
  private Region<Long, String> exampleLocal;

  @Resource(name = "ExampleLocalAlias")
  private Region<Long, String> exampleLocalAlias;

  @Resource(name = "ExampleEvictionLocal")
  private Region<Long, String> exampleEvictionLocal;

  @Resource(name = "ExamplePartition")
  private Region<Object, Object> examplePartition;

  @Autowired
  private GemfireTemplate exampleLocalTemplate;

  protected void assertEvictionAttributes(final EvictionAttributes evictionAttributes,
                                          final EvictionAction expectedAction,
                                          final EvictionAlgorithm expectedAlgorithm,
                                          final int expectedMaximum)
  {
    assertNotNull("EvictionAttributes must not be null!", evictionAttributes);
    assertEquals(expectedAction, evictionAttributes.getAction());
    assertEquals(expectedAlgorithm, evictionAttributes.getAlgorithm());
    assertEquals(expectedMaximum, evictionAttributes.getMaximum());
  }

  protected void assertExpirationAttributes(ExpirationAttributes expirationAttributes,
    ExpirationAction expectedAction, int expectedTimeout) {
    assertNotNull("ExpirationAttributes must not be null!", expirationAttributes);
    assertEquals(expectedAction, expirationAttributes.getAction());
    assertEquals(expectedTimeout, expirationAttributes.getTimeout());
  }

	protected void assertPartitionAttributes(final PartitionAttributes partitionAttributes,
                                           final int expectedLocalMaxMemory,
                                           final int expectedTotalMaxMemory)
  {
    assertNotNull("PartitionAttributes must not be null!", partitionAttributes);
    assertEquals(expectedLocalMaxMemory, partitionAttributes.getLocalMaxMemory());
    assertEquals(expectedTotalMaxMemory, partitionAttributes.getTotalMaxMemory());
  }

  protected void assertRegion(final Region<?, ?> region, final String expectedName, final DataPolicy expectedDataPolicy) {
    assertRegion(region, expectedName, String.format("%1$s%2$s", Region.SEPARATOR, expectedName), expectedDataPolicy);
  }

  protected void assertRegion(final Region<?, ?> region, final String expectedName, final String expectedPath, final DataPolicy expectedDataPolicy) {
    assertNotNull(String.format("Region '%1$s' was not properly configured and initialized!", expectedPath), region);
    assertEquals(expectedName, region.getName());
    assertEquals(expectedPath, region.getFullPath());
    assertNotNull(region.getAttributes());
    assertEquals(expectedDataPolicy, region.getAttributes().getDataPolicy());
  }

  @Test
  public void exampleLocalRegionFactoryBeanReference() {
    assertTrue(exampleLocalRegionFactoryBean instanceof LocalRegionFactoryBean);
  }

  @Test
  public void exampleLocalRegionConfiguration() {
    assertRegion(exampleLocal, "ExampleLocal", DataPolicy.NORMAL);
  }

  @Test
  public void exampleEvictionLocalRegionConfiguration() {
    assertRegion(exampleEvictionLocal, "ExampleEvictionLocal", DataPolicy.NORMAL);
    assertEvictionAttributes(exampleEvictionLocal.getAttributes().getEvictionAttributes(),
      EvictionAction.LOCAL_DESTROY, EvictionAlgorithm.LRU_MEMORY, 4096);
  }

  @Test
  public void exampleLocalRegionDataAccess() {
    assertSame(exampleLocal, exampleLocalTemplate.getRegion());
    assertTrue(exampleLocal.isEmpty());

    exampleLocalTemplate.put(1, "ONE");
    exampleLocalTemplate.put(2, "TWO");
    exampleLocalTemplate.put(3, "THREE");

    assertFalse(exampleLocal.isEmpty());
    assertEquals(3, exampleLocal.size());
    assertEquals("ONE", exampleLocal.get(1));
    assertEquals("TWO", exampleLocal.get(2));
    assertEquals("THREE", exampleLocal.get(3));
  }

  @Test
  public void examplePartitionRegionConfiguration() {
    assertRegion(examplePartition, "ExamplePartition", DataPolicy.PARTITION);
    assertPartitionAttributes(examplePartition.getAttributes().getPartitionAttributes(), 16384, 32768);
    // NOTE basically, GemFire does not care what you explicitly set for the LRU_MEMORY threshold (a.k.a. maximum),
    // it knows better (puh!!!) and so sets it to the 'local-max-memory' setting on the PartitionAttributes, unless
    // 'local-max-memory' is not explicitly set then it "calculates" it to 90% of the JVM heap memory (argh!!!)
    // So, much for "fail-fast" and explicitly letting the user know the user-defined setting for LRU_MEMORY threshold
    // (err... maximum) was not honored and silently ignore it instead... ridiculous!
    assertEvictionAttributes(examplePartition.getAttributes().getEvictionAttributes(), EvictionAction.LOCAL_DESTROY,
      EvictionAlgorithm.LRU_MEMORY, examplePartition.getAttributes().getEvictionAttributes().getMaximum());
  }

  @Test
  public void applicationDaoHasExampleLocalRegionReference() {
    assertSame(exampleLocal, appDao.getExampleLocal());
  }

  @Test
  public void exampleLocalRegionAlias() {
    assertNotNull(exampleLocalAlias);
  }

  @Configuration
  @Import(GemFireConfiguration.class)
  public static class ApplicationConfiguration {

    @Resource(name = "ExampleLocal")
    private Region<Long, String> exampleLocal;

    @Bean
    public ApplicationDao appDao() {
      return new ApplicationDao();
    }

    @Bean(name = "ExampleLocalAlias")
    public Region<Long, String> exampleLocalAlias() {
      return exampleLocal;
    }
  }

  @Component
  public static final class ApplicationDao {

    private Region<Long, String> exampleLocal;

    public Region<Long, String> getExampleLocal() {
      return exampleLocal;
    }

    @Resource(name = "ExampleLocal")
    public void setExampleLocal(final Region<Long, String> exampleLocal) {
      this.exampleLocal = exampleLocal;
    }
  }

}
