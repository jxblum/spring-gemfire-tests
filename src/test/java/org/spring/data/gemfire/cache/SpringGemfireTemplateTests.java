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

package org.spring.data.gemfire.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.query.SelectResults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.app.beans.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The SpringGemfireTemplateTests class is a test suite of test cases testing the contract and functionality of the
 * Spring Data GemFire GemfireTemplate class.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.app.beans.Customer
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see org.apache.geode.cache.Region
 * @since 1.5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class SpringGemfireTemplateTests {

  protected static final AtomicLong ID_SEQUENCE = new AtomicLong(0);

  @Autowired
  private GemfireTemplate customersTemplate;

  private List<Customer> expectedCustomers;

  @Resource(name = "Customers")
  private Region<Long, Customer> customers;

  protected Date createDate(final int year, final int month, final int dayOfMonth) {
    Calendar date = Calendar.getInstance();

    date.clear();
    date.set(Calendar.YEAR, year);
    date.set(Calendar.MONTH, month);
    date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

    return date.getTime();
  }

  protected Customer createCustomer(final String firstName, final String lastName, final Date birthDate) {
    return createCustomer(ID_SEQUENCE.incrementAndGet(), firstName, lastName, birthDate);
  }

  protected Customer createCustomer(final Long id, final String firstName, final String lastName, final Date birthDate) {
    Customer customer = new Customer(id);
    customer.setFirstName(firstName);
    customer.setLastName(lastName);
    customer.setBirthDate(birthDate);
    return customer;
  }

  protected String format(final Date birthDate) {
    return Customer.BIRTH_DATE_FORMAT.format(birthDate);
  }

  protected Customer put(final String firstName, final String lastName, final Date birthDate) {
    Customer customer = createCustomer(firstName, lastName, birthDate);
    customersTemplate.put(customer.getId(), customer);
    return customer;
  }

  @Before
  public void setup() {
    assertNotNull("The '/Customers' Region was not properly configured and initialized!", customers);

    if (customers.isEmpty()) {
      assertEquals(0, customers.size());

      put("Jon", "Doe", createDate(1959, Calendar.FEBRUARY, 14));
      put("Jane", "Doe", createDate(1966, Calendar.APRIL, 4));
      put("Fro", "Doe", createDate(2002, Calendar.DECEMBER, 16));

      expectedCustomers = new ArrayList<>(3);
      expectedCustomers.add(put("Sour", "Doe", createDate(1983, Calendar.OCTOBER, 31)));
      expectedCustomers.add(put("Pie", "Doe", createDate(1988, Calendar.NOVEMBER, 22)));
      expectedCustomers.add(put("Cookie", "Doe", createDate(1991, Calendar.MAY, 27)));

      assertFalse(expectedCustomers.isEmpty());
      assertEquals(3, expectedCustomers.size());
    }
  }

  @Test
  public void query() {
    String query;

    // METHOD 1 - the following OQL query syntax absolutely does not work for a peer application cache scenario;
    // developer must use METHOD 2
    if (System.getProperty("spring.profiles.active").contains("client")) {
      query = String.format("SELECT * FROM %1$s c WHERE c.birthDate >= DATE '%2$s' and c.birthDate <= DATE '%3$s'",
        customersTemplate.getRegion().getFullPath(), format(createDate(1980, Calendar.JULY, 1)),
        format(createDate(1995, Calendar.JUNE, 30)));

      assertEquals("SELECT * FROM /Customers c WHERE c.birthDate >= DATE '1980-07-01' and c.birthDate <= DATE '1995-06-30'", query);

    }
    // METHOD 2 use for peer application cache
    else {
      query = String.format("birthDate >= DATE '%1$s' and birthDate <= DATE '%2$s'",
        format(createDate(1980, Calendar.JULY, 1)), format(createDate(1995, Calendar.JUNE, 30)));

      assertEquals("birthDate >= DATE '1980-07-01' and birthDate <= DATE '1995-06-30'", query);

    }

    System.out.printf("GemFire OQL Query is (%1$s)", query);

    SelectResults<Customer> customerResults = customersTemplate.query(query);

    assertNotNull(customerResults);

    List<Customer> actualCustomers = customerResults.asList();

    assertNotNull(actualCustomers);
    assertFalse(actualCustomers.isEmpty());
    assertEquals(expectedCustomers.size(), actualCustomers.size());
    assertTrue(String.format("Expected (%1$s); But was (%2$s)", expectedCustomers, actualCustomers),
      actualCustomers.containsAll(expectedCustomers));
  }

}
