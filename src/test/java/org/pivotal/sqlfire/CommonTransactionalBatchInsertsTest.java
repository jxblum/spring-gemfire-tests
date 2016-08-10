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

package org.pivotal.sqlfire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.spring.data.gemfire.AbstractUserDomainTestSuite;
import org.spring.data.gemfire.app.beans.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * The CommonTransactionalBatchInsertsTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.spring.data.gemfire.AbstractUserDomainTestSuite
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.spring.data.gemfire.app.dao.UserDao
 * @see org.spring.data.gemfire.app.service.UserService
 * @since 1.0.0
 */
public abstract class CommonTransactionalBatchInsertsTest extends AbstractUserDomainTestSuite {

  protected static final User[] USERS = {
    createUser("jondoe"),
    createUser("janedoe"),
    createUser("piedoe"),
    createUser("cookiedoe"),
    createUser("jackhandy"),
    createUser("mandyhandy"),
    createUser("randyhandy"),
    createUser("sandyhandy"),
    createUser("imapigg"),
    createUser("jondoe")
  };

  @Test
  public void testBatchInserts() {
    assertEquals(0, getUserService().getNumberOfUsers());

    List<User> userList = Arrays.asList(USERS);

    assertNotNull(userList);
    assertEquals(USERS.length, userList.size());
    assertTrue(userList.containsAll(Arrays.asList(USERS)));

    List<User> userBatchOne = userList.subList(0, userList.size() / 2);
    List<User> userBatchTwo = userList.subList(userList.size() / 2, userList.size());

    assertNotNull(userBatchOne);
    assertNotNull(userBatchTwo);
    assertEquals(userList.size(), userBatchOne.size() + userBatchTwo.size());
    assertTrue(userBatchOne.contains(createUser("jondoe")));
    assertFalse(userBatchOne.contains(createUser("imapigg")));
    assertTrue(userBatchTwo.contains(createUser("jondoe")));
    assertFalse(userBatchTwo.contains(createUser("piedoe")));

    getUserService().addAll(userBatchOne);

    assertEquals(userBatchOne.size(), getUserService().getNumberOfUsers());

    try {
      getUserService().addAll(userBatchTwo);
    }
    // TODO refactor!
    // NOTE the following assertions are very fragile and naive but used temporarily only for testing
    // and experimentation purposes
    catch (ConstraintViolationException expected) {
      assertTrue(expected.getCause() instanceof SQLException);
      System.err.printf("%1$s%n", expected.getCause().getMessage());
    }
    catch (DataIntegrityViolationException expected) {
      System.err.printf("%1$s%n", expected);
    }
    catch (DataAccessException expected) {
      assertTrue(expected.getCause() instanceof PersistenceException);
      assertTrue(expected.getCause().getCause() instanceof SQLIntegrityConstraintViolationException
        || expected.getCause() instanceof SQLException);
      System.err.printf("%1$s%n", expected.getCause().getMessage());
    }
    catch (PersistenceException expected) {
      assertTrue(expected.getCause() instanceof ConstraintViolationException);
      assertTrue(expected.getCause().getCause() instanceof SQLException);
      System.err.printf("%1$s%n", expected.getCause().getCause().getMessage());
      //assertTrue(expected.getCause().getCause().getMessage().contains(
      //  "duplicate value(s) for column(s) USERNAME in statement"));
    }

    assertEquals(userBatchOne.size(), getUserService().getNumberOfUsers());

    List<User> users = getUserService().list();

    System.out.printf("%1$s%n", users);
    System.out.printf("%1$s%n", toString(users.get(0)));
  }

}
