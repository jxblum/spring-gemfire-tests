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

import java.util.Properties;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.distributed.DistributedSystem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The CacheInitializationTest class is a test suite of test cases testing the initialization of a GemFire Cache setting
 * attributes/properties having default values defined in the Spring Data GemFire XML Schema (XSD).
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see com.gemstone.gemfire.cache.Cache
 * @since 1.3.3 (Spring Data GemFire)
 * @since 7.0.1 (GemFire)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class CacheInitializationTest {

  @Autowired
  private Cache cache;

  @Test
  public void testCacheInitialization() {
    assertNotNull(cache);
    assertTrue(cache.getCopyOnRead());
    assertEquals(300, cache.getLockLease());
    assertEquals(120, cache.getLockTimeout());
    assertEquals(10, cache.getMessageSyncInterval());
    assertEquals(600, cache.getSearchTimeout());

    DistributedSystem distributedSystem = cache.getDistributedSystem();

    assertNotNull(distributedSystem);

    Properties distributionConfigProperties = distributedSystem.getProperties();

    assertEquals("cacheInitializationTest", distributionConfigProperties.getProperty("name"));
    assertEquals("localhost[11235]", distributionConfigProperties.getProperty("locators"));
    assertEquals("warning", distributionConfigProperties.getProperty("log-level"));
    assertEquals("0", distributionConfigProperties.getProperty("mcast-port"));
    assertEquals("localhost[11235]", distributionConfigProperties.getProperty("start-locator"));
  }

}
