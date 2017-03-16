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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.query.Index;
import org.apache.geode.cache.query.QueryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The CacheRegionIndexDefinitionCreationTest class...
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.query.Index
 * @see org.apache.geode.cache.query.QueryService
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class CacheRegionIndexDefinitionCreationTest {

  private static final AtomicLong ID_SEQUENCE = new AtomicLong(0l);

  private Cache gemfireCache;

  private QueryService queryService;

  private Region<Long, Person> people;

  protected static Date createBirthDate(final int year, final int month, final int dayOfMonth) {
    Calendar birthDate = Calendar.getInstance();
    birthDate.clear();
    birthDate.set(Calendar.YEAR, year);
    birthDate.set(Calendar.MONTH, month);
    birthDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    return birthDate.getTime();
  }

  protected static Person createPerson(final String firstName, final String lastName, final Date birthDate) {
    return createPerson(ID_SEQUENCE.incrementAndGet(), firstName, lastName, birthDate);
  }

  protected static Person createPerson(final Long id, final String firstName, final String lastName, final Date birthDate) {
    return new Person(id, firstName, lastName, birthDate);
  }

  protected static Person put(final Region<Long, Person> people, final Person person) {
    people.put(person.getId(), person);
    return person;
  }

  protected static List<String> toNames(final Iterable<Index> indexes) {
    List<String> indexNames = new ArrayList<>();

    for (Index index : indexes) {
      indexNames.add(index.getName());
    }

    return indexNames;
  }

  @Before
  public void setup() throws Exception {
    gemfireCache = new CacheFactory()
      .set("name", getClass().getName())
      .set("mcast-port", "0")
      .set("log-level", "config")
      .create();

    RegionFactory<Long, Person> regionFactory = gemfireCache.createRegionFactory();

    regionFactory.setDataPolicy(DataPolicy.REPLICATE);
    regionFactory.setKeyConstraint(Long.class);
    regionFactory.setValueConstraint(Person.class);

    people = regionFactory.create("People");

    /*
    put(people, createPerson("Jon", "Doe", createBirthDate(1989, Calendar.NOVEMBER, 11)));
    put(people, createPerson("Jane", "Doe", createBirthDate(1991, Calendar.APRIL, 4)));
    put(people, createPerson("Pie", "Doe", createBirthDate(2008, Calendar.JUNE, 21)));
    put(people, createPerson("Cookie", "Doe", createBirthDate(2011, Calendar.AUGUST, 14)));
    */

    queryService = gemfireCache.getQueryService();
    queryService.defineKeyIndex("IdIdx", "id", "/People");
    queryService.defineHashIndex("BirthDateIdx", "birthDate", "/People");
    queryService.createIndex("FullNameIdx", "fullName", "/People");
    queryService.defineHashIndex("LastNameIdx", "lastName", "/People");
  }

  @After
  public void tearDown() {
    gemfireCache.close();
    gemfireCache = null;
  }

  @Test
  public void indexesDefinedThenCreated() throws Exception {
    assertSame(people, gemfireCache.getRegion("/People"));
    //assertEquals(4, people.size());

    System.out.printf("Cache Indexes: %1$s%n", gemfireCache.getQueryService().getIndexes());
    System.out.printf("Cache /People Region Indexes: %1$s%n", gemfireCache.getQueryService().getIndexes(people));

    List<String> expectedIndexNames = Arrays.asList("FullNameIdx");
    List<String> actualIndexNames = toNames(gemfireCache.getQueryService().getIndexes());

    assertEquals(expectedIndexNames.size(), actualIndexNames.size());
    assertTrue(actualIndexNames.containsAll(expectedIndexNames));

    queryService.createDefinedIndexes();
    //put(people, createPerson("Sour", "Doe", createBirthDate(2013, Calendar.MARCH, 12)));

    System.out.printf("Cache Indexes: %1$s%n", gemfireCache.getQueryService().getIndexes());
    System.out.printf("Cache /People Region Indexes: %1$s%n", gemfireCache.getQueryService().getIndexes(people));

    expectedIndexNames = Arrays.asList("IdIdx", "BirthDateIdx", "FullNameIdx", "LastNameIdx");
    actualIndexNames = toNames(gemfireCache.getQueryService().getIndexes());

    //assertEquals(5, people.size());
    assertEquals(expectedIndexNames.size(), actualIndexNames.size());
    assertTrue(actualIndexNames.containsAll(expectedIndexNames));
  }

  public static class Person implements Serializable {

    protected static final String BIRTH_DATE_FORMAT_PATTERN = "yyyy/MM/dd";

    private Date birthDate;

    private Long id;

    private String firstName;
    private String lastName;

    public Person() {
    }

    public Person(final Long id) {
      this.id = id;
    }

    public Person(final String firstName, final String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public Person(final String firstName, final String lastName, final Date birthDate) {
      this(firstName, lastName);
      this.birthDate = (birthDate != null ? (Date) birthDate.clone() : null);
    }

    public Person(final Long id, final String firstName, final String lastName, final Date birthDate) {
      this(firstName, lastName, birthDate);
      this.id = id;
    }

    public Long getId() {
      return id;
    }

    public Date getBirthDate() {
      return birthDate;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getFullName() {
      return String.format("%1$s %2$s", getFirstName(), getLastName());
    }

    public String getLastName() {
      return lastName;
    }

    protected static boolean equalsIgnoreNull(final Object obj1, final Object obj2) {
      return (obj1 == null ? obj2 == null : obj1.equals(obj2));
    }

    protected static boolean nullSafeEquals(final Object obj1, final Object obj2) {
      return (obj1 != null && obj1.equals(obj2));
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

      return equalsIgnoreNull(getId(), that.getId())
        && (nullSafeEquals(getBirthDate(), that.getBirthDate()))
        && (nullSafeEquals(getFirstName(), that.getFirstName())
        && (nullSafeEquals(getLastName(), that.getLastName())));
    }

    protected static int nullSafeHashCode(final Object obj) {
      return (obj != null ? obj.hashCode() : 0);
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + nullSafeHashCode(getId());
      hashValue = 37 * hashValue + nullSafeHashCode(getBirthDate());
      hashValue = 37 * hashValue + nullSafeHashCode(getFirstName());
      hashValue = 37 * hashValue + nullSafeHashCode(getLastName());
      return hashValue;
    }

    protected static String toString(final Date dateTime, final String DATE_FORMAT_PATTERN) {
      return (dateTime == null ? null : new SimpleDateFormat(DATE_FORMAT_PATTERN).format(dateTime));
    }

    @Override
    public String toString() {
      return String.format("{ @type = %1$s, id = %2$d, firstName = %3$s, lastName = %4$s, birthDate = %5$s }",
        getClass().getName(), getId(), getFirstName(), getLastName(),
        toString(getBirthDate(), BIRTH_DATE_FORMAT_PATTERN));
    }
  }

}
