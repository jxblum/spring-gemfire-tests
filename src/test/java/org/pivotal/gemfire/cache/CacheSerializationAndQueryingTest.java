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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.pdx.PdxInstance;
import com.gemstone.gemfire.pdx.ReflectionBasedAutoSerializer;

import org.codeprimate.io.FileSystemUtils;
import org.codeprimate.lang.ClassUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spring.data.gemfire.app.beans.Customer;

/**
 * The CacheSerializationAndQueryingTest class is a test suite testing the use of a GemFire Cache Region as a polyglot
 * application store with Java Objects, Java Serialized Objects, and PdxInstances and GemFire's capability in querying
 * those stored objects with different forms of arguments (un/serialized) as OQL Query bind parameters.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.spring.data.gemfire.app.beans.Customer
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.cache.query.Query
 * @see com.gemstone.gemfire.cache.query.SelectResults
 * @see com.gemstone.gemfire.pdx.PdxInstance
 * @see com.gemstone.gemfire.pdx.ReflectionBasedAutoSerializer
 * @since 1.0.0
 */
public class CacheSerializationAndQueryingTest {

  private static final AtomicLong ID_SEQUENCE = new AtomicLong(0l);

  private static Cache cache;

  private static Customer jonDoe;
  private static Customer jackHandy;
  private static Customer agentSmith;

  private static Query query;

  private static Region<Long, Object> customers;

  @BeforeClass
  public static void setup() {
    cache = new CacheFactory()
      .setPdxSerializer(new ReflectionBasedAutoSerializer(Customer.class.getName()))
      .set(DistributionConfig.NAME_NAME, "CacheSerializationAndQueryTest")
      .set(DistributionConfig.MCAST_PORT_NAME, "0")
      .set(DistributionConfig.LOG_LEVEL_NAME, "config")
      .create();

    RegionFactory<Long, Object> regionFactory = cache.createRegionFactory();

    //regionFactory.setDataPolicy(DataPolicy.PARTITION);
    regionFactory.setDataPolicy(DataPolicy.REPLICATE);
    regionFactory.setInitialCapacity(11);
    regionFactory.setLoadFactor(0.75f);
    regionFactory.setKeyConstraint(Long.class);
    //regionFactory.setValueConstraint(Customer.class);

    customers = regionFactory.create("Customers");

    assertNotNull("The Customers GemFire Cache Region was not properly configured and initialized", customers);
    assertTrue(customers.isEmpty());

    jonDoe = createCustomer("Jon", "Doe");
    jackHandy = createCustomer("Jack", "Handy");
    agentSmith = createCustomer("Agent", "Smith");

    putObject(jonDoe);
    putJavaSerialized(jackHandy);
    putPdxInstance(agentSmith);

    assertFalse(customers.isEmpty());
    assertEquals(3, customers.size());

    query = cache.getQueryService().newQuery("SELECT c FROM /Customers c WHERE c = $1");
  }

  @AfterClass
  public static void tearDown() {
    cache.close();
    cache = null;
  }

  protected static Customer createCustomer(final String firstName, final String lastName) {
    return createCustomer(ID_SEQUENCE.incrementAndGet(), firstName, lastName);
  }

  protected static Customer createCustomer(final Long id, final String firstName, final String lastName) {
    Customer customer = new Customer(firstName, lastName);
    customer.setId(id);
    return customer;
  }

  protected static byte[] putJavaSerialized(final Customer customer) {
    return put(customer.getId(), toJavaSerializedBytes(customer));
  }

  protected static Customer putObject(final Customer customer) {
    return put(customer.getId(), customer);
  }

  protected static PdxInstance putPdxInstance(final Customer customer) {
    return put(customer.getId(), toPdxInstance(customer));
  }

  protected static <T> T put(final Long key, final T value) {
    customers.put(key, value);
    return value;
  }

  @SuppressWarnings("unchecked")
  protected static <T> T[] toArray(T... values) {
    return (T[]) Arrays.asList(values).toArray();
  }

  @SuppressWarnings({ "unchecked", "unused" })
  protected static <T> T fromJavaSerializedBytes(final byte[] buffer) {
    ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(buffer);
    ObjectInputStream objectIn = null;

    try {
      objectIn = new ObjectInputStream(byteArrayIn);
      return (T) objectIn.readObject();
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to read object from serialized bytes!", e);
    }
    finally {
      FileSystemUtils.close(objectIn);
    }
  }

  protected static byte[] toJavaSerializedBytes(final Customer customer) {
    ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
    ObjectOutputStream objectOut = null;

    try {
      objectOut = new ObjectOutputStream(byteArrayOut);
      objectOut.writeObject(customer);
      objectOut.flush();
    }
    catch (IOException e) {
      throw new RuntimeException(String.format("Failed to serialize Customer (%1$s)!", customer), e);
    }
    finally {
      FileSystemUtils.close(objectOut);
    }

    return byteArrayOut.toByteArray();
  }

  @SuppressWarnings({ "unchecked", "unused" })
  protected static <T> T fromPdxInstance(final PdxInstance pdxInstance) {
    return (T) pdxInstance.getObject();
  }

  protected static PdxInstance toPdxInstance(final Customer customer) {
    return cache.createPdxInstanceFactory(customer.getClass().getName())
      .writeObject("id", customer.getId())
      .writeString("firstName", customer.getFirstName())
      .writeString("lastName", customer.getLastName())
      .markIdentityField("id")
      .create();
  }

  @SuppressWarnings("unchecked")
  protected void assertQueryResults(final int expectedResultsSize, final Object expectedResult, final Object results) {
    if (results instanceof SelectResults) {
      SelectResults selectResults = (SelectResults) results;
      int actualResultsSize = selectResults.size();

      if (actualResultsSize == expectedResultsSize) {
        if (actualResultsSize == 1) {
          Object actualResult = selectResults.asList().get(0);

          actualResult = (actualResult instanceof byte[] ? fromJavaSerializedBytes((byte[]) actualResult)
            : (actualResult instanceof PdxInstance ? fromPdxInstance((PdxInstance) actualResult) : actualResult));

          assertEquals(expectedResult, actualResult);
        }
        else if (actualResultsSize > 1) {
          assertTrue(selectResults.asList().containsAll((Collection) expectedResult));
        }
      }
      else {
        fail(String.format("Expected Query result set of size %1$d; but was %2$d!",
          expectedResultsSize, selectResults.size()));
      }
    }
    else {
      fail(String.format("Expected Query result set of type (%1$s); but was %2$s!", SelectResults.class.getName(),
        ClassUtils.getClassName(results)));
    }
  }

  @Test
  public void testQueryJavaObjects() throws Exception {
    Object queryResults = query.execute(toArray(jonDoe));

    assertQueryResults(1, jonDoe, queryResults);

    queryResults = query.execute(toArray(createCustomer(null, "Jon", "Doe")));

    assertQueryResults(1, jonDoe, queryResults);

    queryResults = query.execute(new Object[] { toJavaSerializedBytes(jonDoe) });

    assertQueryResults(0, jonDoe, queryResults);

    queryResults = query.execute(toArray(toPdxInstance(jonDoe)));

    assertQueryResults(0, jonDoe, queryResults);
  }

  @Test
  public void testQueryJavaSerializedObjects() throws Exception {
    Object queryResults = query.execute(toArray(jackHandy));

    assertQueryResults(0, jackHandy, queryResults);

    queryResults = query.execute(toArray(createCustomer(null, "Jack", "Handy")));

    assertQueryResults(0, jackHandy, queryResults);

    queryResults = query.execute(new Object[] { toJavaSerializedBytes(jackHandy) });

    assertQueryResults(0, jackHandy, queryResults);

    queryResults = query.execute(toArray(toPdxInstance(jackHandy)));

    assertQueryResults(0, jackHandy, queryResults);
  }

  @Test
  public void testQueryPdxInstance() throws Exception {
    Object queryResults = query.execute(toArray(agentSmith));

    assertQueryResults(0, agentSmith, queryResults);

    queryResults = query.execute(toArray(toPdxInstance(createCustomer(null, "Agent", "Smith"))));

    assertQueryResults(0, agentSmith, queryResults);

    queryResults = query.execute(new Object[] { toJavaSerializedBytes(agentSmith) });

    assertQueryResults(0, agentSmith, queryResults);

    queryResults = query.execute(toArray(toPdxInstance(agentSmith)));

    assertQueryResults(1, agentSmith, queryResults);
  }

}
