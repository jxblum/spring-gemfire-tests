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

package org.spring.data.gemfire.app.service;

import javax.annotation.PostConstruct;

import org.spring.data.gemfire.app.dao.GemfireRegionCustomerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * The CustomerService class is a Service bean for maintaining business logic and rules around Customers.
 *
 * @author John Blum
 * @see GemfireRegionCustomerDao
 * @see org.springframework.stereotype.Service
 * @since 1.0.0
 */
@Service
@SuppressWarnings("unused")
public class CustomerService {

  @Autowired
  private GemfireRegionCustomerDao customerDao;

  protected GemfireRegionCustomerDao getCustomerDao() {
    Assert.state(customerDao != null, "A reference to the GemfireRegionCustomerDao was not properly configured!");
    return customerDao;
  }

  @PostConstruct
  public void init() {
    getCustomerDao();
    System.out.printf("%1$s initialized!%n", this);
  }

  // TODO implement service methods here...

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
