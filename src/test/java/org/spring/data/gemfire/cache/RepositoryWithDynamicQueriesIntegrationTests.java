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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.mapping.GemfireMappingContext;
import org.springframework.data.gemfire.mapping.annotation.LocalRegion;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Integration tests for dynamic GemFire OQL queries on Spring Data GemFire Repositories.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.query.Query
 * @see org.apache.geode.cache.query.QueryService
 * @see org.apache.geode.cache.query.SelectResults
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see <a href="http://docs.spring.io/spring-data-gemfire/docs/current/reference/html/#_introduction">GemFire Repositories</a>
 * @see <a href="http://docs.spring.io/spring-data/data-commons/docs/current/reference/html/#repositories.custom-implementations">Custom implementation for Spring Data repositories</a>
 * @see <a href="http://stackoverflow.com/questions/42840786/spring-data-gemfire-oql>Spring Data GemFire OQL</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class RepositoryWithDynamicQueriesIntegrationTests {

  protected static final String GEMFIRE_LOG_LEVEL = "config";

  @Resource(name = "Users")
  private Region<String, User> users;

  private final Set<User> savedUsers = new HashSet<>();

  @Autowired
  private UserRepository userRepository;

  @SafeVarargs
  private static <T> T[] asArray(T... array) {
    return array;
  }

  @Before
  public void setup() {
    save(User.newUser("jackBlack"));
    save(User.newUser("jonDoe"));
    save(User.newUser("janeDoe"));
    save(User.newUser("cookieDoe"));
    save(User.newUser("froDoe"));
    save(User.newUser("hoDoe"));
    save(User.newUser("pieDoe"));
    save(User.newUser("sourDoe"));
    save(User.newUser("jackHandy"));

    assertThat(users).hasSize(9);
  }

  protected Set<User> query(String usernameEndingWith) {
    return savedUsers.stream().filter(user -> user.getName().endsWith(usernameEndingWith))
      .collect(Collectors.toSet());
  }

  protected User save(User user) {
    savedUsers.add(userRepository.save(user));
    return user;
  }

  @Test
  public void gemfireQueryTest() throws Exception {
    QueryService queryService = users.getRegionService().getQueryService();
    Query query = queryService.newQuery("$1");
    Object results = query.execute(asArray("SELECT * FROM /Users WHERE name LIKE '%Doe'"));

    assertThat(results).isInstanceOf(SelectResults.class);
    assertThat((SelectResults) results).hasSize(7);
  }

  @Test
  public void springDataGemFireDynamicQueryTest() {
    List<User> users = userRepository.dynamicQuery("SELECT * FROM /Users WHERE name LIKE '%Doe'");

    assertThat(users).isNotNull();
    assertThat(users).hasSize(7);
  }

  @Test
  public void springDataGemFireCustomDynamicQueryTest() {
    List<User> users = userRepository.customDynamicQuery("SELECT * FROM /Users WHERE name LIKE '%Doe'");

    assertThat(users).isNotNull();
    assertThat(users).hasSize(7);
    assertThat(users).containsAll(query("Doe"));
  }

  @Data
  @LocalRegion("Users")
  @RequiredArgsConstructor(staticName = "newUser")
  static class User {

    @Id @NonNull String name;
  }

  interface UserRepository extends CrudRepository<User, String>, UserRepositoryExtension {

    @org.springframework.data.gemfire.repository.Query("$1")
    List<User> dynamicQuery(String query);
  }

  interface UserRepositoryExtension {

    List<User> customDynamicQuery(String query);
  }

  static class UserRepositoryImpl implements UserRepositoryExtension {

    @Autowired
    private GemfireTemplate usersTemplate;

    @Override
    public List<User> customDynamicQuery(String query) {
      return usersTemplate.<User>find(query).asList();
    }
  }

  @SuppressWarnings("unused")
  @PeerCacheApplication(name = "RepositoryWithDynamicQueriesIntegrationTests", logLevel = GEMFIRE_LOG_LEVEL)
  static class TestConfiguration {

    @Bean(name = "Users")
    LocalRegionFactoryBean<String, User> usersRegion(GemFireCache gemfireCache) {
      LocalRegionFactoryBean<String, User> users = new LocalRegionFactoryBean<>();

      users.setCache(gemfireCache);
      users.setClose(false);
      users.setPersistent(false);

      return users;
    }

    @Bean
    GemfireRepositoryFactoryBean<UserRepository, User, String> userRepository(GemFireCache gemfireCache) {

      GemfireRepositoryFactoryBean<UserRepository, User, String> repositoryFactoryBean =
        new GemfireRepositoryFactoryBean<>(UserRepository.class);

      repositoryFactoryBean.setCustomImplementation(userRepositoryExtension());
      repositoryFactoryBean.setGemfireMappingContext(new GemfireMappingContext());

      return repositoryFactoryBean;
    }

    @Bean
    UserRepositoryExtension userRepositoryExtension() {
      return new UserRepositoryImpl();
    }

    @Bean
    GemfireTemplate userTemplate(GemFireCache gemfireCache) {
      return new GemfireTemplate(gemfireCache.getRegion("/Users"));
    }
  }
}
