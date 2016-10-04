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

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.GemFireCache;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.cache.CacheEvictionWithGemFireIntegrationTest.ApplicationTestConfiguration;
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
import org.springframework.data.gemfire.mapping.GemfireMappingContext;
import org.springframework.data.gemfire.mapping.Region;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean;
import org.springframework.data.gemfire.support.GemfireCacheManager;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Integration test testing the functionality and behavior of the Spring Cache Abstraction's
 * {@link CacheEvict} annotation when using Pivotal GemFire as the caching provider.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see <a href="http://stackoverflow.com/questions/39830488/gemfire-entrynotfoundexception-for-cacheevict">Gemfire EntryNotFoundException for @CacheEvict</a>
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationTestConfiguration.class)
@SuppressWarnings("unused")
public class CacheEvictionWithGemFireIntegrationTest extends AbstractSpringCacheAbstractionIntegrationTest {

  protected static final AtomicLong ID_SEQUENCE = new AtomicLong(0L);

  private Person janeDoe;
  private Person jonDoe;

  @Autowired
  private PeopleService peopleService;

  @Resource(name = "People")
  private com.gemstone.gemfire.cache.Region<Long, Person> peopleRegion;

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
      GemfireRepositoryFactoryBean<PersonRepository, Person, Long> personRepository = new GemfireRepositoryFactoryBean<>();
      personRepository.setGemfireMappingContext(new GemfireMappingContext());
      personRepository.setRepositoryInterface(PersonRepository.class);
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
