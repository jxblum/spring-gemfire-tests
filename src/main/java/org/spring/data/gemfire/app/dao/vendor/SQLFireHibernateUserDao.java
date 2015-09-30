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

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.spring.data.gemfire.app.dao.provider.HibernateUserDao;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * The SQLFireHibernateUserDao class...
 *
 * @author John Blum
 * @see org.hibernate.Session
 * @see org.spring.data.gemfire.app.beans.User
 * @see SQLFireHibernateUserDao
 * @see org.springframework.stereotype.Repository
 * @since 1.0.0
 */
@Repository("userDao")
@SuppressWarnings("unused")
public class SQLFireHibernateUserDao extends HibernateUserDao {

  protected Session preProcess(final Session session) {
    Assert.notNull(session, "The Hibernate Session must not be null!");
    Assert.state(session.isOpen(), "The Session is closed!");

    if (session instanceof SessionImpl) {
      System.out.printf("The Hibernate Session is a SessionImpl!%n");
      try {
        Assert.state(!((SessionImpl) session).connection().isClosed(), "The Hibernate Session's Connection is closed!");
        //((SessionImpl) session).connection().setAutoCommit(false); // NOTE not required for SQLFire
        ((SessionImpl) session).connection().setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      }
      catch (SQLException e) {
        System.err.printf("Failed to set 'autoCommit' and 'Isolation-level' on Connection!%n%1$s%n", e);
      }
    }

    return session;
  }

}
