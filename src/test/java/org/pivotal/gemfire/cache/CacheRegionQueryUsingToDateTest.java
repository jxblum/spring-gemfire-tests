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

package org.pivotal.gemfire.cache;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;

import org.codeprimate.lang.ObjectUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

/**
 * The CacheRegionQueryUsingToDateTest class is a test suite of test cases testing the contract and functionality
 * of GemFire's Query execution using the TO_DATE(..) function.
 *
 * @author John Blum
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.cache.query.Query
 * @see com.gemstone.gemfire.cache.query.QueryService
 * @since 1.0.0
 */
public class CacheRegionQueryUsingToDateTest {

  private static final AtomicLong ID_SEQUENCE = new AtomicLong(0l);

  private Cache gemfireCache;

  @Before
  public void setup() {
    gemfireCache = new CacheFactory()
      .set("name", getClass().getSimpleName())
      .set("mcast-port", "0")
      .set("log-level", "config")
      .create();

    assertThat(gemfireCache, is(not(nullValue())));

    RegionFactory<Long, Person> peopleRegionFactory = gemfireCache.createRegionFactory();

    peopleRegionFactory.setDataPolicy(DataPolicy.REPLICATE);
    peopleRegionFactory.setInitialCapacity(11);
    peopleRegionFactory.setLoadFactor(0.85f);
    peopleRegionFactory.setKeyConstraint(Long.class);
    peopleRegionFactory.setValueConstraint(Person.class);

    Region people = peopleRegionFactory.create("People");

    assertThat(people, is(not(nullValue())));
    assertThat(people.getName(), is(equalTo("People")));
    assertThat(people.getFullPath(), is(equalTo(String.format("%1$s%2$s", Region.SEPARATOR, "People"))));
    assertThat(people.getAttributes(), is(not(nullValue())));
    assertThat(people.getAttributes().getDataPolicy(), is(equalTo(DataPolicy.REPLICATE)));
  }

  @Before
  public void setupPeople() {
    Region<Long, Person> localPeople = gemfireCache.getRegion("/People");

    put(localPeople, createPerson("Jon", "Doe", createDate(1979, Calendar.MAY, 15)));
    put(localPeople, createPerson("Jane", "Doe", createDate(1981, Calendar.APRIL, 21)));
    put(localPeople, createPerson("Pie", "Doe", createDate(1997, Calendar.NOVEMBER, 22)));
    put(localPeople, createPerson("Cookie", "Doe", createDate(2008, Calendar.AUGUST, 16)));
    put(localPeople, createPerson("Sour", "Doe", createDate(2012, Calendar.DECEMBER, 1)));
    put(localPeople, createPerson("Jack", "Handy", createDate(1977, Calendar.MAY, 10)));
    put(localPeople, createPerson("Sandy", "Handy", createDate(1977, Calendar.MARCH, 3)));
    put(localPeople, createPerson("Jack", "Black", createDate(1972, Calendar.JUNE, 26)));
    put(localPeople, createPerson("Ben", "Dover", createDate(1969, Calendar.MAY, 5)));
    put(localPeople, createPerson("Ima", "Pigg", createDate(1975, Calendar.JULY, 17)));

    assertThat(localPeople.size(), is(equalTo(10)));
  }

  @After
  public void tearDown() {
    gemfireCache.close();
    gemfireCache = null;
  }

  protected Date createDate(final int year, final int month, final int day) {
    Calendar dateTime = Calendar.getInstance();
    dateTime.clear();
    dateTime.set(Calendar.YEAR, year);
    dateTime.set(Calendar.MONTH, month);
    dateTime.set(Calendar.DAY_OF_MONTH, day);
    return dateTime.getTime();
  }

  protected Person createPerson(final String firstName, final String lastName, final Date birthDate) {
    Person person = new Person(firstName, lastName, birthDate);
    person.setId(ID_SEQUENCE.incrementAndGet());
    return person;
  }

  protected void put(final Region<Long, Person> region, final Person person) {
    Assert.notNull(person.getId(), "person ID must not be null");
    region.put(person.getId(), person);
  }

  protected String toString(final List<Person> people) {
    StringBuilder buffer = new StringBuilder("[");

    for (Person person : people) {
      buffer.append(buffer.length() > 1 ? ", " : "");
      buffer.append(person.getName());
    }

    buffer.append("]");

    return buffer.toString();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void queryMayBirthdays() throws Exception {
    QueryService queryService = gemfireCache.getQueryService();

    Query query = queryService.newQuery("<trace> SELECT DISTINCT * FROM /People p WHERE p.getBirthDateAsString('MM') = '05' ORDER BY p.lastName");
    //Query query = queryService.newQuery("SELECT DISTINCT * FROM /People p WHERE p.birthDate = TO_DATE('1979-05-15', 'yyyy-MM-dd') ORDER BY p.lastName");

    SelectResults<Person> results = (SelectResults<Person>) query.execute();

    List<Person> people = results.asList();

    assertThat(toString(people), is(equalTo("[Jon Doe, Ben Dover, Jack Handy]")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void querySpecificBirthDate() throws Exception {
    QueryService queryService = gemfireCache.getQueryService();

    Query query = queryService.newQuery("<trace> SELECT DISTINCT * FROM /People p WHERE p.birthDate = TO_DATE('1979-05-15', 'yyyy-MM-dd') ORDER BY p.lastName");

    SelectResults<Person> results = (SelectResults<Person>) query.execute();

    List<Person> people = results.asList();

    assertThat(toString(people), is(equalTo("[Jon Doe]")));
  }

  public static class Person {

    protected static final String BIRTH_DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    private final Date birthDate;

    private Long id;

    private final String firstName;
    private final String lastName;

    public Person(final String firstName, final String lastName, final Date birthDate) {
      this.firstName = firstName;
      this.lastName = lastName;
      this.birthDate = birthDate;
    }

    public Long getId() {
      return id;
    }

    public void setId(final Long id) {
      this.id = id;
    }

    public Date getBirthDate() {
      return birthDate;
    }

    public String getBirthDateAsString() {
      return getBirthDateAsString(BIRTH_DATE_FORMAT_PATTERN);
    }

    public String getBirthDateAsString(final String pattern) {
      return new SimpleDateFormat(pattern).format(getBirthDate());
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public String getName() {
      return String.format("%1$s %2$s", getFirstName(), getLastName());
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

      return ObjectUtils.nullSafeEquals(this.getFirstName(), that.getFirstName())
        && ObjectUtils.nullSafeEquals(this.getLastName(), that.getLastName())
        && ObjectUtils.nullSafeEquals(this.getBirthDate(), that.getBirthDate());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + ObjectUtils.hashCode(getFirstName());
      hashValue = 37 * hashValue + ObjectUtils.hashCode(getLastName());
      hashValue = 37 * hashValue + ObjectUtils.hashCode(getBirthDate());
      return hashValue;
    }

    @Override
    public String toString() {
      return String.format("%1$s, born %2$s", getName(), getBirthDateAsString());
    }
  }

}
