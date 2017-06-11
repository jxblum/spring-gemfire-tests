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

package org.spring.data.gemfire.app.dao.repo;

import java.util.List;

import org.spring.data.gemfire.app.beans.Customer;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.data.gemfire.repository.Query;
import org.springframework.data.gemfire.repository.query.annotation.Hint;
import org.springframework.data.gemfire.repository.query.annotation.Limit;
import org.springframework.data.gemfire.repository.query.annotation.Trace;

/**
 * The CustomerRepository class...
 *
 * @author John Blum
 * @see org.spring.data.gemfire.app.beans.Customer
 * @see org.springframework.data.gemfire.repository.GemfireRepository
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public interface CustomerRepository extends GemfireRepository<Customer, Long> {

  //SELECT * FROM /Customers WHERE id = $1
  Customer findCustomerById(Long id);

  //SELECT * FROM /Customers WHERE lastName = $1
  List<Customer> findByLastName(String lastName);

  @Trace
  @Limit(10)
  @Hint({"CustomerIdIdx", "AnotherIndexIdx" })
  @Query("SELECT DISTINCT c FROM /Customers c, /Accounts a WHERE c.id = a.customerId")
  List<Customer> findCustomersWithAccounts();

}
