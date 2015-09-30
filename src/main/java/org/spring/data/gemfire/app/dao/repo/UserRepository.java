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

package org.spring.data.gemfire.app.dao.repo;

import java.util.List;

import org.spring.data.gemfire.app.beans.User;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.data.gemfire.repository.Query;

/**
 * The UserRepository class is a Data Access Object (DAO) for accessing and persisting Users as well as performing
 * Queries.
 *
 * @author John Blum
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.springframework.data.gemfire.repository.GemfireRepository
 * @see org.springframework.data.gemfire.repository.Query
 * @since 1.0.0
 * @since 1.3.3 (Spring Data GemFire)
 * @since 7.0.1 (GemFire)
 */
@SuppressWarnings("unused")
public interface UserRepository extends GemfireRepository<User, String> {

  // Query for Active Users
  //@Query("SELECT DISTINCT u FROM /Users u WHERE u.active = true")
  List<User> findDistinctByActiveTrue();

  // Query for Inactive Users
  //@Query("SELECT DISTINCT u FROM /Users u WHERE u.active = false")
  List<User> findDistinctByActiveFalse();

  // Query for Users by (like) Name
  List<User> findDistinctByUsernameLike(String username);

  // NOTE the 'NOT LIKE' operator is unsupported in GemFire's OQL/Query syntax
  //public List<User> findDistinctByUsernameNotLike(String username);

  @Query("<trace> SELECT DISTINCT u FROM /Users u, u.addresses a WHERE a.city = $1")
  //@Query("<trace> SELECT DISTINCT u, a FROM /Users u, u.addresses a WHERE a.city = $1")
  //@Query("IMPORT org.spring.data.gemfire.app.beans.Address; SELECT DISTINCT u, a FROM /Users u, u.addresses a TYPE Address WHERE a.city = $1")
  List<User> findUsersInCity(String city);

  @Query("SELECT count(*) FROM /Users u WHERE u.username LIKE $1")
  Integer countUsersByUsernameLike(String username);

}
