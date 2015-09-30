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

package org.spring.data.gemfire.app.service;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import org.spring.data.gemfire.app.beans.User;
import org.spring.data.gemfire.app.dao.UserDao;
import org.spring.data.gemfire.app.dao.repo.UserRepository;
import org.spring.data.gemfire.app.dao.support.BatchingDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * The UserService class is a Service bean/component for servicing Users.
 *
 * @author John Blum
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.spring.data.gemfire.app.dao.UserDao
 * @see org.spring.data.gemfire.app.dao.repo.UserRepository
 * @see org.springframework.stereotype.Service
 * @see org.springframework.transaction.annotation.Transactional
 * @since 1.0.0
 */
@Service("userService")
@SuppressWarnings("unused")
public class UserService {

  @Autowired
  private UserDao userDao;

  @Autowired(required = false)
  private UserRepository userRepository;

  public UserService() {
  }

  public UserService(final UserDao userDao) {
    this.userDao = userDao;
  }

  public UserService(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserService(final UserDao userDao, final UserRepository userRepository) {
    this.userDao = userDao;
    this.userRepository = userRepository;
  }

  protected UserDao getUserDao() {
    Assert.state(userDao != null, "A reference to the UserDao was not properly configured!");
    return userDao;
  }

  protected UserRepository getUserRepository() {
    Assert.state(userRepository != null, "A reference to the UserRepository was not properly configured!");
    return userRepository;
  }

  @PostConstruct
  public void init() {
    getUserDao();
    //getUserRepository();
    System.out.printf("%1$s initialized!%n", this);
  }

  @SuppressWarnings("unchecked")
  @Transactional
  //@Transactional(isolation = Isolation.READ_COMMITTED)
  public Iterable<User> addAll(final Iterable<User> users) {
    if (getUserDao() instanceof BatchingDaoSupport) {
      return ((BatchingDaoSupport<User, String>) getUserDao()).batchInsert(asList(users));
    }
    else {
      for (User user : users) {
        add(user);
      }

      return users;
    }
  }

  @Transactional
  public User add(final User user) {
    return getUserDao().save(user);
  }

  protected <T> List<T> asList(final Iterable<T> it) {
    final List<T> list = new ArrayList<T>();
    for (T item : it) {
      list.add(item);
    }
    return list;
  }

  @Transactional(readOnly = true)
  public int getNumberOfUsers() {
    return getUserDao().count();
  }

  @Transactional(readOnly = true)
  public List<User> list() {
    return getUserDao().findAll();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
