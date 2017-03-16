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

package org.spring.data.gemfire.app.beans;

import org.springframework.data.gemfire.mapping.annotation.Region;

/**
 * The Customer class is an abstract data type and model for a customer.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see org.spring.data.gemfire.app.beans.Person
 * @see org.springframework.data.gemfire.mapping.Region
 * @since 1.0.0
 */
@Region("Customers")
@SuppressWarnings("unused")
public class Customer extends Person {

  public Customer() {
  }

  public Customer(Long id) {
    super(id);
  }

  public Customer(String firstName, String lastName) {
    super(firstName, lastName);
  }

  public boolean isNew() {
    return (getId() == null);
  }
}
