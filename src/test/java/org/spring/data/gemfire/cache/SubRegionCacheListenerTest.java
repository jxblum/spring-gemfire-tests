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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Stack;
import javax.annotation.Resource;

import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.util.CacheListenerAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The SubRegionCacheListenerTest class is a test suite of test cases testing the contract and functionality of GemFire
 * CacheListeners on peer Cache Sub-Regions.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see org.apache.geode.cache.CacheListener
 * @see org.apache.geode.cache.Region
 * @since 1.5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class SubRegionCacheListenerTest {

  private static final Stack<EntryEvent<Integer, String>> entryEvents = new Stack<>();

  @Resource(name = "/Parent/Child")
  private Region<Integer, String> child;

  @Test
  public void testCacheListenerCallback() {
    assertNotNull("The '/Parent/Child' Cache Sub-Region was not properly configured and initialized!", child);
    assertEquals("Child", child.getName());
    assertEquals("/Parent/Child", child.getFullPath());
    assertTrue(child.isEmpty());
    assertTrue(entryEvents.isEmpty());

    child.put(1, "TEST");

    assertFalse(child.isEmpty());
    assertEquals(1, child.size());
    assertEquals("TEST", child.get(1));
    assertFalse(entryEvents.isEmpty());

    EntryEvent event = entryEvents.pop();

    assertNotNull(event);
    assertEquals(1, event.getKey());
    assertNull(event.getOldValue());
    assertEquals("TEST", event.getNewValue());
    assertTrue(entryEvents.isEmpty());

    child.put(1, "TESTING");

    assertFalse(child.isEmpty());
    assertEquals(1, child.size());
    assertEquals("TESTING", child.get(1));
    assertFalse(entryEvents.isEmpty());

    event = entryEvents.pop();

    assertNotNull(event);
    assertEquals(1, event.getKey());
    assertEquals("TEST", event.getOldValue());
    assertEquals("TESTING", event.getNewValue());
    assertTrue(entryEvents.isEmpty());

    child.remove(1);

    assertTrue(child.isEmpty());

    event = entryEvents.pop();

    assertNotNull(event);
    assertEquals(1, event.getKey());
    assertEquals("TESTING", event.getOldValue());
    assertNull(event.getNewValue());
  }

  public static final class SubRegionCacheListener extends CacheListenerAdapter<Integer, String> {

    @Override
    public void afterCreate(final EntryEvent<Integer, String> event) {
      entryEvents.push(event);
    }

    @Override
    public void afterDestroy(final EntryEvent<Integer, String> event) {
      entryEvents.push(event);
    }

    @Override
    public void afterUpdate(final EntryEvent<Integer, String> event) {
      entryEvents.push(event);
    }
  }

}
