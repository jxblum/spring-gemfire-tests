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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The User class represents an authorized user of a service or computer system, etc.
 *
 * @author John Blum
 * @see java.lang.Comparable
 * @see javax.persistence.Column
 * @see javax.persistence.Entity
 * @see javax.persistence.Id
 * @see javax.persistence.Table
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.gemfire.mapping.Region
 * @since 1.0.0
 */
@Entity
@Region("Users")
@Table(name = "Users")
@SuppressWarnings("unused")
public class User implements Comparable<User> {

  private boolean active = true;

  private Address address;

  private Calendar since;

  private PhoneNumber phoneNumber;

  private String email;

  private Set<Address> addresses = new HashSet<>();

  @Id
  @javax.persistence.Id
  @javax.persistence.Column(name = "username", nullable = false, unique = true)
  private String username;

  public User() {
  }

  public User(String username) {
    Assert.hasText(username, "Username is required!");
    this.username = username;
  }

  public Boolean getActive() {
    return active;
  }

  public boolean isActive() {
    return Boolean.TRUE.equals(getActive());
  }

  public void setActive(final Boolean active) {
    this.active = Boolean.TRUE.equals(active);
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(final Address address) {
    this.address = address;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public Calendar getSince() {
    return since;
  }

  public void setSince(final Calendar since) {
    this.since = since;
  }

  public String getUsername() {
    return username;
  }

  public Set<Address> getAddresses() {
    return addresses;
  }

  public boolean add(final Address address) {
    return addresses.add(address);
  }

  @Override
  public int compareTo(final User user) {
    return getUsername().compareTo(user.getUsername());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof User)) {
      return false;
    }

    User that = (User) obj;

    return this.getUsername().equals(that.getUsername())
      && ObjectUtils.nullSafeEquals(this.getEmail(), that.getEmail());
  }

  @Override
  public int hashCode() {
    int hashValue = 17;
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getEmail());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getUsername());
    return hashValue;
  }

  @Override
  public String toString() {
    return getUsername();
  }
}
