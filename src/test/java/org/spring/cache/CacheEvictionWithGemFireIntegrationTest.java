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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.cache.CacheEvictionWithGemFireIntegrationTest.Sgf539WorkaroundConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.cache.GemfireCache;
import org.springframework.data.gemfire.cache.GemfireCacheManager;
import org.springframework.data.gemfire.mapping.GemfireMappingContext;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Integration test testing the functionality and behavior of the Spring Cache Abstraction's
 * {@link CachePut} annotation combined with the {@link CacheEvict} annotation on a single
 * application {@link Service} method when using Pivotal GemFire as the caching provider.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.cache.annotation.CacheEvict
 * @see org.springframework.cache.annotation.CachePut
 * @see org.springframework.cache.annotation.EnableCaching
 * @see <a href="http://stackoverflow.com/questions/39830488/gemfire-entrynotfoundexception-for-cacheevict">Gemfire EntryNotFoundException for @CacheEvict</a>
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Sgf539WorkaroundConfiguration.class)
@SuppressWarnings("unused")
public class CacheEvictionWithGemFireIntegrationTest extends AbstractSpringCacheAbstractionIntegrationTest {

  protected static final AtomicLong ID_SEQUENCE = new AtomicLong(0L);

  private Person janeDoe;
  private Person jonDoe;

  @Autowired
  private PeopleService peopleService;

  @Resource(name = "People")
  private org.apache.geode.cache.Region<Long, Person> peopleRegion;

  protected void assertNoPeopleInDepartment(Department department) {
    assertPeopleInDepartment(department);
  }

  protected void assertPeopleInDepartment(Department department, Person... people) {
    List<Person> peopleInDepartment = peopleService.findByDepartment(department);

    assertThat(peopleInDepartment).isNotNull();
    assertThat(peopleInDepartment.size()).isEqualTo(people.length);
    assertThat(peopleInDepartment).contains(people);
  }

  protected Person newPerson(String name, String mobile, Department department) {
    return newPerson(ID_SEQUENCE.incrementAndGet(), name, mobile, department);
  }

  protected Person newPerson(Long id, String name, String mobile, Department department) {
    Person person = Person.newPerson(department, mobile, name);
    person.setId(id);
    return person;
  }

  protected Person save(Person person) {
    peopleRegion.put(person.getId(), person);
    return person;
  }

  @Before
  public void setup() {
    janeDoe = save(newPerson("Jane Doe", "541-555-1234", Department.MARKETING));
    jonDoe = save(newPerson("Jon Doe", "972-555-1248", Department.ENGINEERING));

    assertThat(peopleRegion.containsValue(janeDoe)).isTrue();
    assertThat(peopleRegion.containsValue(janeDoe)).isTrue();
  }

  @Test
  public void janeDoeUpdateSuccessful() {
    assertNoPeopleInDepartment(Department.DESIGN);
    assertThat(peopleService.wasCacheMiss()).isTrue();

    janeDoe.setDepartment(Department.DESIGN);
    peopleService.update(janeDoe);

    assertPeopleInDepartment(Department.DESIGN, janeDoe);
    assertThat(peopleService.wasCacheMiss()).isTrue();
  }

  @Test
  public void jonDoeUpdateSuccessful() {
    jonDoe.setDepartment(Department.RESEARCH_DEVELOPMENT);
    peopleService.update(jonDoe);

    assertPeopleInDepartment(Department.RESEARCH_DEVELOPMENT, jonDoe);
    assertThat(peopleService.wasCacheMiss()).isTrue();
  }

  @Configuration
  @EnableCaching
  @Import(ApplicationTestConfiguration.class)
  static class Sgf539WorkaroundConfiguration {

    @Bean
    GemfireCacheManager cacheManager(GemFireCache gemfireCache) {
      GemfireCacheManager cacheManager = new GemfireCacheManager() {
        @Override protected org.springframework.cache.Cache decorateCache(org.springframework.cache.Cache cache) {
          return new GemfireCache((org.apache.geode.cache.Region<?, ?>) cache.getNativeCache()) {
            @Override public void evict(Object key) {
              getNativeCache().remove(key);
            }
          };
        }
      };

      cacheManager.setCache((Cache) gemfireCache);

      return cacheManager;
    }
  }

  @Configuration
  @EnableCaching
  @Import(GemFireConfiguration.class)
  static class ApplicationTestConfiguration {

    @Bean
    GemfireCacheManager cacheManager(GemFireCache gemfireCache) {
      GemfireCacheManager cacheManager = new GemfireCacheManager();
      cacheManager.setCache((Cache) gemfireCache);
      return cacheManager;
    }

    @Bean
    GemfireRepositoryFactoryBean<PersonRepository, Person, Long> personRepository() {
      GemfireRepositoryFactoryBean<PersonRepository, Person, Long> personRepository =
        new GemfireRepositoryFactoryBean<>(PersonRepository.class);
      personRepository.setGemfireMappingContext(new GemfireMappingContext());
      return personRepository;
    }

    @Bean
    PeopleService peopleService(PersonRepository personRepository) {
      return new PeopleService(personRepository);
    }
  }

  @Configuration
  static class GemFireConfiguration {

    static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";

    Properties gemfireProperties() {
      Properties gemfireProperties = new Properties();

      gemfireProperties.setProperty("name", applicationName());
      gemfireProperties.setProperty("mcast-port", "0");
      gemfireProperties.setProperty("locators", "");
      gemfireProperties.setProperty("log-level", logLevel());

      return gemfireProperties;
    }

    String applicationName() {
      return CacheEvictionWithGemFireIntegrationTest.class.getName();
    }

    String logLevel() {
      return System.getProperty("gemfire.log.level", DEFAULT_GEMFIRE_LOG_LEVEL);
    }

    @Bean
    CacheFactoryBean gemfireCache() {
      CacheFactoryBean gemfireCache = new CacheFactoryBean();

      gemfireCache.setClose(true);
      gemfireCache.setProperties(gemfireProperties());

      return gemfireCache;
    }

    @Bean(name = "People")
    LocalRegionFactoryBean<Long, Person> peopleRegion(GemFireCache gemfireCache) {
      LocalRegionFactoryBean<Long, Person> peopleRegion = new LocalRegionFactoryBean<>();

      peopleRegion.setCache(gemfireCache);
      peopleRegion.setClose(false);
      peopleRegion.setPersistent(false);

      return peopleRegion;
    }

    @Bean(name = "DepartmentPeople")
    LocalRegionFactoryBean<Long, Person> departmentPeopleRegion(GemFireCache gemfireCache) {
      LocalRegionFactoryBean<Long, Person> departmentPeopleRegion = new LocalRegionFactoryBean<>();

      departmentPeopleRegion.setCache(gemfireCache);
      departmentPeopleRegion.setClose(false);
      departmentPeopleRegion.setPersistent(false);

      return departmentPeopleRegion;
    }

    @Bean(name = "MobilePeople")
    LocalRegionFactoryBean<Long, Person> mobilePeopleRegion(GemFireCache gemfireCache) {
      LocalRegionFactoryBean<Long, Person> mobilePeopleRegion = new LocalRegionFactoryBean<>();

      mobilePeopleRegion.setCache(gemfireCache);
      mobilePeopleRegion.setClose(false);
      mobilePeopleRegion.setPersistent(false);

      return mobilePeopleRegion;
    }
  }

  public enum Department {
    ACCOUNTING,
    DESIGN,
    ENGINEERING,
    LEGAL,
    MANAGEMENT,
    MARKETING,
    RESEARCH_DEVELOPMENT,
    SALES
  }

  @Data
  @Region("People")
  @RequiredArgsConstructor(staticName = "newPerson")
  public static class Person implements Serializable {

    @Id
    private Long id;

    @NonNull private Department department;
    @NonNull private String mobile;
    @NonNull private String name;

  }

  @Service
  public static class PeopleService extends CachingSupport {

    private final PersonRepository personRepository;

    public PeopleService(PersonRepository personRepository) {
      this.personRepository = personRepository;
    }

    @Cacheable("DepartmentPeople")
    public List<Person> findByDepartment(Department department) {
      setCacheMiss();
      return personRepository.findByDepartment(department);
    }

    @Cacheable("MobilePeople")
    public Person findByMobile(String mobile) {
      setCacheMiss();
      return personRepository.findByMobile(mobile);
    }

    @Caching(
      evict = @CacheEvict(value = "DepartmentPeople", key = "#p0.department"),
      put = @CachePut(value = "MobilePeople", key="#p0.mobile")
    )
    public Person update(Person person) {
      return personRepository.save(person);
    }
  }

  public interface PersonRepository extends CrudRepository<Person, Long> {

    List<Person> findByDepartment(Department department);

    Person findByMobile(String mobile);

  }
}
