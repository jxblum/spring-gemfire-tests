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

import static org.junit.Assert.*;

import java.util.List;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheClosedException;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEvent;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventListener;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueue;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueueFactory;
import com.gemstone.gemfire.cache.util.Gateway.OrderPolicy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

//import com.gemstone.gemfire.cache.wan.GatewaySender.OrderPolicy;

/**
 * The GemFireBasedParallelAsyncEventQueueTest class is a test suite of test cases testing the contract
 *
 * @author John Blum
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueue
 * @since 1.7.0
 */
public class GemFireBasedParallelAsyncEventQueueTest {

  private AsyncEventQueue eventQueue;

  @Before
  public void setup() throws Exception {
    try {
      setupGemFireTestFixtures();
    }
    catch (CacheClosedException ignore) {
      setupGemFire();
    }
  }

  protected void setupGemFireTestFixtures() {
    Cache gemfireCache = CacheFactory.getAnyInstance();

    eventQueue = gemfireCache.getAsyncEventQueue("TestEventQueue");
  }

  protected void setupGemFire() {
    Cache gemfireCache = new CacheFactory()
      .set("name", getClass().getSimpleName())
      .set("mcast-port", "0")
      .set("log-level", "config")
      .create();

    AsyncEventQueueFactory eventQueueFactory = gemfireCache.createAsyncEventQueueFactory();

    eventQueueFactory.setBatchConflationEnabled(true);
    eventQueueFactory.setBatchSize(20);
    eventQueueFactory.setBatchTimeInterval(500);
    eventQueueFactory.setDispatcherThreads(2);
    eventQueueFactory.setMaximumQueueMemory(100);
    eventQueueFactory.setOrderPolicy(OrderPolicy.KEY);
    eventQueueFactory.setParallel(true);
    eventQueueFactory.setPersistent(false);

    eventQueue = eventQueueFactory.create("TestEventQueue", new TestAsyncEventListener());
  }

  @After
  public void tearDown() {
    try {
      CacheFactory.getAnyInstance().close();
    }
    catch (CacheClosedException ignore) {
    }
  }

  protected AsyncEventQueue getEventQueue() {
    Assert.state(eventQueue != null, "The 'AsyncEventQueue' was not properly initialized!");
    return eventQueue;
  }

  @Test
  public void testAsyncEventQueueConfiguration() {
    AsyncEventQueue localEventQueue = getEventQueue();

    assertTrue(localEventQueue.getAsyncEventListener() instanceof TestAsyncEventListener);
    assertTrue(localEventQueue.isBatchConflationEnabled());
    assertEquals(20, localEventQueue.getBatchSize());
    assertEquals(500, localEventQueue.getBatchTimeInterval());
    assertEquals(2, localEventQueue.getDispatcherThreads());
    assertEquals(100, localEventQueue.getMaximumQueueMemory());
    assertEquals(OrderPolicy.KEY, localEventQueue.getOrderPolicy());
    assertTrue(localEventQueue.isParallel());
    assertFalse(localEventQueue.isPersistent());
  }

  protected static class TestAsyncEventListener implements AsyncEventListener {

    @Override
    public boolean processEvents(final List<AsyncEvent> events) {
      throw new UnsupportedOperationException("Not Implemented!");
    }

    @Override
    public void close() {
    }
  }

}
