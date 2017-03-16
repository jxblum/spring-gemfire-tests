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

package org.spring.data.gemfire.cache;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.LoaderHelper;
import org.codeprimate.sql.DataSourceAdapter;
import org.spring.data.gemfire.app.beans.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.support.LazyWiringDeclarableSupport;
import org.springframework.util.Assert;

/**
 * The UserDataStoreCacheLoader class is an implementation of GemFire's CacheLoader component for loading Users into
 * the GemFire Users Region on Cache misses.
 *
 * @author John Blum
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.springframework.data.gemfire.LazyWiringDeclarableSupport
 * @see org.apache.geode.cache.CacheLoader
 * @since 1.3.4 (Spring Data GemFire)
 * @since 7.0.1 (GemFire)
 */
@SuppressWarnings("unused")
public class UserDataStoreCacheLoader extends LazyWiringDeclarableSupport implements CacheLoader<String, User> {

  private static final Map<String, User> USER_DATA = new HashMap<String, User>(11);

  static {
    USER_DATA.put("bschuchardt", createUser("bschuchardt", true, createCalendar(1984, Calendar.JANUARY, 17)));
    USER_DATA.put("dhoots", createUser("dhoots", true, createCalendar(2012, Calendar.MARCH, 31)));
    USER_DATA.put("dschneider", createUser("dschneider", true, createCalendar(1994, Calendar.APRIL, 1)));
    USER_DATA.put("dsmith", createUser("dsmith", true, createCalendar(2008, Calendar.JULY, 4)));
    USER_DATA.put("jblum", createUser("jblum", true, createCalendar(2011, Calendar.MAY, 31)));
    USER_DATA.put("klund", createUser("klund", true, createCalendar(2011, Calendar.JANUARY, 15)));
    USER_DATA.put("rholmes", createUser("rholmes", true, createCalendar(2012, Calendar.MARCH, 31)));
    USER_DATA.put("sagarwal", createUser("sagarwal", true, createCalendar(2011, Calendar.MARCH, 15)));
    USER_DATA.put("sbansod", createUser("sbansod", false, createCalendar(2012, Calendar.JANUARY, 5)));
  }

  @Autowired
  private DataSource userDataSource;

  protected static Calendar createCalendar(int year, int month, int dayOfMonth) {
    return createCalendar(year, month, dayOfMonth, 0, 0, 0);
  }

  protected static Calendar createCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
    Calendar dateTime = Calendar.getInstance();

    dateTime.clear();
    dateTime.set(Calendar.YEAR, year);
    dateTime.set(Calendar.MONTH, month);
    dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    dateTime.set(Calendar.HOUR_OF_DAY, dayOfMonth);
    dateTime.set(Calendar.MINUTE, minute);
    dateTime.set(Calendar.SECOND, second);

    return dateTime;
  }

  protected static User createUser(String username) {
    return createUser(username, String.format("%1$s@xcompay.com", username), true, Calendar.getInstance());
  }

  protected static User createUser(String username, Calendar since) {
    return createUser(username, String.format("%1$s@xcompay.com", username), true, since);
  }

  protected static User createUser(String username, Boolean active, Calendar since) {
    return createUser(username, String.format("%1$s@xcompay.com", username), active, since);
  }

  protected static User createUser(String username, String email, Boolean active, Calendar since) {
    User user = new User(username);
    user.setActive(active);
    user.setEmail(email);
    user.setSince(since);
    return user;
  }

  protected DataSource getDataSource() {
    Assert.state(userDataSource != null, "A reference to the User DataSource was not properly configured!");
    return userDataSource;
  }

  @PostConstruct
  public void init() {
    getDataSource();
    System.out.printf("%1$s initialized!", this.getClass().getName());
  }

  @Override
  public User load(LoaderHelper<String, User> helper) throws CacheLoaderException {
    System.out.printf("Reading value for key (%1$s) in Regin (%2$s)...%n",
      helper.getKey(), helper.getRegion().getName());

    if (helper.getRegion().getRegionService() instanceof Cache) {
      ((Cache) helper.getRegion().getRegionService()).getLogger().info(String.format(
        "Reading value for key (%1$s) in Regin (%2$s)...%n", helper.getKey(), helper.getRegion().getName()));
    }

    return USER_DATA.get(helper.getKey());
  }

  @Override
  public void close() {
    USER_DATA.clear();
  }

  public static final class TestDataSource extends DataSourceAdapter {
  }
}
