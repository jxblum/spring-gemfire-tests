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
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.codeprimate.lang.NumberUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractUserDomainTestSuite;
import org.spring.data.gemfire.app.beans.User;
import org.spring.data.gemfire.app.dao.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The RepositoryQueriesTest class is a test suite of test cases testing the GemFire Query capability of Spring Data
 * GemFire Repositories.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.spring.data.gemfire.app.dao.repo.UserRepository
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see org.apache.geode.cache.Region
 * @since 1.3.3 (Spring Data GemFire)
 * @since 7.0.1 (GemFire)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class RepositoryQueriesTest extends AbstractUserDomainTestSuite {

  @Resource(name = "Users")
  private Region users;

  @Autowired
  private UserRepository userRepository;

  protected static void assertQueryResults(final Iterable<User> actualUsers, final String... expectedUsernames) {
    assertNotNull("The query did not return any results!", actualUsers);

    List<String> actualUsernames = new ArrayList<>(expectedUsernames.length);

    for (User actualUser : actualUsers) {
      actualUsernames.add(actualUser.getUsername());
    }

    assertEquals(expectedUsernames.length, actualUsernames.size());
    assertTrue(actualUsernames.containsAll(Arrays.asList(expectedUsernames)));
  }

  @Before
  public void setup() {
    assertNotNull("The Users Region cannot be null!", users);

    if (users.isEmpty()) {
      userRepository.save(createUser("blumj", true));
      userRepository.save(createUser("blums", true));
      userRepository.save(createUser("blume", false));
      userRepository.save(createUser("bloomr", false));
      userRepository.save(createUser("handyj", true));
      userRepository.save(createUser("handys", false));
      userRepository.save(createUser("doej", true));
      userRepository.save(createUser("doep", false));
      userRepository.save(createUser("doec", false));

      assertFalse(users.isEmpty());
      assertEquals(9, users.size());
    }
  }

  @Test
  public void testQueries() {
    List<User> activeUsers = userRepository.findDistinctByActiveTrue();

    assertQueryResults(activeUsers, "blumj", "blums", "handyj", "doej");

    List<User> inactiveUsers = userRepository.findDistinctByActiveFalse();

    assertQueryResults(inactiveUsers, "blume", "bloomr", "handys", "doep", "doec");

    Integer count = userRepository.countUsersByUsernameLike("blum%");

    assertEquals(3, NumberUtils.intValue(count));

    List<User> blumUsers = userRepository.findDistinctByUsernameLike("blum%");

    assertQueryResults(blumUsers, "blumj", "blums", "blume");

    /*
    List<User> nonHandyUsers = userRepository.findDistinctByUsernameNotLike("handy%");

    assertQueryResults(nonHandyUsers, "blumj", "blums", "blume", "bloomr", "doej", "doep", "doec");
    */
  }

}
