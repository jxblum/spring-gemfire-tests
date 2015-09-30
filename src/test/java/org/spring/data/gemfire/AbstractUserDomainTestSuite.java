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

package org.spring.data.gemfire;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.spring.data.gemfire.app.beans.User;
import org.spring.data.gemfire.app.service.UserService;

/**
 * The AbstractUserDomainTestSuite class is a abstract base class containing functionality common to all
 * test suite classes that test the User-based functional classes and components/beans (UserService, UserDao, etc)
 * for Users.
 *
 * @author John Blum
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.spring.data.gemfire.app.dao.UserDao
 * @see org.spring.data.gemfire.app.dao.repo.UserRepository
 * @see org.spring.data.gemfire.app.service.UserService
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractUserDomainTestSuite {

  protected static User createUser(final String username) {
    return createUser(username, true);
  }

  protected static User createUser(final String username, final Boolean active) {
    return createUser(username, active, Calendar.getInstance());
  }

  protected static User createUser(final String username, final Boolean active, final Calendar since) {
    return createUser(username, active, since, String.format("%1$s@xcompany.com", username));
  }

  protected static User createUser(final String username, final Boolean active, final Calendar since, final String email) {
    User user = new User(username);
    user.setActive(active);
    user.setEmail(email);
    user.setSince(since);
    return user;
  }

  protected static String toString(final Calendar dateTime) {
    return (dateTime == null ? "null" : new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(dateTime.getTime()));
  }

  protected static String toString(final User user) {
    return "{ @type = ".concat(user.getClass().getName())
      .concat(", username = ").concat(user.getUsername())
      .concat(", email = ").concat(String.valueOf(user.getEmail()))
      .concat(", since = ").concat(toString(user.getSince()))
      .concat(", active = ").concat(String.valueOf(user.isActive()))
      .concat(" }");
  }

  protected UserService getUserService() {
    throw new IllegalStateException("The UserService has not been configured and initialized!");
  }

}
