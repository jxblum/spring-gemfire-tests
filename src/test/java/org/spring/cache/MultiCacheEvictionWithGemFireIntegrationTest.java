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

package org.spring.cache;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;

import com.gemstone.gemfire.cache.CacheLoader;
import com.gemstone.gemfire.cache.CacheLoaderException;
import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.LoaderHelper;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.query.SelectResults;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.cache.MultiCacheEvictionWithGemFireIntegrationTest.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.mapping.GemfireMappingContext;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The MultiCacheEvictionWithGemFireIntegrationTest class is a test suite of test cases testing the functionality
 * of Spring's Cache Abstraction evicting multiple cache entries using Spring's {@link CacheEvict} annotation
 * on a Repository method.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.cache.AbstractSpringCacheAbstractionIntegrationTest
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.CacheEvict
 * @see org.springframework.cache.annotation.Caching
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.repository.Repository
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfiguration.class)
@SuppressWarnings("unused")
public class MultiCacheEvictionWithGemFireIntegrationTest extends AbstractSpringCacheAbstractionIntegrationTest {

  private static final AtomicBoolean CACHE_MISS = new AtomicBoolean(false);

  private static volatile Cache usersCache;

  @Resource(name = "Users")
  private Region<Object, User> usersRegion;

  @Autowired
  private UserRepository userRepository;

  @SafeVarargs
  protected static <T> T[] asArray(T... array) {
    return array;
  }

  protected void assertUser(User user, String name, String email) {
    assertThat(user, is(notNullValue()));
    assertThat(user.getId(), is(greaterThan(0l)));
    assertThat(user.getName(), is(equalTo(name)));
    assertThat(user.getEmail(), is(equalTo(email)));
  }

  protected boolean areAllKeysPresent(String... keys) {
    for (String key : keys) {
      if (usersCache.get(key) == null) {
        return false;
      }
    }

    return true;
  }

  protected boolean isAnyKeyPresent(String... keys) {
    for (String key : keys) {
      if (usersCache.get(key) != null) {
        return true;
      }
    }

    return false;
  }

  protected boolean isCacheHit() {
    return !isCacheMiss();
  }

  protected boolean isCacheMiss() {
    return CACHE_MISS.getAndSet(false);
  }

  protected boolean isCached(User user) {
    return isAnyKeyPresent(user.getName(), user.getEmail());
  }

  protected boolean isFullyCached(User user) {
    return areAllKeysPresent(user.getName(), user.getEmail());
  }

  protected boolean isStored(User user) {
    return (usersRegion.containsKey(user.getId()) && !(usersRegion.containsKey(user.getName())
      || usersRegion.containsKey(user.getEmail())));
  }

  protected boolean isNotStored(User user) {
    return !(usersRegion.containsKey(user.getId()) || usersRegion.containsKey(user.getName())
      || usersRegion.containsKey(user.getEmail()));
  }

  @Before
  public void setup() {
    CACHE_MISS.set(false);
  }

  @Test
  public void composedAtCacheEvictAnnotationsEvictsAllData() {
    User jonDoe = userRepository.save(User.newUser("jonDoe"));

    assertUser(jonDoe, "jonDoe", "jonDoe@home.com");
    assertThat(isCached(jonDoe), is(false));
    assertThat(isStored(jonDoe), is(true));

    User jonDoeByName = userRepository.findByName(jonDoe.getName());

    assertThat(jonDoeByName, is(equalTo(jonDoe)));
    assertThat(isCached(jonDoe), is(true));
    assertThat(isFullyCached(jonDoe), is(false));
    assertThat(isStored(jonDoe), is(true));

    User jonDoeByEmail = userRepository.findByEmail(jonDoe.getEmail());

    assertThat(jonDoeByEmail, is(equalTo(jonDoe)));
    assertThat(isFullyCached(jonDoe), is(true));
    assertThat(isStored(jonDoe), is(true));

    usersRegion.removeAll(usersRegion.keySet()); // remove from persistent store

    assertThat(isStored(jonDoe), is(false));
    assertThat(userRepository.findByEmail(jonDoe.getEmail()), is(equalTo(jonDoe)));
    assertThat(userRepository.findByName(jonDoe.getName()), is(equalTo(jonDoe)));
    assertThat(isStored(jonDoe), is(false));
    assertThat(isFullyCached(jonDoe), is(true));

    jonDoe.setEmail("jonDoe@xyz.com");

    assertUser(jonDoe, "jonDoe", "jonDoe@xyz.com");

    jonDoe = userRepository.save(jonDoe);

    assertUser(jonDoe, "jonDoe", "jonDoe@xyz.com");
    assertThat(isCached(jonDoe), is(false));
    assertThat(isStored(jonDoe), is(true));
  }

  @Configuration
  @EnableCaching
  @Import(GemFireConfiguration.class)
  static class ApplicationConfiguration {

    @Bean
    CacheManager cacheManager() {
      ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("Users") {
        @Override protected Cache createConcurrentMapCache(String name) {
          Cache cache = super.createConcurrentMapCache(name);
          usersCache = (name.equals("Users") ? cache : null);
          return cache;
        }
      };

      cacheManager.setAllowNullValues(false);

      return cacheManager;
    }

    @Bean
    GemfireRepositoryFactoryBean<UserRepository, User, Long> userRepository() {
      GemfireRepositoryFactoryBean<UserRepository, User, Long> userRepository =
        new GemfireRepositoryFactoryBean<>();

      userRepository.setGemfireMappingContext(new GemfireMappingContext());
      userRepository.setRepositoryInterface(UserRepository.class);

      return userRepository;
    }
  }

  @Configuration
  static class GemFireConfiguration {

    Properties gemfireProperties() {
      Properties gemfireProperties = new Properties();

      gemfireProperties.setProperty("name", applicationName());
      gemfireProperties.setProperty("mcast-port", "0");
      gemfireProperties.setProperty("log-level", logLevel());

      return gemfireProperties;
    }

    String applicationName() {
      return MultiCacheEvictionWithGemFireIntegrationTest.class.getSimpleName();
    }

    String logLevel() {
      return "config";
    }

    @Bean
    CacheFactoryBean gemfireCache() {
      CacheFactoryBean gemfireCache = new CacheFactoryBean();

      gemfireCache.setClose(true);
      gemfireCache.setProperties(gemfireProperties());

      return gemfireCache;
    }

    @Bean(name = "Users")
    @SuppressWarnings("unchecked")
    PartitionedRegionFactoryBean<Object, User> usersRegion(GemFireCache gemfireCache) {
      PartitionedRegionFactoryBean<Object, User> usersRegion = new PartitionedRegionFactoryBean<>();

      usersRegion.setCache(gemfireCache);
      usersRegion.setCacheLoader(userCacheLoader());
      usersRegion.setClose(false);
      usersRegion.setPersistent(false);

      return usersRegion;
    }

    CacheLoader<Object, User> userCacheLoader() {
      return new CacheLoader<Object, User>() {
        @Override
        public User load(LoaderHelper<Object, User> helper) throws CacheLoaderException {
          try {
            CACHE_MISS.set(true);

            String key = String.valueOf(helper.getKey());

            String queryPredicate = String.format("id = %1$s OR name = '%1$s' OR email = '%1$s'", key);
            SelectResults<User> queryResults = helper.getRegion().query(queryPredicate);
            List<User> users = queryResults.asList();
            User user = (!(users == null || users.isEmpty()) ? users.get(0) : null);

            return (user != null ? user : createUser(key));
          }
          catch (Exception e) {
            throw new RuntimeException(e);
          }
        }

        private User createUser(String key) {
          final int indexOfAtSign = key.indexOf("@");
          User user;

          if (indexOfAtSign > -1) {
            user = User.newUser(key.substring(0, indexOfAtSign));
            user.setEmail(key);
          }
          else {
            user = User.newUser(key);
            user.setEmail(String.format("%1$s@xyz.com", key));
          }

          return user;
        }

        @Override
        public void close() {
        }
      };
    }
  }

  @org.springframework.data.gemfire.mapping.Region("Users")
  public static class User implements Comparable<User>, Serializable {

    protected static final AtomicLong ID_SEQUENCE = new AtomicLong(0);

    @Id
    private Long id;

    private String email;
    private String name;

    public static User newUser(String name) {
      return newUser(name, String.format("%1$s@home.com", name));
    }

    public static User newUser(String name, String email) {
      User user = new User(name);
      user.setEmail(email);
      user.setId(ID_SEQUENCE.incrementAndGet());
      return user;
    }

    public User(String name) {
      Assert.hasText(name, "The User's name must be specified");
      this.name = name;
    }

    public boolean isNew() {
      return (getId() == null);
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getName() {
      return name;
    }

    @Override
    public int compareTo(User user) {
      return this.getName().compareTo(user.getName());
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }

      if (!(obj instanceof User)) {
        return false;
      }

      User that = (User) obj;

      return ObjectUtils.nullSafeEquals(this.getId(), that.getId())
        && ObjectUtils.nullSafeEquals(this.getName(), that.getName());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getId());
      hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getName());
      return hashValue;
    }

    @Override
    public String toString() {
      return String.format("{ @type = %1$s, id = %2$d, name = %3$s, email = %4$s }",
        getClass().getName(), getId(), getName(), getEmail());
    }
  }

  @SuppressWarnings("unchecked")
  interface UserRepository extends Repository<User, Long> {

    @Cacheable(cacheNames = "Users")
    User findByEmail(String email);

    @Cacheable(cacheNames = "Users")
    User findByName(String name);

    @Caching(
      evict = {
        @CacheEvict(cacheNames = "Users", key="#a0.name", beforeInvocation = true),
        @CacheEvict(cacheNames = "Users", key="#a0.email", beforeInvocation = true)
      }
      /*
      , put = {
        @CachePut(cacheNames = "Users", key="#result.name"),
        @CachePut(cacheNames = "Users", key="#result.email")
      }
      */
    )
    User save(User user);
  }
}
