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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.spring.data.gemfire.app.beans.User;
import org.spring.data.gemfire.app.dao.provider.HibernateUserDao;
import org.spring.data.gemfire.app.service.UserService;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.util.Assert;

/**
 * The HibernateTransactionalBatchInsertsTest class...
 *
 * sqlf locator start -dir=sqlf-locator -peer-discovery-address=localhost -peer-discovery-port=16248 -client-bind-address=localhost -client-port=1529 -J-Dsqlfire.debug.true=TraceTran -log-level=fine
 * sqlf server start -dir=sqlf-server -locators=localhost[16248] -client-bind-address=localhost -client-port=1531 -J-Dsqlfire.debug.true=QueryDistribution,TraceTran,TraceQuery -log-level=fine
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.pivotal.sqlfire.CommonTransactionalBatchInsertsTest
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.spring.data.gemfire.app.dao.UserDao;
 * @see org.spring.data.gemfire.app.service.UserService;
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class HibernateTransactionalBatchInsertsTest extends CommonTransactionalBatchInsertsTest {

  protected static Properties createHibernateConfigurationSettings() {
    Properties hibernateConfigurationSettings = new Properties();

    hibernateConfigurationSettings.setProperty("hibernate.dialect", "com.vmware.sqlfire.hibernate.SQLFireDialect");
    hibernateConfigurationSettings.setProperty("hibernate.show_sql", "true");
    hibernateConfigurationSettings.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
    //hibernateConfigurationSettings.setProperty("hibernate.connection.autocommit", "false");
    hibernateConfigurationSettings.setProperty("hibernate.connection.isolation", String.valueOf(Connection.TRANSACTION_READ_COMMITTED));
    hibernateConfigurationSettings.setProperty("hibernate.connection.driver_class", "com.vmware.sqlfire.jdbc.ClientDriver");
    hibernateConfigurationSettings.setProperty("hibernate.connection.url", "jdbc:sqlfire://localhost:1529/");
    hibernateConfigurationSettings.setProperty("hibernate.connection.username", "app");
    hibernateConfigurationSettings.setProperty("hibernate.connection.password", "app");
    hibernateConfigurationSettings.setProperty("hibernate.connection.pool_size", "1");
    hibernateConfigurationSettings.setProperty("hibernate.hbm2ddl.auto", "update");
    hibernateConfigurationSettings.setProperty("hibernate.hbm2ddl.import_files", "/data/source/schema/create-user-table.sqlf.ddl.sql");

    return hibernateConfigurationSettings;
  }

  protected static SessionFactory createSessionFactoryUsingConfiguration() {
    System.out.printf("Using Hibernate's Configuration class to configure Hibernate!%n");

    return new Configuration()
      .addAnnotatedClass(User.class)
      .setProperties(createHibernateConfigurationSettings())
      .buildSessionFactory();
  }

  protected static SessionFactory createSessionFactoryUsingLocalSessionFactoryBean() {
    System.out.printf("Using Spring's LocalSessionFactoryBean class to configure Hibernate!%n");

    try {
      LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();

      factoryBean.setAnnotatedClasses(User.class);
      factoryBean.setHibernateProperties(createHibernateConfigurationSettings());
      factoryBean.afterPropertiesSet();

      return factoryBean.getObject();
    }
    catch (IOException e) {
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      fail(writer.toString());
      throw new RuntimeException(e);
    }
  }

  private SessionFactory sessionFactory;

  private UserService userService;

  @Before
  public void setup() {
    //sessionFactory = createSessionFactoryUsingConfiguration();
    sessionFactory = createSessionFactoryUsingLocalSessionFactoryBean();
    userService = new UserService(new HibernateUserDao(sessionFactory));
  }

  @After
  public void tearDown() {
    sessionFactory.close();
    sessionFactory = null;
    userService = null;
  }

  @Override
  protected UserService getUserService() {
    Assert.state(userService != null, "A reference to the UserService was not properly configured and initialized!");
    return userService;
  }

}
