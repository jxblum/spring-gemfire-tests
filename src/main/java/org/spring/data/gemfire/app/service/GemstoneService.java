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
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.spring.data.gemfire.app.beans.Gemstone;
import org.spring.data.gemfire.app.dao.GemstoneDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * The GemstoneService class is a Service object implementing business logic and rules, along with data services
 * on Gemstone domain objects.
 *
 * @author John Blum
 * @see org.spring.data.gemfire.app.beans.Gemstone
 * @see org.spring.data.gemfire.app.dao.GemstoneDao
 * @see org.springframework.stereotype.Service
 * @see org.springframework.transaction.annotation.Transactional
 * @since 1.0.0
 */
@Service("gemsService")
@SuppressWarnings("unused")
public class GemstoneService {

  protected static final List<String> APPROVED_GEMS = new ArrayList<String>(Arrays.asList(
    "ALEXANDRITE", "AQUAMARINE", "DIAMOND", "OPAL", "PEARL", "RUBY", "SAPPHIRE", "SPINEL", "TOPAZ"));

  @Resource(name = "databaseGemsDao")
  private GemstoneDao databaseGemstoneDao;

  @Resource(name = "gemfireGemsDao")
  private GemstoneDao gemfireGemstoneDao;

  public GemstoneService() {
  }

  public GemstoneService(final GemstoneDao databaseGemstoneDao, final GemstoneDao gemfireGemstoneDao) {
    Assert.notNull(databaseGemstoneDao, "The 'Database' GemsDao reference must not be null!");
    Assert.notNull(gemfireGemstoneDao, "The 'GemFire' GemsDao reference must not be null!");
    this.databaseGemstoneDao = databaseGemstoneDao;
    this.gemfireGemstoneDao = gemfireGemstoneDao;
  }

  protected GemstoneDao getDatabaseGemstoneDao() {
    Assert.state(databaseGemstoneDao != null, "A reference to the 'Database' GemsDao was not properly configured!");
    return databaseGemstoneDao;
  }

  protected GemstoneDao getGemFireGemsDao() {
    Assert.state(gemfireGemstoneDao != null , "A reference to the 'GemFire' GemsDao was not properly configured!");
    return gemfireGemstoneDao;
  }

  @PostConstruct
  public void init() {
    getDatabaseGemstoneDao();
    getGemFireGemsDao();
    System.out.printf("%1$s initialized!%n", getClass().getSimpleName());
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public int countFromDatabase() {
    return getDatabaseGemstoneDao().count();
  }

  // NOTE GemFire does not allow Region.size() within a Transactional context even when the Transaction is read-only.
  //@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  //@Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Throwable.class)
  public int countFromGemFire() {
    return getGemFireGemsDao().count();
  }

  @Transactional(readOnly = true)
  public Iterable<Gemstone> listFromDatabase() {
    return getDatabaseGemstoneDao().findAll();
  }

  // NOTE GemFire does not allow Region.getAll(Region.keySet()) within a Transactional context even when
  // the Transaction is read-only.
  //@Transactional(readOnly = true)
  public Iterable<Gemstone> listFromGemFire() {
    return getGemFireGemsDao().findAll();
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public Gemstone loadFromDatabase(final Long id) {
    return getDatabaseGemstoneDao().findBy(id);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public Gemstone loadFromGemFire(final Long key) {
    return getGemFireGemsDao().findBy(key);
  }

  @Transactional(readOnly = false)
  public Gemstone save(Gemstone gemstone) {
    gemstone = getGemFireGemsDao().save(getDatabaseGemstoneDao().save(gemstone));

    // NOTE deliberate, but stupid and naive business validation after the mutating data access!
    if (!APPROVED_GEMS.contains(gemstone.getName().toUpperCase())) {
      // NOTE if the gemstone is not valid, blow chunks (should cause transaction to rollback for GemFire and Database)!
      throw new IllegalGemstoneException(String.format("'%1$s' is not a valid gemstone!", gemstone.getName()));
    }

    return gemstone;
  }

  public static final class IllegalGemstoneException extends IllegalArgumentException {

    public IllegalGemstoneException() {
    }

    public IllegalGemstoneException(final String message) {
      super(message);
    }

    public IllegalGemstoneException(final Throwable cause) {
      super(cause);
    }

    public IllegalGemstoneException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }

}
