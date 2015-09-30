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

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEvent;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueue;

import org.junit.runner.RunWith;
import org.pivotal.gemfire.cache.GemFireBasedSerialAsyncEventQueueTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The SerialAsyncEventQueueTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueue
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class SerialAsyncEventQueueTest extends GemFireBasedSerialAsyncEventQueueTest {

  @Autowired
  private AsyncEventQueue eventQueue;

  @Resource(name = "EventsRegion")
  private Region<String, AsyncEvent> eventsRegion;

  @Resource(name = "TestRegion")
  private Region<String, String> testRegion;

  @Override
  public void setup() throws Exception {
  }

  @Override
  protected AsyncEventQueue getEventQueue() {
    return eventQueue;
  }

  @Override
  protected Region<String, AsyncEvent> getEventsRegion() {
    return eventsRegion;
  }

  @Override
  protected Region<String, String> getTestRegion() {
    return testRegion;
  }

}
