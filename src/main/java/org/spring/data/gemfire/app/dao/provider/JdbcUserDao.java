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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.spring.data.gemfire.app.beans.User;
import org.spring.data.gemfire.app.dao.UserDao;
import org.spring.data.gemfire.app.dao.support.BatchingDaoSupportAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * The JdbcUserDao class is
 *
 * @author John Blum
 * @see java.sql.Connection
 * @see java.sql.ResultSet
 * @see java.sql.Statement
 * @see javax.sql.DataSource
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.spring.data.gemfire.app.dao.UserDao
 * @see org.spring.data.gemfire.app.dao.support.BatchingDaoSupportAdapter
 * @see org.springframework.jdbc.support.JdbcUtils
 * @see org.springframework.stereotype.Repository
 * @since 1.0.0
 */
@Repository("userDao")
@SuppressWarnings("unused")
public class JdbcUserDao extends BatchingDaoSupportAdapter<User, String> implements UserDao {

  protected static final String COUNT_USER_SQL = "SELECT DISTINCT count(*) FROM Users";
  protected static final String EXISTS_USER_SQL = "SELECT count(*) FROM Users WHERE username = ?";
  protected static final String FIND_USER_SQL = "SELECT username, email, active, since FROM Users WHERE username = ?";
  protected static final String FIND_ALL_USERS_SQL = "SELECT username, email, active, since FROM Users";
  protected static final String INSERT_USER_SQL = "INSERT INTO Users (username, email, active, since) VALUES (?, ?, ?, ?)";
  protected static final String UPDATE_USER_SQL = "UPDATE Users SET email = ?, active = ?, since = ? WHERE username = ?";

  @Autowired
  private DataSource userDataSource;

  public JdbcUserDao() {
  }

  public JdbcUserDao(final DataSource userDataSource) {
    Assert.notNull(userDataSource, String.format(
      "The JDBC DataSource used by this DAO (%1$s) for data access cannot be null!", getClass().getName()));
    this.userDataSource = userDataSource;
  }

  protected ConnectionBuilder createConnectionBuilder() {
    return new ConnectionBuilder();
  }

  protected DataAccessException createDataAccessException(final String message, final Throwable cause) {
    return new DataAccessException(message, cause) {};
  }

  protected Connection getConnection() {
    try {
      return getDataSource().getConnection();
    }
    catch (SQLException e) {
      throw new CannotGetJdbcConnectionException(String.format("Failed to get JDBC Connection from Data Source (%1$s)!",
        getDataSource().toString()), e);
    }
  }

  protected DataSource getDataSource() {
    Assert.state(userDataSource != null, "A reference to the User DataSource was not properly configured!");
    return userDataSource;
  }

  protected Timestamp convert(final Calendar dateTime) {
    return (dateTime != null ? new Timestamp(dateTime.getTimeInMillis()) : null);
  }

  protected Calendar convert(final Timestamp timestamp) {
    if (timestamp != null) {
      Calendar dateTime = Calendar.getInstance();
      dateTime.clear();
      dateTime.setTime(timestamp);
      return dateTime;
    }

    return null;
  }

  protected User mapUser(final ResultSet resultSet, final int rowIndex) throws SQLException {
    User user = new User(resultSet.getString("username"));
    user.setEmail(resultSet.getString("email"));
    user.setActive(Boolean.valueOf(resultSet.getString("active")));
    user.setSince(convert(resultSet.getTimestamp("since")));
    return user;
  }

  protected PreparedStatement prepareInsert(final PreparedStatement statement, final User user) throws SQLException {
    int parameterIndex = 1;

    statement.setString(parameterIndex++, user.getUsername());
    statement.setString(parameterIndex++, (user.getEmail() != null ? user.getEmail()
      : user.getUsername() + "@yahoo.com"));
    statement.setString(parameterIndex++, String.valueOf(user.isActive()));
    setTimestamp(statement, parameterIndex++, convert(user.getSince()));

    return statement;
  }

  protected PreparedStatement prepareUpdate(final PreparedStatement statement, final User user) throws SQLException {
    int parameterIndex = 1;

    statement.setString(parameterIndex++, (user.getEmail() != null ? user.getEmail()
      : user.getUsername() + "@yahoo.com"));
    statement.setString(parameterIndex++, String.valueOf(user.isActive()));
    setTimestamp(statement, parameterIndex++, convert(user.getSince()));
    statement.setString(parameterIndex++, user.getUsername());

    return statement;
  }

  protected void rollback(final Connection connection) {
    if (connection != null) {
      try {
        connection.rollback();
      }
      catch (SQLException e) {
        throw createDataAccessException("Failed to rollback the Connection!", e);
      }
    }
  }

  protected PreparedStatement setTimestamp(final PreparedStatement statement,
                                           final int parameterIndex,
                                           final Timestamp value)
    throws SQLException
  {
    if (value != null) {
      statement.setTimestamp(parameterIndex, value);
    }
    else {
      statement.setNull(parameterIndex, Types.TIMESTAMP);
    }

    return statement;
  }

  @PostConstruct
  public void init() {
    getDataSource();
    System.out.printf("%1$s initialized!%n", getClass().getSimpleName());
  }

  @Override
  public int count() {
    try {
      Connection connection = createConnectionBuilder().setTransactionIsolation(
        Connection.TRANSACTION_READ_COMMITTED).build();

      Statement statement = connection.createStatement();

      ResultSet resultSet = statement.executeQuery(COUNT_USER_SQL);

      int count = 0;

      if (resultSet != null && resultSet.next()) {
        count = resultSet.getInt(1);
      }

      return count;
    }
    catch (SQLException e) {
      throw createDataAccessException("Failed to count the number of users!", e);
    }
  }

  @Override
  public boolean exists(final String id) {
    try {
      Connection connection = createConnectionBuilder().setTransactionIsolation(
        Connection.TRANSACTION_READ_COMMITTED).build();

      PreparedStatement statement = connection.prepareStatement(EXISTS_USER_SQL);

      statement.setString(1, id);

      ResultSet resultSet = statement.executeQuery();

      return (resultSet != null && resultSet.next() && resultSet.getInt(1) > 0);
    }
    catch (SQLException e) {
      throw createDataAccessException(String.format("Failed to determine if User identified by (%1$s) exists!", id), e);
    }
  }

  @Override
  public List<User> findAll() {
    try {
      List<User> users = new ArrayList<User>();

      Connection connection = createConnectionBuilder().setTransactionIsolation(
        Connection.TRANSACTION_READ_COMMITTED).build();

      Statement statement = connection.createStatement();

      ResultSet resultSet = statement.executeQuery(FIND_ALL_USERS_SQL);

      if (resultSet != null) {
        int rowIndex = 0;

        while (resultSet.next()) {
          users.add(mapUser(resultSet, rowIndex));
        }
      }

      return users;
    }
    catch (SQLException e) {
      throw createDataAccessException("Failed to find all users!", e);
    }
  }

  @Override
  public User save(final User user) {
    final boolean update = exists(user.getUsername());

    final String SQL = (update ? UPDATE_USER_SQL : INSERT_USER_SQL);

    Connection connection = createConnectionBuilder().setAutoCommit(false).setTransactionIsolation(
      Connection.TRANSACTION_READ_COMMITTED).build();

    try {
      PreparedStatement statement = connection.prepareStatement(SQL);

      statement = (update ? prepareUpdate(statement, user) : prepareInsert(statement, user));
      statement.executeUpdate();

      connection.commit();

      return user;
    }
    catch (SQLException e) {
      rollback(connection);

      throw createDataAccessException(String.format("Failed to save (%1$s) User (%2$s)!",
        (update ? "UPDATE" : "INSERT"), user), e);
    }
    finally {
      JdbcUtils.closeConnection(connection);
    }
  }

  public Iterable<User> batchInsert(final Iterable<User> users) {
    Connection connection = createConnectionBuilder().setAutoCommit(false).setTransactionIsolation(
      Connection.TRANSACTION_READ_COMMITTED).build();

    try {
      PreparedStatement statement = connection.prepareStatement(INSERT_USER_SQL);

      for (User user : users) {
        prepareInsert(statement, user);
        statement.executeUpdate();
      }

      connection.commit();

      return users;
    }
    catch (SQLException e) {
      rollback(connection);
      throw createDataAccessException(String.format("Failed to save the Collection of Users (%1$s)!", users), e);
    }
  }

  protected class ConnectionBuilder {

    private final Connection connection;

    public ConnectionBuilder() {
      connection = getConnection();
    }

    protected Connection getRawConnection() {
      Assert.state(connection != null, "The JDBC Connection reference was null!");
      return connection;
    }

    public ConnectionBuilder setAutoCommit(final boolean autoCommit) {
      try {
        getRawConnection().setAutoCommit(autoCommit);
        return this;
      }
      catch (SQLException e) {
        throw new InvalidDataAccessApiUsageException(String.format(
          "Failed to set 'autoCommit' to (%1$s) on the Connection!", autoCommit));
      }
    }

    public ConnectionBuilder setTransactionIsolation(final int isolationLevel) {
      try {
        getRawConnection().setTransactionIsolation(isolationLevel);
        return this;
      }
      catch (SQLException e) {
        throw new InvalidDataAccessApiUsageException(String.format(
          "Failed to set 'transactionIsolation' to (%1$s) on the Connection!", isolationLevel));
      }
    }

    public Connection build() {
      try {
        Assert.state(!getRawConnection().isClosed(), "The Connection has been 'closed'!");
        //Assert.state(getRawConnection().isValid(0), "The Connection is no longer 'valid'!");
        return getRawConnection();
      }
      catch (SQLException e) {
        throw new CannotGetJdbcConnectionException("Failed to build a JDBC Connection!", e);
      }
    }
  }

}
