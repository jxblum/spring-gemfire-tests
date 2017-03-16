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
import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.asyncqueue.AsyncEvent;
import org.apache.geode.cache.asyncqueue.AsyncEventListener;
import org.apache.geode.cache.asyncqueue.AsyncEventQueue;
import org.spring.data.gemfire.app.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The TestAsyncEventQueueListener class...
 *
 * @author John Blum
 * @see org.spring.data.gemfire.app.service.CustomerService
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
 * @see org.apache.geode.cache.asyncqueue.AsyncEventListener
 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueue
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class TestAsyncEventListener implements AsyncEventListener {

  @Resource(name = "q1")
  private AsyncEventQueue queueOne;

  // NOTE using instance "field" injection; uncomment the @Autowired annotation and this works!
  @Autowired
  private CustomerService customerService;

  @Resource(name = "Customers")
  private Region customersRegion;

  /*
  @Autowired
  public TestAsyncEventListener(final TestBean testBean) {
  }
  */

  /*
  NOTE causes a BeanCurrentlyInCreationException when creating the AsyncEventListener (asyncEventListener) bean.
  The Exception is caused by a "circular reference" in the CustomerService bean.  The CustomerService has a reference
  to the customersDao bean, which has a reference to the customersRegion bean, which triggers the Async Event Queue
  (asyncEventQueue) bean referring to this listener to be created.
  Use "field"/"setter" injection instead.
  */
  /*
  @Autowired
  public TestAsyncEventListener(final CustomerService customerService) {
    setCustomerService(customerService);
  }
  */

  // NOTE using "setter" injection; uncomment the "setter" method and this will work too!
  //@Autowired
  public final void setCustomerService(final CustomerService customerService) {
    this.customerService = customerService;
  }

  public void init() {
    /*
    Assert.state(customerService != null, "The CustomerService reference was not properly initialized!");
    Assert.state(customersRegion != null, "The Region (customersRegion) was not properly initialized!");
    Assert.isTrue("Customers".equals(customersRegion.getName()));
    Assert.state(queueOne != null, "The Async Event Queue (q1) to which this listener is attached was not properly initialized!");
    System.out.printf("%1$s initialized!%n", this);
    */
  }

  @Override
  public boolean processEvents(final List<AsyncEvent> asyncEvents) {
    return false;
  }

  public void close() {
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
