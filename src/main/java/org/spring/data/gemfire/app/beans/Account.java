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

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.util.Assert;

/**
 * The Account class is an abstract data type (ADT) for modeling customer accounts.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see org.springframework.data.gemfire.mapping.Region
 * @since 1.0.0
 */
@Region("Accounts")
@SuppressWarnings("unused")
public class Account implements Serializable {

  @Id
  private Long id;

  private Long customerId;

  private String number;

  public Account() {
  }

  public Account(Long customerId) {
    Assert.notNull(customerId, "The Customer ID to which this Account belongs cannot be null!");
    this.customerId = customerId;
  }

  public Account(Customer customer) {
    this(customer.getId());
  }

  public Account(Long customerId, String number) {
    this(customerId);
    this.number = number;
  }

  public Account(Customer customer, String number) {
    this(customer);
    this.number = number;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long customerId) {
    this.customerId = customerId;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  @Override
  public String toString() {
    return String.format("{ @type = %1$s, id = %2$d, customerId = %3$d, number = %4$s }",
      getClass().getName(), getId(), getCustomerId(), getNumber());
  }
}
