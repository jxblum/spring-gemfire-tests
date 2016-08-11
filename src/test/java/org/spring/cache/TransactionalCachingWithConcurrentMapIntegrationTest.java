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

import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

import org.codeprimate.lang.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.support.DaoSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test suite of test cases testing Spring's Cache Abstraction in the context of a JDBC/RDBMS Transaction in addition
 * to a multi-threaded environment.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.CacheEvict
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see org.springframework.transaction.annotation.EnableTransactionManagement
 * @see org.springframework.transaction.annotation.Transactional
 * @see edu.umd.cs.mtc.MultithreadedTestCase
 * @see edu.umd.cs.mtc.TestFramework
 * @see <a href="http://stackoverflow.com/questions/38787390/wrong-spring-cache-state-when-using-transaction-and-parallel-threads">Wrong Spring cache state when using transaction and parallel threa</a>
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TransactionalCachingWithConcurrentMapIntegrationTest.ApplicationConfiguration.class)
@SuppressWarnings("unused")
public class TransactionalCachingWithConcurrentMapIntegrationTest {

  @Autowired
  private ExampleService exampleService;

  @Test
  public void multiThreadedTransactionalCacheableServiceAccessIsCorrect() throws Throwable {
    TestFramework.runOnce(new ConcurrentTransactionalCachingServiceTestCase(exampleService));
  }

  public static final class ConcurrentTransactionalCachingServiceTestCase extends MultithreadedTestCase {

    private final ExampleService exampleService;

    public ConcurrentTransactionalCachingServiceTestCase(ExampleService exampleService) {
      Assert.notNull(exampleService, "ExampleService cannot be null");
      this.exampleService = exampleService;
    }

    public void thread1() {
      Thread.currentThread().setName("ReaderThread");

      assertThat(exampleService.load(1L)).isEqualTo("initial");
      assertThat(exampleService.cacheMiss()).isTrue();

      waitForTick(2);

      assertThat(exampleService.load(1L)).isEqualTo("test");
      assertThat(exampleService.cacheMiss()).isTrue();
    }

    public void thread2() {
      Thread.currentThread().setName("WriterThread");

      waitForTick(1);

      assertThat(exampleService.load(1L)).isEqualTo("initial");
      assertThat(exampleService.cacheMiss()).isFalse();

      exampleService.save(1L, "test");

      waitForTick(3);

      assertThat(exampleService.load(1L)).isEqualTo("test");
      assertThat(exampleService.cacheMiss()).isFalse();
    }
  }

  @Configuration
  @EnableCaching
  @EnableTransactionManagement
  static class ApplicationConfiguration {

    @Bean
    ConcurrentMapCacheManager cacheManager() {
      return new ConcurrentMapCacheManager();
    }

    String toClasspathResourceBaseName(Class<?> type) {
      return type.getName().replaceAll("\\.", "/");
    }

    String toDatabaseName(Class<?> type) {
      return type.getSimpleName();
    }

    @Bean
    DataSource dataSource(ResourceLoader resourceLoader) {
      Class type = TransactionalCachingWithConcurrentMapIntegrationTest.class;

      return new EmbeddedDatabaseBuilder(resourceLoader)
        .setName(toDatabaseName(type))
        .setType(EmbeddedDatabaseType.HSQL)
        .addScript(String.format("%s-schema.sql", toClasspathResourceBaseName(type)))
        .addScript(String.format("%s-data.sql", toClasspathResourceBaseName(type)))
        .build();
    }

    @Bean
    PlatformTransactionManager transactionManager(DataSource dataSource) {
      return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
      return new JdbcTemplate(dataSource);
    }

    @Bean
    ExampleJdbcDao exampleDao(JdbcTemplate jdbcTemplate) {
      return new ExampleJdbcDao(jdbcTemplate);
    }

    @Bean
    ExampleService exampleService(ExampleJdbcDao exampleDao) {
      return new ExampleService(exampleDao);
    }
  }

  @Service
  @Transactional
  static class ExampleService {

    private final ExampleJdbcDao exampleDao;

    private AtomicBoolean CACHE_MISS = new AtomicBoolean(false);

    ExampleService(ExampleJdbcDao exampleDao) {
      Assert.notNull(exampleDao, "ExampleJdbcDao cannot be null");
      this.exampleDao = exampleDao;
    }

    public boolean cacheHit() {
      return !cacheMiss();
    }

    public boolean cacheMiss() {
      return CACHE_MISS.getAndSet(false);
    }

    @Cacheable("Example")
    @Transactional(readOnly = true)
    public String load(Long id) {
      CACHE_MISS.set(true);
      return exampleDao.load(id);
    }

    @CacheEvict(value = "Example", allEntries = true)
    public void save(Long id, String value) {
      exampleDao.save(id, value);
    }
  }

  @Repository
  static class ExampleJdbcDao extends DaoSupport {

    private final JdbcTemplate jdbcTemplate;

    ExampleJdbcDao(JdbcTemplate jdbcTemplate) {
      Assert.notNull(jdbcTemplate, "JdbcTemplate cannot be null");
      this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void checkDaoConfig() throws IllegalArgumentException {
      Assert.notNull(jdbcTemplate, "JdbcTemplate was not properly initialized");
    }

    public String load(Long id) {
      return jdbcTemplate.queryForObject("SELECT cached_value FROM Example WHERE id = ?", String.class, id);
    }

    public void save(Long id, String value) {
      jdbcTemplate.update("UPDATE Example SET cached_value = ? WHERE id = ?", value, id);
    }
  }
}
