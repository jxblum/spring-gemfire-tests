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
import java.util.Map;

import com.vmware.sqlfire.hibernate.SQLFireDialect;

import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * The SQLFireHibernateJpaVendorAdapter class...
 *
 * @author John Blum
 * @see org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SQLFireHibernateJpaVendorAdapter extends HibernateJpaVendorAdapter {

  @Override
  public Map<String, Object> getJpaPropertyMap() {
    Map<String, Object> jpaPropertyMap = super.getJpaPropertyMap();
    jpaPropertyMap.put("hibernate.connection.isolation", String.valueOf(Connection.TRANSACTION_READ_COMMITTED));
    return jpaPropertyMap;
  }

  @Override
  protected Class determineDatabaseDialectClass(final Database database) {
    /*
    Class databaseDialectClass = super.determineDatabaseDialectClass(database);
    return (databaseDialectClass != null ? databaseDialectClass : SQLFireDialect.class);
    */
    return SQLFireDialect.class;
  }

}
