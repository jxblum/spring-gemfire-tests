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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.query.FunctionDomainException;
import org.apache.geode.cache.query.NameResolutionException;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryInvocationTargetException;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.cache.query.TypeMismatchException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractUserDomainTestSuite;
import org.spring.data.gemfire.app.beans.User;
import org.spring.data.gemfire.app.dao.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests testing the GemFire Query capability of Spring Data GemFire Repositories.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.spring.data.gemfire.app.dao.repo.UserRepository
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see org.apache.geode.cache.Region
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class RepositoryQueriesTest extends AbstractUserDomainTestSuite {

  @Resource(name = "Users")
  private Region users;

  @Autowired
  private UserRepository userRepository;

  private static <T> T[] asArray(T... array) {
    return array;
  }

  private static void assertQueryResults(Iterable<User> actualUsers, String... expectedUsernames) {
    assertThat(actualUsers).describedAs("The query did not return any results!").isNotNull();

    List<String> actualUsernames = new ArrayList<>(expectedUsernames.length);

    for (User actualUser : actualUsers) {
      actualUsernames.add(actualUser.getUsername());
    }

    assertThat(actualUsernames.size()).isEqualTo(expectedUsernames.length);
    assertThat(actualUsernames).contains(expectedUsernames);
  }

  @Before
  @SuppressWarnings("unchecked")
  public void setup() {
    assertThat(users).describedAs("The Users Region cannot be null!").isNotNull();

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

      assertThat(users).isNotEmpty();
      assertThat(users).hasSize(9);
    }
  }

  @Test
  public void queriesAreCorrect() {
    List<User> activeUsers = userRepository.findDistinctByActiveTrue();

    assertQueryResults(activeUsers, "blumj", "blums", "handyj", "doej");

    List<User> inactiveUsers = userRepository.findDistinctByActiveFalse();

    assertQueryResults(inactiveUsers, "blume", "bloomr", "handys", "doep", "doec");

    Integer count = userRepository.countUsersByUsernameLike("blum%");

    assertThat(count).isEqualTo(3);

    List<User> blumUsers = userRepository.findDistinctByUsernameLike("blum%");

    assertQueryResults(blumUsers, "blumj", "blums", "blume");

    /*
    List<User> nonHandyUsers = userRepository.findDistinctByUsernameNotLike("handy%");

    assertQueryResults(nonHandyUsers, "blumj", "blums", "blume", "bloomr", "doej", "doep", "doec");
    */
  }

  @Test
  @SuppressWarnings("unchecked")
  public void gemfireWilcardQueryDoesNotWork() throws Exception {
    QueryService queryService = users.getRegionService().getQueryService();
    Query query = queryService.newQuery("SELECT count(*) FROM /Users WHERE username LIKE '%$1%'");
    Object results = query.execute(asArray("b"));

    assertThat(results).isInstanceOf(SelectResults.class);
    assertThat(((SelectResults<Integer>) results).asList().get(0)).isEqualTo(0);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void gemfireQueryWithWildcardArgumentWorks() throws Exception {
    QueryService queryService = users.getRegionService().getQueryService();
    Query query = queryService.newQuery("SELECT count(*) FROM /Users WHERE username LIKE $1");
    Object results = query.execute(asArray("%b%"));

    assertThat(results).isInstanceOf(SelectResults.class);
    assertThat(((SelectResults<Integer>) results).asList().get(0)).isEqualTo(4);
  }
}
