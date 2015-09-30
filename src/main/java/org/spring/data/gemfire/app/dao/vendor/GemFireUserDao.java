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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;

import org.spring.data.gemfire.app.beans.User;
import org.spring.data.gemfire.app.dao.UserDao;
import org.springframework.stereotype.Repository;

/**
 * The GemFireUserDao class...
 *
 * @author John Blum
 * @see
 * @since 7.x
 */
@Repository("userDao")
public class GemFireUserDao implements UserDao {

  @Resource(name = "Users")
  private Region<String, User> users;

  @Override
  public List<User> findAll() {
    return new ArrayList<User>(users.values());
  }

  @Override
  public int count() {
    return users.size();
  }

  @Override
  public boolean exists(final String key) {
    return users.containsKey(key);
  }

  @Override
  public User findBy(final String key) {
    return users.get(key);
  }

  @Override
  public boolean remove(final User user) {
    return (users.remove(user.getUsername()) != null);
  }

  @Override
  public User save(final User user) {
    users.put(user.getUsername(), user);
    return user;
  }

}
