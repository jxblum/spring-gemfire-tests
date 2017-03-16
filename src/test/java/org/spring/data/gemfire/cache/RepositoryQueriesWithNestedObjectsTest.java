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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractUserDomainTestSuite;
import org.spring.data.gemfire.app.beans.Address;
import org.spring.data.gemfire.app.beans.State;
import org.spring.data.gemfire.app.beans.User;
import org.spring.data.gemfire.app.dao.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The RepositoryQueriesWithNestedObjectsTest class is a test suite of test cases testing complex GemFire OQL query
 * execution against a complex domain object hierarchy.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class RepositoryQueriesWithNestedObjectsTest extends AbstractUserDomainTestSuite {

  private static final AtomicLong ID_GENERATOR = new AtomicLong(0l);

  @Resource(name = "Users")
  private Region users;

  @Autowired
  private UserRepository userRepository;

  protected Address createAddress(final String street1, final String city, final State state, final String zipCode) {
    Address address = new Address(street1, city, state, zipCode);
    address.setId(ID_GENERATOR.incrementAndGet());
    return address;
  }

  protected User createUserWithAddresses(final String username, final Address... addresses) {
    User user = createUser(username);
    for (Address address : addresses) {
      user.add(address);
    }
    return user;
  }

  @Test
  public void testQueryNestedObjects() {
    userRepository.save(createUserWithAddresses("jonDoe", createAddress("100 Main St.", "Portland", State.OREGON, "12345"),
      createAddress("500 Walker Ave.", "Portland", State.MAINE, "12345"),
      createAddress("200 Barnes Rd.", "Butte", State.MONTANA, "12345")));
    userRepository.save(createUserWithAddresses("janeDoe", createAddress("100 Main St.", "Portland", State.OREGON, "12345")));
    userRepository.save(createUserWithAddresses("pieDoe", createAddress("100 Main St.", "Portland", State.OREGON, "12345")));
    userRepository.save(createUserWithAddresses("cookieDoe", createAddress("100 Main St.", "Portland", State.OREGON, "12345")));
    userRepository.save(createUserWithAddresses("jackHandy", createAddress("1500 Washington Ave.", "New York", State.NEW_YORK, "12345")));
    userRepository.save(createUserWithAddresses("sandyHandy", createAddress("1500 Washington Ave.", "New York", State.NEW_YORK, "12345")));
    userRepository.save(createUserWithAddresses("imaPigg", createAddress("131 SE 13th St.", "Jackson", State.MISSISSIPPI, "12345")));
    userRepository.save(createUser("joeBlow"));
    //userRepository.save(createUserWithAddresses("joeBlow", createAddress("4141 NW 5th St.", "Los Angeles", State.CALIFORNIA, "12345")));

    assertEquals(8l, userRepository.count());

    List<User> results = userRepository.findUsersInCity("Portland");

    assertNotNull(results);
    assertFalse(results.isEmpty());
    //assertEquals(4, results.size());
    assertTrue(String.format("Expected [jonDoe, janeDoe, pieDoe, cookieDoe]; but was (%1$s)", results),
      results.containsAll(Arrays.asList(createUser("jonDoe"), createUser("janeDoe"), createUser("pieDoe"),
        createUser("cookieDoe"))));
  }

}
