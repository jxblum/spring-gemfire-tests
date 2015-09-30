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

import com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.beans.TestBean;
import org.spring.data.gemfire.cache.asyncqueue.QueueAsyncEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The AsyncEventQueueWithListenerTest class is a test suite of test cases testing the circular references between
 * an Async Event Queue and a registered AsyncEventListener that refers back to the Async Event Queue on which the
 * listener is registered.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.beans.TestBean
 * @see org.spring.data.gemfire.cache.asyncqueue.QueueAsyncEventListener
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueue
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class AsyncEventQueueWithListenerTest {

  @Autowired
  private AsyncEventQueue queue;

  @Resource(name = "testBeanOne")
  private TestBean testBeanOne;

  @Test
  public void testAsyncEventQueueAndListenerConfiguration() {
    assertNotNull(queue);
    assertEquals("QueueOne", queue.getId());
    assertFalse(queue.isPersistent());
    assertFalse(queue.isParallel());
    assertEquals(50, queue.getMaximumQueueMemory());
    assertEquals(4, queue.getDispatcherThreads());
    assertTrue(queue.getAsyncEventListener() instanceof QueueAsyncEventListener);
    assertSame(queue, ((QueueAsyncEventListener) queue.getAsyncEventListener()).getQueue());
  }

  @Test
  public void testTestBeansConfiguration() {
    assertNotNull(testBeanOne);
    assertEquals("One", testBeanOne.getName());
    assertNotNull(testBeanOne.getTestBean());
    assertEquals("Two", testBeanOne.getTestBean().getName());
    assertSame(testBeanOne, testBeanOne.getTestBean().getTestBean());
  }

}
