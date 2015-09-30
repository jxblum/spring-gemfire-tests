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

package org.pivotal.gemfire.cache.client;

import static org.junit.Assert.*;

import java.util.Properties;
import javax.annotation.Resource;

import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The ClientCacheLocalRegionUpdateTest class is a test suite of test cases testing the behavior of updating a local,
 * client Region and it's affect on a corresponding GemFire Server Region of a similar name when the local, client
 * Region both specifies and does not specify a GemFire Pool.
 *
 * @author John Blum
 * @see com.gemstone.gemfire.cache.client.ClientCache
 * @see com.gemstone.gemfire.cache.Region
 * @since 1.0.0
 */
public class ClientCacheLocalRegionUpdateTest {

  protected static final String LOCAL_CLIENT_REGION_NAME = "Example";

  @Autowired
  private ClientCache clientCache;

  @Resource(name = LOCAL_CLIENT_REGION_NAME)
  private Region<Long, Integer> example;

  @Before
  public void setup() {
    if (clientCache == null) {
      Properties gemfireProperties = new Properties();

      gemfireProperties.setProperty("name", "LocalOnlyClientCache");
      gemfireProperties.setProperty("log-level", "config");

      clientCache = new ClientCacheFactory(gemfireProperties)
        .set(DistributionConfig.CACHE_XML_FILE_NAME, "local-only-client-region-cache.xml")
        .create();

      assertNotNull("The GemFire 'ClientCache' was not properly configured and initialized!", clientCache);
      assertFalse("The GemFire 'ClientCache' was unexpectedly closed!", clientCache.isClosed());
    }

    example = (example != null ? example : clientCache.<Long, Integer>getRegion(LOCAL_CLIENT_REGION_NAME));

    assertNotNull("The GemFire LOCAL-only, client Region '/Example' was not found!", example);
    assertEquals("Example", example.getName());
    assertEquals("/Example", example.getFullPath());
    assertNotNull(example.getAttributes());
    assertEquals(DataPolicy.NORMAL, example.getAttributes().getDataPolicy());
    assertNull(String.format("Expected null; but was (%1$s)!", example.getAttributes().getPoolName()),
      example.getAttributes().getPoolName());
  }

  @After
  public void tearDown() {
    clientCache.close();
    clientCache = null;
  }

  protected int intValue(final Integer value) {
    return (value != null ? value : 0);
  }

  @Test
  public void testLocalClientRegionUpdate() {
    int count = intValue(example.get(1l));
    assertEquals(0, count);
    assertNull(example.put(1l, ++count));
    assertEquals(1, intValue(example.get(1l)));
  }

}
