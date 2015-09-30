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

package org.spring.data.gemfire.app.dao.vendor;

import java.sql.Connection;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.spring.data.gemfire.app.dao.provider.JpaUserDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * The SQLFireJpaUserDao class...
 *
 * @author John Blum
 * @see java.sql.Connection
 * @see javax.persistence.EntityManager
 * @see org.hibernate.Session
 * @see org.hibernate.internal.SessionImpl
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.spring.data.gemfire.app.dao.provider.JpaUserDao
 * @see org.springframework.stereotype.Repository
 * @see org.springframework.transaction.annotation.Transactional
 * @since 1.0.0
 */
@Repository("userDao")
@Transactional(readOnly = true)
@SuppressWarnings("unused")
public class SQLFireJpaUserDao extends JpaUserDao {

  @Override
  protected EntityManager prepare(final EntityManager entityManager) {
    Assert.notNull(entityManager, "The JPA EntityManager must not be null!");
    Assert.state(entityManager.isOpen(), "The EntityManager is closed!");

    if (entityManager.getDelegate() instanceof Session) {
      Session session = (Session) entityManager.getDelegate();

      if (session instanceof SessionImpl) {
        try {
          ((SessionImpl) session).connection().setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
          //((SessionImpl) session).connection().setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
          //((SessionImpl) session).connection().setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
          //((SessionImpl) session).connection().setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
          //((SessionImpl) session).connection().setTransactionIsolation(Connection.TRANSACTION_NONE);
        }
        catch (SQLException e) {
          //System.err.printf("Failed to set the JDBC Connection Transaction Isolation Level to (READ_COMMITTED)!%n%1$s%n", e);
          throw new PersistenceException("Failed to set the JDBC Connection Transaction Isolation-level to (READ_COMMITTED)!", e);
        }
      }
    }

    return entityManager;
  }

}
