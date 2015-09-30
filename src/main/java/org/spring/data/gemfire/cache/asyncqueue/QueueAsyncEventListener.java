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

package org.spring.data.gemfire.cache.asyncqueue;

import java.util.List;
import javax.annotation.PostConstruct;

import com.gemstone.gemfire.cache.asyncqueue.AsyncEvent;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventListener;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * The QueueAsyncEventListener class is an implementation of the AsyncEventListener interface that contains a reference
 * to the AsyncEventQueue upon which it is registered.
 *
 * @author John Blum
 * @see com.gemstone.gemfire.cache.asyncqueue.AsyncEvent
 * @see com.gemstone.gemfire.cache.asyncqueue.AsyncEventListener
 * @see com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueue
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class QueueAsyncEventListener implements AsyncEventListener {

  @Autowired
  private AsyncEventQueue queue;

  public QueueAsyncEventListener() {
    this.queue = null;
  }

  // TODO comment out both the @Autowired annotated 'queue' instance field injection and the @Autowired annotated
  // 'setQueue' method (setter) injection, and then uncomment the @Autowired annotated constructor to reproduce
  // the BeanCurrentlyInCreationException...
  // NOTE DO NOT USE Spring's CONSTRUCTOR DEPENDENCY INJECTION (DI) WHEN THERE IS A CIRCULAR REFERENCE!
  //@Autowired
  public QueueAsyncEventListener(final AsyncEventQueue queue) {
    this.queue = queue;
  }

  @PostConstruct
  public void init() {
    getQueue();
    System.out.printf("%1$s initialized!%n", this);
  }

  public AsyncEventQueue getQueue() {
    Assert.state(queue != null, String.format(
      "A reference to the Async Event Queue on which this listener (%1$s) has been registered was not properly configured!",
        this));
    return queue;
  }

  //@Autowired
  public void setQueue(final AsyncEventQueue queue) {
    this.queue = queue;
  }

  @Override
  public boolean processEvents(final List<AsyncEvent> events) {
    return false;
  }

  public void close() {
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

}
