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

package org.pivotal.gemfire.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spring.data.gemfire.support.CacheUtils;

/**
 * The CacheRegionSubRegionTest class is a test suite of test cases testing GemFire's Cache Region/Sub-Region
 * functionality.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.CacheFactory
 * @see org.apache.geode.cache.Region
 * @since 7.0.1
 */
public class CacheSubRegionTest {

  private Cache gemfireCache;

  @Before
  public void setup() {
    gemfireCache = new CacheFactory()
      .set("cache-xml-file", "subregion-cache.xml")
      .set("locators", "localhost[11235]")
      .set("log-level", "config")
      .set("mcast-port", "0")
      .set("start-locator", "localhost[11235]")
      .create();

    assertNotNull("The GemFire Cache was not properly initialized!", gemfireCache);
  }

  @After
  public void tearDown() {
    CacheUtils.close(gemfireCache);
  }

  @Test
  public void testCacheSubRegionCreationAndAccess() {
    Region parent = gemfireCache.getRegion("Parent");

    assertNotNull(parent);
    assertEquals("Parent", parent.getName());
    assertEquals("/Parent", parent.getFullPath());
    assertSame(parent, gemfireCache.getRegion("/Parent"));

    Region child = parent.getSubregion("Child");

    assertNotNull(child);
    assertEquals("Child", child.getName());
    assertEquals("/Parent/Child", child.getFullPath());
    assertSame(child, gemfireCache.getRegion("/Parent/Child"));
    assertSame(child, gemfireCache.getRegion("Parent/Child"));
    assertNull(gemfireCache.getRegion("Child"));

    Region grandchild = child.getSubregion("Grandchild");

    assertNotNull(grandchild);
    assertEquals("Grandchild", grandchild.getName());
    assertEquals("/Parent/Child/Grandchild", grandchild.getFullPath());
    assertSame(grandchild, gemfireCache.getRegion("/Parent/Child/Grandchild"));
    assertSame(grandchild, gemfireCache.getRegion("Parent/Child/Grandchild"));
    assertNull(gemfireCache.getRegion("../Child/Grandchild"));
    assertNull(gemfireCache.getRegion("Child/Grandchild"));
    assertNull(gemfireCache.getRegion("Grandchild"));
    //assertSame(grandchild, parent.getSubregion("/Child/Grandchild")); // ASSERTION FAILED!
    assertSame(grandchild, parent.getSubregion("Child/Grandchild"));
    assertNull(parent.getSubregion("Grandchild"));
  }

}
