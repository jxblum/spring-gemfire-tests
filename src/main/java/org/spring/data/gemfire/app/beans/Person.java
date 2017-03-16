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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.codeprimate.util.ComparatorAccumulator;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.util.ObjectUtils;

/**
 * The Person class is a representation modeling a person.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see java.lang.Comparable
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.gemfire.mapping.Region
 * @since 1.0.0
 */
@Region("People")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@SuppressWarnings("unused")
public class Person implements Comparable<Person>, Serializable {

  public static final DateFormat BIRTH_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private Long birthDate;

  @Id
  private Long id;

  private String firstName;
  private String lastName;

  private Gender gender;

  public Person() {
  }

  public Person(final Long id) {
    this.id = id;
  }

  public Person(final String firstName, final String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  protected Calendar getCalendar(final long timeInMillis) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.setTimeInMillis(timeInMillis);
    return calendar;
  }

  @JsonProperty("id")
  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  @JsonProperty("birthDate")
  public Date getBirthDate() {
    Calendar birthDate = getBirthDateAsCalendar();
    return (birthDate != null ? birthDate.getTime() : null);
  }

  public Calendar getBirthDateAsCalendar() {
    return (birthDate != null ? getCalendar(birthDate) : null);
  }

  public void setBirthDate(final Long birthDate) {
    this.birthDate = birthDate;
  }

  public void setBirthDate(final Calendar birthDate) {
    this.birthDate = birthDate.getTimeInMillis();
  }

  public void setBirthDate(final Date birthDate) {
    this.birthDate = birthDate.getTime();
  }

  @JsonProperty("firstName")
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  @JsonProperty("gender")
  public Gender getGender() {
    return gender;
  }

  public void setGender(final Gender gender) {
    this.gender = gender;
  }

  @JsonProperty("lastName")
  public String getLastName() {
    return lastName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  public String getName() {
    return String.format("%1$s %2$s", getFirstName(), getLastName());
  }

  @Override
  public int compareTo(final Person that) {
    return new ComparatorAccumulator()
      .doCompare(this.getLastName(), that.getLastName())
      .doCompare(this.getFirstName(), that.getFirstName())
      .doCompare(getBirthDate(), that.getBirthDate())
      .getResult();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof Person)) {
      return false;
    }

    Person that = (Person) obj;

    return ObjectUtils.nullSafeEquals(this.getBirthDate(), that.getBirthDate())
      && ObjectUtils.nullSafeEquals(this.getLastName(), that.getLastName())
      && ObjectUtils.nullSafeEquals(this.getFirstName(), that.getFirstName());
  }

  @Override
  public int hashCode() {
    int hashValue = 17;
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getBirthDate());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getFirstName());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getLastName());
    return hashValue;
  }

  @Override
  public String toString() {
    return getName();
  }
}
