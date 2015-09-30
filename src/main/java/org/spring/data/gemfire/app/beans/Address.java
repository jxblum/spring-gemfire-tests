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

import org.codeprimate.util.ComparatorAccumulator;
import org.springframework.util.ObjectUtils;

/**
 * The Address class...
 *
 * @author John Blum
 * @see
 * @since 7.x
 */
@SuppressWarnings("unused")
public class Address implements Comparable<Address> {

  private Long id;

  private State state;

  private String street1;
  private String street2;
  private String city;
  private String zipCode;
  private String zipCodeExt;

  public Address() {
  }

  public Address(final Long id) {
    this.id = id;
  }

  public Address(final String street1, final String city, final State state, final String zipCode) {
    this.street1 = street1;
    this.city = city;
    this.state = state;
    this.zipCode = zipCode;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getStreet1() {
    return street1;
  }

  public void setStreet1(final String street1) {
    this.street1 = street1;
  }

  public String getStreet2() {
    return street2;
  }

  public void setStreet2(final String street2) {
    this.street2 = street2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(final String city) {
    this.city = city;
  }

  public State getState() {
    return state;
  }

  public void setState(final State state) {
    this.state = state;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(final String zipCode) {
    this.zipCode = zipCode;
  }

  public String getZipCodeExt() {
    return zipCodeExt;
  }

  public void setZipCodeExt(final String zipCodeExt) {
    this.zipCodeExt = zipCodeExt;
  }

  protected static <T extends Comparable<T>> int nullSafeCompareTo(final T c1, final T c2) {
    return (c1 == null ? 1 : (c2 == null ? -1 : c1.compareTo(c2)));
  }

  @Override
  public int compareTo(final Address address) {
    return new ComparatorAccumulator()
      .doCompare(getState(), address.getState())
      .doCompare(getZipCode(), address.getZipCode())
      .doCompare(getCity(), address.getCity())
      .doCompare(getStreet1(), address.getStreet1())
      .doCompare(getStreet2(), address.getStreet2())
      .getResult();
  }

  protected boolean equalsIgnoreNull(final Object obj1, final Object obj2) {
    return (obj1 == null ? obj2 == null : obj1.equals(obj2));
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof Address)) {
      return false;
    }

    Address that = (Address) obj;

    return equalsIgnoreNull(this.getId(), that.getId())
      && ObjectUtils.nullSafeEquals(this.getStreet1(), that.getStreet1())
      && ObjectUtils.nullSafeEquals(this.getStreet2(), that.getStreet2())
      && ObjectUtils.nullSafeEquals(this.getCity(), that.getCity())
      && ObjectUtils.nullSafeEquals(this.getState(), that.getState())
      && ObjectUtils.nullSafeEquals(this.getZipCode(), that.getZipCode());
  }

  @Override
  public int hashCode() {
    int hashValue = 17;
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getId());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getStreet1());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getStreet2());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getCity());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getState());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getZipCode());
    return hashValue;
  }

  @Override
  public String toString() {
    return String.format("%1$s %2$s, %3$s %4$s", getStreet1(), getCity(), getState(), getZipCode());
  }

}
