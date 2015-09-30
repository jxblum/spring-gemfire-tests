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

package org.spring.data.gemfire.app.dao.provider;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.gemstone.gemfire.cache.Cache;

import org.codeprimate.lang.InitializationException;
import org.spring.data.gemfire.app.beans.Gemstone;
import org.spring.data.gemfire.app.dao.GemstoneDao;
import org.spring.data.gemfire.app.dao.support.DaoSupportAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The DatabaseGemstoneDao class is a Data Access Object (DAO) implementing the GemsDao interface for accessing
 * and persisting data about gemstones to RDBMS Table.
 *
 * @author John Blum
 * @see javax.naming.Context
 * @see javax.sql.DataSource
 * @see org.spring.data.gemfire.app.beans.Gemstone
 * @see org.spring.data.gemfire.app.dao.GemstoneDao
 * @see org.spring.data.gemfire.app.dao.support.DaoSupportAdapter
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.stereotype.Repository
 * @see org.springframework.transaction.annotation.Transactional
 * @see com.gemstone.gemfire.cache.Cache
 * @since 1.0.0
 */
@Repository("databaseGemstoneDao")
@SuppressWarnings("unused")
public class DatabaseGemstoneDao extends DaoSupportAdapter<Gemstone, Long> implements GemstoneDao {

  protected static final String COUNT_GEMSTONES_SQL = "SELECT DISTINCT count(*) FROM gemfire.gemstones";
  protected static final String INSERT_GEMSTONE_SQL = "INSERT INTO gemfire.gemstones (id, stone_name) VALUES (?, ?)";
  protected static final String SELECT_ALL_GEMSTONES_SQL = "SELECT * FROM gemfire.gemstones;";
  protected static final String SELECT_GEMSTONE_BY_ID_SQL = "SELECT * FROM gemfire.gemstones WHERE id = ?;";

  protected static final String GEMFIREDB_JNDI_LOCATION = "java:/gemfiredb";

  @Autowired
  private Cache gemfireCache;

  @Autowired(required = false)
  private JdbcTemplate databaseTemplate;

  public DatabaseGemstoneDao() {
  }

  public DatabaseGemstoneDao(final Cache gemfireCache) {
    Assert.notNull(gemfireCache, "The GemFire Cache reference must not be null!");
    this.gemfireCache = gemfireCache;
  }

  protected Cache getCache() {
    Assert.state(gemfireCache != null, "A reference to the GemFire Cache was not properly configured!");
    return gemfireCache;
  }

  protected JdbcTemplate getDatabaseTemplate() {
    Assert.state(databaseTemplate != null, "A reference to the JDBC template was not properly configured!");
    return databaseTemplate;
  }

  @PostConstruct
  public void init() {
    try {
      Context jndiContext = getCache().getJNDIContext();
      Object dataSourceObject = jndiContext.lookup(GEMFIREDB_JNDI_LOCATION);

      Assert.isTrue(dataSourceObject instanceof DataSource, String.format(
        "Expected a javax.sql.DataSource object; but was %1$s%n", ObjectUtils.nullSafeClassName(dataSourceObject)));

      databaseTemplate = new JdbcTemplate((DataSource) dataSourceObject);

      System.out.printf("%1$s initialized!%n", getClass().getSimpleName());
    }
    catch (NamingException e) {
      throw new InitializationException(String.format("Failed to initialize %1$s!", getClass().getSimpleName()), e);
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public int count() {
    return getDatabaseTemplate().queryForObject(COUNT_GEMSTONES_SQL, Integer.class);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public Gemstone findBy(final Long id) {
    return getDatabaseTemplate().queryForObject(SELECT_GEMSTONE_BY_ID_SQL, GemstoneRowMapper.INSTANCE, id);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public Iterable<Gemstone> findAll() {
    return getDatabaseTemplate().query(SELECT_ALL_GEMSTONES_SQL, GemstoneRowMapper.INSTANCE);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public Gemstone save(final Gemstone gemstone) {
    getDatabaseTemplate().update(INSERT_GEMSTONE_SQL, gemstone.getId(), gemstone.getName());
    return gemstone;
  }

  protected static class GemstoneRowMapper implements RowMapper<Gemstone> {

    protected static final GemstoneRowMapper INSTANCE = new GemstoneRowMapper();

    @Override
    public Gemstone mapRow(final ResultSet resultSet, final int rowNumber) throws SQLException {
      return new Gemstone(resultSet.getLong(1), resultSet.getString(2));
    }
  }

}
