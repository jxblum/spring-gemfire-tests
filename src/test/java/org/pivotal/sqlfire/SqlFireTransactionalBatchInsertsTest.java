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

import org.junit.runner.RunWith;
import org.spring.data.gemfire.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * The TransactionalBatchInsertsTest class is a test suite of test cases testing the transactional behavior of SQLFire
 * (SQLF) when a batch of inserts could lead to a potential resource conflict exception or error.
 *
 * sqlf locator start -dir=sqlf-locator -peer-discovery-address=localhost -peer-discovery-port=16248 -client-bind-address=localhost -client-port=1529 -J-Dsqlfire.debug.true=TraceTran -log-level=fine
 * sqlf server start -dir=sqlf-server -locators=localhost[16248] -client-bind-address=localhost -client-port=1531 -J-Dsqlfire.debug.true=QueryDistribution,TraceTran,TraceQuery -log-level=fine
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.AbstractUserDomainTestSuite
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.spring.data.gemfire.app.dao.UserDao
 * @see org.spring.data.gemfire.app.service.UserService
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@SuppressWarnings("unused")
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("SqlFireTransactionalBatchInsertsTest-hibernate.xml")
//@ContextConfiguration("SqlFireTransactionalBatchInsertsTest-jdbc.xml")
@ContextConfiguration("org/pivotal/sqlfire/SqlFireTransactionalBatchInsertsTest-jpa.xml")
//@ActiveProfiles("hsql")
@ActiveProfiles("sqlf")
public class SqlFireTransactionalBatchInsertsTest extends CommonTransactionalBatchInsertsTest {

  @Autowired
  private UserService userService;

  @Override
  protected UserService getUserService() {
    Assert.state(userService != null, "A reference to a UserService object was not properly configured!");
    return userService;
  }
}
