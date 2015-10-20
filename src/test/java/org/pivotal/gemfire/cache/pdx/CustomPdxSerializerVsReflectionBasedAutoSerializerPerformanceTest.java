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

package org.pivotal.gemfire.cache.pdx;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.number.OrderingComparisons.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.gemstone.gemfire.internal.lang.ObjectUtils;
import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxSerializer;
import com.gemstone.gemfire.pdx.PdxWriter;
import com.gemstone.gemfire.pdx.ReflectionBasedAutoSerializer;

import org.codeprimate.lang.Assert;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spring.data.gemfire.pdx.PdxReaderSupport;
import org.spring.data.gemfire.pdx.PdxWriterSupport;

/**
 * The CustomPdxSerializerVsReflectionBasedAutoSerializerPerformanceTest class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class CustomPdxSerializerVsReflectionBasedAutoSerializerPerformanceTest {

  private static final int JVM_WARM_UP_ITERATIONS = 1000;
  private static final int TEST_ITERATIONS = (JVM_WARM_UP_ITERATIONS * 100);

  private static long customSerializerTime;
  private static long reflectionSerializerTime;

  private static Person[] people;

  private PdxReader pdxReader;

  private PdxSerializer customSerializer;
  private PdxSerializer reflectionSerializer;

  private PdxWriter pdxWriter;

  private Random randomNumberGenerator;

  protected static Person newPerson(final String firstName, final String lastName) {
    return new Person(firstName, lastName);
  }

  @BeforeClass
  public static void setupBeforeClass() {
    people = new Person[10];
    people[0] = newPerson("Jon", "Doe");
    people[1] = newPerson("Jane", "Doe");
    people[2] = newPerson("Cookie", "Doe");
    people[3] = newPerson("En", "Doe");
    people[4] = newPerson("Fro", "Doe");
    people[5] = newPerson("HapKi", "Doe");
    people[6] = newPerson("Joe", "Doe");
    people[7] = newPerson("Pie", "Doe");
    people[8] = newPerson("Po", "Doe");
    people[9] = newPerson("Sour", "Doe");
  }

  @AfterClass
  public static void tearDownAfterClass() {
    System.out.printf("Custom Serialization Time (%1$d)%n", customSerializerTime);
    System.out.printf("Reflection-based Serialization Time (%1$d)%n", reflectionSerializerTime);

    assertThat(String.format("Expected custom Serialization (%1$d) to be faster than Reflection-bssed Serialization (%2$d)!",
      customSerializerTime, reflectionSerializerTime), customSerializerTime,
        is(lessThanOrEqualTo(reflectionSerializerTime)));
  }

  @Before
  public void setup() {
    PdxSerializedDataStore serializedDataStore = new PdxSerializedDataStore();

    pdxReader = new CustomPdxReader(serializedDataStore);
    pdxWriter = new CustomPdxWriter(serializedDataStore);
    customSerializer = new PersonPdxSerializer();
    reflectionSerializer = new ReflectionBasedAutoSerializer(Person.class.getName());
    randomNumberGenerator = new Random(System.currentTimeMillis());
  }

  protected Person select(int index) {
    return people[index];
  }

  protected Person selectRandom() {
    return select(randomNumberGenerator.nextInt(people.length));
  }

  protected long doSerialization(PdxSerializer pdxSerializer) {
    Person person = selectRandom();

    for (int count = 0; count < JVM_WARM_UP_ITERATIONS; count++) {
      pdxSerializer.toData(person, pdxWriter);
      pdxSerializer.fromData(person.getClass(), pdxReader);
    }

    long t0 = System.nanoTime();

    for (int count = 0; count < TEST_ITERATIONS; count++) {
      person = selectRandom();
      pdxSerializer.toData(person, pdxWriter);
      pdxSerializer.fromData(person.getClass(), pdxReader);
    }

    return (System.nanoTime() - t0);
  }

  @Test
  public void measureCustomPdxSerializer() {
    customSerializerTime = doSerialization(customSerializer);
  }

  @Test
  public void measureReflectionBasedAutoSerializer() {
    reflectionSerializerTime = doSerialization(reflectionSerializer);
  }

  protected static class PdxSerializedDataStore {

    private static final Map<String, byte[]> serializedData = new ConcurrentHashMap<>();

    protected static byte[] retrieve(String name) {
      return serializedData.get(name);
    }

    protected static void store(String name, byte[] data) {
      serializedData.put(name, data);
    }
  }

  public static class CustomPdxReader extends PdxReaderSupport {

    private final PdxSerializedDataStore serializedDataStore;

    public CustomPdxReader(PdxSerializedDataStore serializedDataStore) {
      Assert.notNull(serializedDataStore, "PdxSerializedDataStore must not be null");
      this.serializedDataStore = serializedDataStore;
    }

    @Override
    @SuppressWarnings("all")
    public String readString(final String fieldName) {
      return new String(serializedDataStore.retrieve(fieldName));
    }
  }

  public static class CustomPdxWriter extends PdxWriterSupport {

    private final PdxSerializedDataStore serializedDataStore;

    public CustomPdxWriter(PdxSerializedDataStore serializedDataStore) {
      Assert.notNull(serializedDataStore, "PdxSerializedDataStore must not be null");
      this.serializedDataStore = serializedDataStore;
    }

    @Override
    @SuppressWarnings("all")
    public PdxWriter writeString(final String fieldName, final String value) {
      this.serializedDataStore.store(fieldName, value.getBytes());
      return this;
    }
  }

  public static class Person {

    private String firstName;
    private String lastName;

    public Person(final String firstName, final String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
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

      return (ObjectUtils.equals(this.getLastName(), that.getLastName()))
        && (ObjectUtils.equals(this.getFirstName(), that.getFirstName()));
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + ObjectUtils.hashCode(getLastName());
      hashValue = 37 * hashValue + ObjectUtils.hashCode(getFirstName());
      return hashValue;
    }

    @Override
    public String toString() {
      return String.format("%1$s %2$s", getFirstName(), getLastName());
    }
  }

  public static class PersonPdxSerializer implements PdxSerializer {

    @Override
    public boolean toData(final Object obj, final PdxWriter out) {
      if (obj instanceof Person) {
        Person person = (Person) obj;
        out.writeString("firstName", person.getFirstName());
        out.writeString("lastName", person.getLastName());
        return true;
      }

      return false;
    }

    @Override
    public Object fromData(final Class<?> type, final PdxReader in) {
      if (Person.class.isAssignableFrom(type)) {
        return new Person(in.readString("firstName"), in.readString("lastName"));
      }

      return null;
    }
  }

}
