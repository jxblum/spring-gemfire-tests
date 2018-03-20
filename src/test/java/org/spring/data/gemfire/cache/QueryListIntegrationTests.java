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

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.query.SelectResults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Integration tests for Pivotal GemFire OQL Query with predicate on nested {@link List} using Spring.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see <a href="https://stackoverflow.com/questions/49385728/oql-to-query-list-in-gemfire">OQL to query List in GemFire</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class QueryListIntegrationTests {

  @Autowired
  private GemfireTemplate employeesTemplate;

  private Employee cookieDoe;
  private Employee pieDoe;
  private Employee sourDoe;

  private Employee save(Employee employee) {
    this.employeesTemplate.put(employee.getId(), employee);
    return employee;
  }

  @Before
  public void setup() {

    cookieDoe = save(Employee.newEmployee(1L, "Cookie Doe")
      .with(Experience.newExperience("ABC", LocalDate.of(2000, Month.JULY, 1))
        .ended(LocalDate.of(2010, Month.JUNE, 30)))
      .with(Experience.newExperience("XYZ", LocalDate.of(2010, Month.SEPTEMBER, 5))));

    pieDoe = save(Employee.newEmployee(2L, "Pie Doe")
      .with(Experience.newExperience("ABC", LocalDate.of(2008, Month.APRIL, 1))));

    sourDoe = save(Employee.newEmployee(3L, "Sour Doe")
      .with(Experience.newExperience("XYZ", LocalDate.of(2012, Month.SEPTEMBER, 5))));
  }

  @Test
  public void queryByExperience() {

    assertThat(this.employeesTemplate.getRegion().size()).isEqualTo(3);

    SelectResults<Employee> results =
      this.employeesTemplate.query("SELECT e FROM /Employees e, e.experiences x WHERE x.organization = 'ABC'");

    assertThat(results).isNotNull();
    assertThat(results.size()).isEqualTo(2);

    System.err.printf("Employee query results [%s]%n", results.asList());

    assertThat(results.asList()).containsExactlyInAnyOrder(cookieDoe, pieDoe);
  }

  @ClientCacheApplication
  @SuppressWarnings("unused")
  static class TestConfiguration {

    @Bean("Employees")
    public ClientRegionFactoryBean<Object, Object> employeesRegion(GemFireCache gemfireCache) {

      ClientRegionFactoryBean<Object, Object> clientRegion = new ClientRegionFactoryBean<>();

      clientRegion.setCache(gemfireCache);
      clientRegion.setClose(false);
      clientRegion.setShortcut(ClientRegionShortcut.LOCAL);

      return clientRegion;
    }

    @Bean
    public GemfireTemplate employeesTemplate(GemFireCache gemfireCache) {
      return new GemfireTemplate(gemfireCache.getRegion("/Employees"));
    }
  }

  @Data
  @RequiredArgsConstructor(staticName = "newEmployee")
  static class Employee {

    @Id @NonNull
    private final Long id;

    @NonNull
    private final String name;

    private List<Experience> experiences = new ArrayList<>();

    Employee with(Experience experience) {

      Assert.notNull(experience, "Experience is required");

      this.experiences.add(experience);

      return this;
    }

    @Override
    public String toString() {
      return getName();
    }
  }

  @Data
  @RequiredArgsConstructor(staticName = "newExperience")
  static class Experience {

    @NonNull
    private final String organization;

    @NonNull
    private final LocalDate from;

    private LocalDate to;

    Experience ended(LocalDate to) {

      Assert.isTrue(to == null || to.isAfter(getFrom()),
        () -> String.format("End date/time [%1$s] must be after start date/time [%2$s]", to, getFrom()));

      this.to = to;

      return this;
    }
  }
}
