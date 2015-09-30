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

package org.pivotal.gemfire.pdx;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import com.gemstone.gemfire.pdx.PdxInstanceFactory;
import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxSerializable;
import com.gemstone.gemfire.pdx.PdxSerializer;
import com.gemstone.gemfire.pdx.PdxWriter;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

/**
 * The GemFireJsonToPdxToObjectDataAccessIntegrationTest class is a test suite of test cases testing the contract
 * and functionality of GemFire's JSON support and GemFire's ability to convert the resulting PDX data back
 * to the desired application domain object type on Region gets and queries.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.pdx.JSONFormatter
 * @see com.gemstone.gemfire.pdx.PdxInstance
 * @see com.gemstone.gemfire.pdx.PdxSerializable
 * @see com.gemstone.gemfire.pdx.PdxSerializer
 * @since 1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("unused")
public class GemFireJsonToPdxToObjectDataAccessIntegrationTest {

  protected static final AtomicLong ID_SEQUENCE = new AtomicLong(0l);

  private static Cache gemfireCache;

  private static Region<Long, Object> customers;

  private Customer jonDoe;
  private Customer janeDoe;
  private Customer jackBlack;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @BeforeClass
  public static void setupBeforeClass() {
    gemfireCache = new CacheFactory()
      .setPdxSerializer(new CustomerPdxSerializer())
      .setPdxReadSerialized(false)
      .setPdxIgnoreUnreadFields(true)
      .set("name", GemFireJsonToPdxToObjectDataAccessIntegrationTest.class.getSimpleName())
      .set("mcast-port", "0")
      .set("log-level", "warning")
      .create();

    RegionFactory<Long, Object> regionFactory = gemfireCache.createRegionFactory();

    regionFactory.setDataPolicy(DataPolicy.PARTITION);
    regionFactory.setInitialCapacity(11);
    regionFactory.setLoadFactor(0.75f);
    regionFactory.setKeyConstraint(Long.class);
    regionFactory.setValueConstraint(Object.class);

    customers = regionFactory.create("Customers");
  }

  @AfterClass
  public static void tearDownAfterClass() {
    gemfireCache.close();
  }

  protected Customer createCustomer(String firstName, String lastName) {
    return createCustomer(ID_SEQUENCE.incrementAndGet(), firstName, lastName);
  }

  protected Customer createCustomer(Long id, String firstName, String lastName) {
    return new Customer(id, firstName, lastName);
  }

  protected <T> T fromPdx(Object pdxInstance, Class<T> toType) {
    try {
      if (pdxInstance == null) {
        return null;
      }
      else if (toType.isInstance(pdxInstance)) {
        return toType.cast(pdxInstance);
      }
      else if (pdxInstance instanceof PdxInstance) {
        return new ObjectMapper().readValue(JSONFormatter.toJSON(((PdxInstance) pdxInstance)), toType);
      }
      else {
        throw new IllegalArgumentException(String.format("Expected object of type PdxInstance; but was (%1$s)",
          nullSafeClassName(pdxInstance)));
      }
    }
    catch (IOException e) {
      throw new RuntimeException(String.format("Failed to convert PDX to object of type (%1$s)", toType), e);
    }
  }

  protected String nullSafeClassName(Object obj) {
    return (obj != null ? obj.getClass().getName() : null);
  }

  protected Customer put(Customer customer) {
    Object existingCustomer = customers.putIfAbsent(customer.getId(), toPdx(customer));
    return (existingCustomer != null ? fromPdx(existingCustomer, Customer.class) : customer);
  }

  protected Customer putWithFactory(Customer customer) {
    Object existingCustomer = customers.putIfAbsent(customer.getId(), toPdxUsingFactory(customer));
    return (existingCustomer != null ? fromPdx(existingCustomer, Customer.class) : customer);
  }

  protected PdxInstance toPdx(Object obj) {
    try {
      String json = new ObjectMapper().writeValueAsString(obj);
      System.err.printf("JSON (%1$s)%n", json);
      return JSONFormatter.fromJSON(new ObjectMapper().writeValueAsString(obj));
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException(String.format("Failed to convert object (%1$s) to JSON", obj), e);
    }
  }

  protected PdxInstance toPdxUsingFactory(Customer customer) {
    PdxInstanceFactory pdxInstanceFactory = gemfireCache.createPdxInstanceFactory(customer.getClass().getName());
    pdxInstanceFactory.writeLong("id", customer.getId());
    pdxInstanceFactory.writeString("firstName", customer.getFirstName());
    pdxInstanceFactory.writeString("lastName", customer.getLastName());
    return pdxInstanceFactory.create();
  }

  @Before
  public void setup() {
    jonDoe = put(createCustomer("Jon", "Doe"));
    janeDoe = put(createCustomer("Jane", "Doe"));
    jackBlack = putWithFactory(createCustomer("Jack", "Black"));
  }

  @Test
  public void regionGetOnFactoryCreatedPdxInstance() {
    assertThat((Customer) customers.get(jackBlack.getId()), is(equalTo(jackBlack)));
  }

  @Test
  public void regionGetOnObjectJsonCreatedPdxInstance() {
    assertThat((Customer) customers.get(jonDoe.getId()), is(equalTo(jonDoe)));
    assertThat((Customer) customers.get(janeDoe.getId()), is(equalTo(janeDoe)));
  }

  @Test
  public void regionQueryOnObjectJsonCreatedPdxInstance() throws Exception {
    SelectResults<Customer> selectResults = customers.query(String.format("id = %1$d", jonDoe.getId()));

    List<Customer> customers = selectResults.asList();

    assertThat(customers, is(notNullValue()));
    assertThat(customers.isEmpty(), is(false));
    assertThat(customers.size(), is(equalTo(1)));

    Customer actualJonDoe = customers.get(0);

    assertThat(actualJonDoe, is(equalTo(jonDoe)));

    // NOTE: now ensure jonDoe was not deserialized!
    expectedException.expect(ClassCastException.class);
    expectedException.expectCause(is(nullValue(Throwable.class)));
    expectedException.expectMessage(String.format(
      "com.gemstone.gemfire.pdx.internal.PdxInstanceImpl cannot be cast to %1$s", Customer.class.getName()));

    assertThat((Customer) GemFireJsonToPdxToObjectDataAccessIntegrationTest.customers.get(jonDoe.getId()),
      is(equalTo(jonDoe)));
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
  public static class Customer implements PdxSerializable {

    protected static final CustomerPdxSerializer pdxSerializer = new CustomerPdxSerializer();

    private Long id;

    private String firstName;
    private String lastName;

    public Customer() {
    }

    public Customer(Long id) {
      this.id = id;
    }

    public Customer(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public Customer(Long id, String firstName, String lastName) {
      this.id = id;
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public String getName() {
      return String.format("%1$s %2$s", getFirstName(), getLastName());
    }

    @Override
    public void fromData(final PdxReader reader) {
      Customer customer = (Customer) pdxSerializer.fromData(Customer.class, reader);

      if (customer != null) {
        this.id = customer.getId();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
      }
    }

    @Override
    public void toData(final PdxWriter writer) {
      pdxSerializer.toData(this, writer);
    }

    protected static boolean equalsIgnoreNull(Object obj1, Object obj2) {
      return (obj1 == null ? obj2 == null : obj1.equals(obj2));
    }

    protected static boolean nullSafeEquals(Object obj1, Object obj2) {
      return (obj1 != null && obj1.equals(obj2));
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof Customer)) {
        return false;
      }

      Customer that = (Customer) obj;

      return equalsIgnoreNull(this.getId(), that.getId())
        && nullSafeEquals(this.getFirstName(), that.getFirstName())
        && nullSafeEquals(this.getLastName(), that.getLastName());
    }

    protected static int nullSafeHashCode(Object value) {
      return (value != null ? value.hashCode() : 0);
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + nullSafeHashCode(getId());
      hashValue = 37 * hashValue + nullSafeHashCode(getFirstName());
      hashValue = 37 * hashValue + nullSafeHashCode(getLastName());
      return hashValue;
    }

    @Override
    public String toString() {
      return String.format("{ @type = %1$s, id = %2$d, name = %3$s }",
        getClass().getName(), getId(), getName());
    }
  }

  public static class CustomerPdxSerializer implements PdxSerializer {

    @Override
    public Object fromData(Class<?> type, PdxReader in) {
      if (Customer.class.equals(type)) {
        return new Customer(in.readLong("id"), in.readString("firstName"), in.readString("lastName"));
      }

      return null;
    }

    @Override
    public boolean toData(Object obj, PdxWriter out) {
      if (obj instanceof Customer) {
        Customer customer = (Customer) obj;
        out.writeLong("id", customer.getId());
        out.writeString("firstName", customer.getFirstName());
        out.writeString("lastName", customer.getLastName());
        return true;
      }

      return false;
    }
  }

}
