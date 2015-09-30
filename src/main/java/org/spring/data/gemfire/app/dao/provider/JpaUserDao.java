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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.spring.data.gemfire.app.beans.User;
import org.spring.data.gemfire.app.dao.UserDao;
import org.spring.data.gemfire.app.dao.support.DaoSupportAdapter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * The JpaUserDao class is a Data Access Object (DAO) used to perform data access, persistence and querying operations
 * on Users along the ability to execute any functions or stored procedures using JPA.
 *
 * @author John Blum
 * @see javax.persistence.EntityManager
 * @see javax.persistence.PersistenceContext
 * @see javax.persistence.Query
 * @see javax.persistence.TypedQuery
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.spring.data.gemfire.app.dao.UserDao
 * @see org.spring.data.gemfire.app.dao.support.DaoSupportAdapter
 * @see org.springframework.stereotype.Repository
 * @see org.springframework.transaction.annotation.Transactional
 * @since 1.0.0
 */
@Repository("userDao")
@Transactional(readOnly = true)
@SuppressWarnings("unused")
public class JpaUserDao extends DaoSupportAdapter<User, String> implements UserDao {

  protected static final String COUNT_USERS_SQL = "SELECT count(*) FROM Users";
  protected static final String FIND_ALL_USER_HQL = "SELECT u FROM User u";

  @PersistenceContext(properties = { @PersistenceProperty(name="hibernate.connection.isolation", value="2") })
  private EntityManager entityManager;

  public JpaUserDao() {
  }

  public JpaUserDao(final EntityManager entityManager) {
    Assert.notNull(entityManager, String.format(
      "The JPA EntityManager used by this DAO (%1$s) for data access cannot be null!",getClass().getName()));
    this.entityManager = entityManager;
  }

  @PostConstruct
  public void init() {
    Assert.state(entityManager != null, "A reference to the JPA EntityManager was not properly configured!");
    System.out.printf("%1$s%n", entityManager.getEntityManagerFactory().getProperties());
    System.out.printf("%1$s initialized!%n", getClass().getSimpleName());
  }

  protected EntityManager prepare(final EntityManager entityManager) {
    return entityManager;
  }

  public int count() {
    Query userCountQuery = entityManager.createNativeQuery(COUNT_USERS_SQL);
    return Integer.valueOf(String.valueOf(userCountQuery.getSingleResult()));
  }

  public List<User> findAll() {
    TypedQuery<User> userQuery = entityManager.createQuery(FIND_ALL_USER_HQL, User.class);
    return userQuery.getResultList();
  }

  @Transactional
  public User save(final User user) {
    EntityManager localEntityManager = prepare(this.entityManager);
    localEntityManager.persist(user);
    localEntityManager.flush();
    return user;
  }

}
