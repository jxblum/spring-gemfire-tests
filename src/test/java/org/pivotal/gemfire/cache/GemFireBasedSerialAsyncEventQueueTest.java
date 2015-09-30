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

import static org.codeprimate.lang.concurrent.ThreadUtils.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheClosedException;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEvent;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventListener;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueue;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueueFactory;
import com.gemstone.gemfire.cache.util.Gateway.OrderPolicy;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

//import com.gemstone.gemfire.cache.wan.GatewaySender.OrderPolicy;

/**
 * The GemFireBasedSerialAsyncEventQueueTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueue
 * @since 1.0.0
 */
public class GemFireBasedSerialAsyncEventQueueTest {

  protected static final int DEFAULT_REGION_PUT_OP_COUNT = 200;

  private AsyncEventQueue eventQueue;

  private Region<String, AsyncEvent> eventsRegion;
  private Region<String, String> testRegion;

  @Before
  public void setup() throws Exception {
    try {
      setupGemFireTestFixtures();
    }
    catch (CacheClosedException ignore) {
      setupGemFire();
    }
  }

  private void setupGemFireTestFixtures() {
    Cache gemfireCache = CacheFactory.getAnyInstance();

    eventQueue = gemfireCache.getAsyncEventQueue("TestQueue");
    eventsRegion = gemfireCache.getRegion("/EventsRegion");
    testRegion = gemfireCache.getRegion("/TestRegion");
  }

  protected void setupGemFire() {
    Cache gemfireCache = new CacheFactory()
      .set("name", getClass().getSimpleName())
      .set("mcast-port", "0")
      .set("log-level", "config")
      .create();

    RegionFactory<String, AsyncEvent> eventsRegionFactory = gemfireCache.createRegionFactory();

    eventsRegionFactory.setDataPolicy(DataPolicy.PARTITION);
    eventsRegionFactory.setKeyConstraint(String.class);
    eventsRegionFactory.setValueConstraint(AsyncEvent.class);

    eventsRegion = eventsRegionFactory.create("EventsRegion");

    TestAsyncEventListener eventListener = new TestAsyncEventListener();

    eventListener.setEventsRegion(eventsRegion);

    AsyncEventQueueFactory eventQueueFactory = gemfireCache.createAsyncEventQueueFactory();

    eventQueueFactory.setBatchConflationEnabled(true);
    eventQueueFactory.setBatchSize(20);
    eventQueueFactory.setBatchTimeInterval(500);
    eventQueueFactory.setDispatcherThreads(8);
    eventQueueFactory.setMaximumQueueMemory(100);
    //queueFactory.setOrderPolicy(OrderPolicy.KEY);
    eventQueueFactory.setParallel(false);
    eventQueueFactory.setPersistent(false);

    eventQueue = eventQueueFactory.create("TestQueue", eventListener);

    RegionFactory<String, String> testRegionFactory = gemfireCache.createRegionFactory();

    testRegionFactory.setDataPolicy(DataPolicy.PARTITION);
    testRegionFactory.setKeyConstraint(String.class);
    testRegionFactory.setValueConstraint(String.class);
    testRegionFactory.addAsyncEventQueueId(eventQueue.getId());

    testRegion = testRegionFactory.create("TestRegion");

    tearDownGemFire();
  }

  protected void tearDownGemFire() {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override public void run() {
        try {
          CacheFactory.getAnyInstance().close();
        }
        catch (CacheClosedException ignore) {
        }
      }
    }));
  }

  protected AsyncEventQueue getEventQueue() {
    Assert.state(eventQueue != null, "The 'EventQueue' was not properly configured and initialized!");
    return eventQueue;
  }

  protected Region<String, AsyncEvent> getEventsRegion() {
    Assert.state(eventsRegion != null, "The 'EventsRegion' was not properly configured and initialized!");
    return eventsRegion;
  }

  protected int getRegionPutOpCount() {
    return DEFAULT_REGION_PUT_OP_COUNT;
  }

  protected Region<String, String> getTestRegion() {
    Assert.state(testRegion != null, "The 'TestRegion' was not properly configured and initialized!");
    return testRegion;
  }

  @Test
  public void testAsyncEventQueueConfiguration() {
    assertNotNull("The 'AsyncEventQueue' was not properly configured and initialized!", getEventQueue());
    assertTrue(getEventQueue().isBatchConflationEnabled());
    assertEquals(20, getEventQueue().getBatchSize());
    assertEquals(500, getEventQueue().getBatchTimeInterval());
    assertEquals(8, getEventQueue().getDispatcherThreads());
    assertEquals(100, getEventQueue().getMaximumQueueMemory());
    assertEquals(OrderPolicy.KEY, getEventQueue().getOrderPolicy());
    assertFalse(getEventQueue().isParallel());
    assertFalse(getEventQueue().isPersistent());
  }

  @Test
  public void testAsyncEventQueueFunction() {
    for (int index = 0; index < getRegionPutOpCount(); index++) {
      getTestRegion().put("key" + index, "value" + index);
    }

    waitFor(5, TimeUnit.SECONDS).checkEvery(500, TimeUnit.MILLISECONDS).on(new CompletableTask() {
      @Override public boolean isComplete() {
        return (DEFAULT_REGION_PUT_OP_COUNT == getEventsRegion().size());
      }
    });

    for (int index = 0; index < getRegionPutOpCount(); index++) {
      assertEquals("value" + index, getEventsRegion().get("key" + index).getDeserializedValue());
    }
  }

  public static class TestAsyncEventListener implements AsyncEventListener {

    private Region<String, AsyncEvent> eventsRegion;

    @Resource(name = "EventsRegion")
    public final void setEventsRegion(final Region<String, AsyncEvent> events) {
      this.eventsRegion = events;
    }

    protected Region<String, AsyncEvent> getEventsRegion() {
      Assert.state(eventsRegion != null, "The 'EventsRegion' was not properly configured!");
      return eventsRegion;
    }

    @Override
    public boolean processEvents(final List<AsyncEvent> events) {
      for (AsyncEvent event : events) {
        log(event);
        getEventsRegion().put(event.getKey().toString(), event);
      }

      return true;
    }

    protected void log(final AsyncEvent event) {
      System.out.printf("{ operation = %1$s, key = %2$s, value = %3$s }%n", event.getOperation(), event.getKey(),
        event.getDeserializedValue());
    }

    @Override
    public void close() {
    }
  }

}
