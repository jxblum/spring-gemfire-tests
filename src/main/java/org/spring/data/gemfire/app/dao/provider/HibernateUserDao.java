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
import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.spring.data.gemfire.app.beans.User;
import org.spring.data.gemfire.app.dao.UserDao;
import org.spring.data.gemfire.app.dao.support.BatchingDaoSupportAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * The HibernateUserDao class is
 *
 * @author John Blum
 * @see org.hibernate.Query
 * @see org.hibernate.Session
 * @see org.hibernate.SessionFactory
 * @see org.hibernate.Transaction
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.spring.data.gemfire.app.dao.UserDao
 * @see org.spring.data.gemfire.app.dao.support.BatchingDaoSupportAdapter
 * @see org.springframework.orm.hibernate4.LocalSessionFactoryBean
 * @see org.springframework.stereotype.Repository
 * @since 1.0.0
 */
@Repository("userDao")
@SuppressWarnings("unused")
public class HibernateUserDao extends BatchingDaoSupportAdapter<User, String> implements UserDao {

  protected static final String COUNT_USERS_HQL = "SELECT count(u) FROM User u";
  protected static final String FIND_ALL_USERS_HQL = "SELECT u FROM User u";

  @Resource(name = "&localSessionFactory")
  private LocalSessionFactoryBean sessionFactoryBean;

  @Autowired
  private SessionFactory sessionFactory;

  public HibernateUserDao() {
  }

  public HibernateUserDao(final SessionFactory sessionFactory) {
    Assert.notNull(sessionFactory, String.format(
      "The Hibernate SessionFactory used by this DAO (%1$s) for data access cannot be null!", getClass().getName()));
    this.sessionFactory = sessionFactory;
  }

  protected void close(final Session session) {
    try {
      if (session != null) {
        session.close();
      }
    }
    catch (HibernateException unexpected) {
      System.err.printf("Failed to close Session (%1$s)!%n", session);
    }
  }

  protected <T> T doInTransaction(final TransactionCallback<T> callback) {
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();

    try {
      preProcess(session);
      T result = callback.doInTransaction(session);
      tx.commit();
      session.flush();
      return result;
    }
    catch (HibernateException e) {
      tx.rollback();
      throw e;
    }
    finally {
      close(session);
    }
  }

  protected Session preProcess(final Session session) {
    return session;
  }

  @PostConstruct
  public void init() {
    Assert.state(sessionFactory != null, "A reference to the Hibernate SessionFactory was not properly configured!");
    System.out.printf("Hibernate configuration settings (%1$s)%n", sessionFactoryBean.getConfiguration());
    System.out.printf("%1$s initialized!%n", getClass().getSimpleName());
  }

  @Override
  public int count() {
    return doInTransaction(new TransactionCallback<Integer>() {
      @Override public Integer doInTransaction(final Session session) {
        Query query = session.createQuery(COUNT_USERS_HQL);
        return Integer.valueOf(String.valueOf(query.uniqueResult()));
      }
    });
  }

  @SuppressWarnings("unchecked")
  public List<User> findAll() {
    return doInTransaction(new TransactionCallback<List<User>>() {
      @Override public List<User> doInTransaction(final Session session) {
        Query query = session.createQuery(FIND_ALL_USERS_HQL);
        return query.list();
      }
    });
  }

  @Override
  public User save(final User user) {
    return doInTransaction(new TransactionCallback<User>() {
      @Override public User doInTransaction(final Session session) {
        session.saveOrUpdate(user);
        return user;
      }
    });
  }

  @Override
  public Iterable<User> batchInsert(final Iterable<User> users) {
    System.out.printf("Batch Inserting Users (%1$s)%n", users);
    return doInTransaction(new TransactionCallback<Iterable<User>>() {
      @Override public Iterable<User> doInTransaction(final Session session) {
        for (User user : users) {
          session.save(user);
        }
        return users;
      }
    });
  }

  protected static interface TransactionCallback<T> {
    public T doInTransaction(Session session);
  }

}
