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

package org.pivotal.gemfire.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spring.data.gemfire.app.beans.Gemstone;
import org.spring.data.gemfire.app.dao.GemstoneDao;
import org.spring.data.gemfire.app.dao.provider.DatabaseGemstoneDao;
import org.spring.data.gemfire.app.dao.vendor.GemFireGemstoneDao;
import org.spring.data.gemfire.app.service.GemstoneService;
import org.spring.data.gemfire.app.service.GemstoneService.IllegalGemstoneException;
import org.spring.data.gemfire.app.service.vendor.GemFireGemstoneService;

/**
 * The GemFireGlobalTransactionTest class is a test suite of test cases testing the global transactional behavior
 * of both GemFire and an external transactional resource (e.g. RDBMS such as HSQLDB) using JTA and GemFire's
 * Transaction Manager as the JTA implementation.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.spring.data.gemfire.app.beans.Gemstone
 * @see org.spring.data.gemfire.app.dao.provider.DatabaseGemstoneDao
 * @see org.spring.data.gemfire.app.dao.vendor.GemFireGemstoneDao
 * @see org.spring.data.gemfire.app.service.GemstoneService
 * @see org.spring.data.gemfire.app.service.vendor.GemFireGemstoneService
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.CacheFactory
 * @since 1.4.0
 */
@SuppressWarnings("unused")
public class GemFireGlobalTransactionTest {

  protected static final AtomicLong ID_GENERATOR = new AtomicLong(1);

  private Cache gemfireCache;

  private GemstoneService gemstoneService;

  protected static Gemstone createGemstone(final String name) {
    return createGemstone(ID_GENERATOR.getAndIncrement(), name);
  }

  protected static Gemstone createGemstone(final Long id, final String name) {
    return new Gemstone(id, name);
  }

  protected static <T> List<T> toList(final Iterable<T> iterable) {
    List<T> list = new ArrayList<T>();

    if (iterable != null) {
      for (T item : iterable) {
        list.add(item);
      }
    }

    return list;
  }

  protected static String toString(Iterable<Gemstone> gemstones) {
    StringBuilder buffer = new StringBuilder("[");
    int count = 0;

    for (Gemstone gemstone : gemstones) {
      buffer.append(++count > 1 ? ", " : "").append(toString(gemstone));
    }

    buffer.append("]");

    return buffer.toString();
  }

  protected static String toString(final Gemstone... gemstones) {
    return Arrays.toString(gemstones);
  }

  protected static String toString(final Gemstone gemstone) {
    return gemstone.getName();
  }

  @Before
  public void setup() {
    gemfireCache = new CacheFactory()
      .set("name", "G1")
      .set("cache-xml-file", "gemfire-global-transaction-cache.xml")
      .set("log-level", "config")
      .set("mcast-port", "0")
      .create();

    GemstoneDao databaseGemstoneDao = new DatabaseGemstoneDao(gemfireCache);
    GemstoneDao gemfireGemstoneDao = new GemFireGemstoneDao(gemfireCache.<Long, String>getRegion("Gemstones"));

    databaseGemstoneDao.init();
    gemfireGemstoneDao.init();

    gemstoneService = new GemFireGemstoneService(gemfireCache, databaseGemstoneDao, gemfireGemstoneDao);
    gemstoneService.init();
  }

  @After
  public void tearDown() {
    gemfireCache.close();
    gemfireCache = null;
    gemstoneService = null;
  }

  protected GemstoneService getGemstoneService() {
    return gemstoneService;
  }

  @Test
  public void testGlobalTransactionConfiguration() throws Exception {
    try {
      assertNotNull("A reference to the GemsService was not properly configured!", getGemstoneService());

      assertEquals(0, getGemstoneService().countFromDatabase());
      assertEquals(0, getGemstoneService().countFromGemFire());

      getGemstoneService().save(createGemstone("DIAMOND"));
      getGemstoneService().save(createGemstone("RUBY"));

      assertEquals(2, getGemstoneService().countFromDatabase());
      assertEquals(2, getGemstoneService().countFromGemFire());

      try {
        getGemstoneService().save(createGemstone("Coal"));
        fail("'Coal' is not a valid gemstone!");
      }
      catch (IllegalGemstoneException expected) {
        assertEquals("'Coal' is not a valid gemstone!", expected.getMessage());
      }

      assertEquals(2, getGemstoneService().countFromDatabase());
      assertEquals(2, getGemstoneService().countFromGemFire());

      Gemstone expectedPearl = getGemstoneService().save(createGemstone("Pearl"));
      getGemstoneService().save(createGemstone("sapphire"));

      assertEquals(4, getGemstoneService().countFromDatabase());
      assertEquals(4, getGemstoneService().countFromGemFire());

      try {
        getGemstoneService().save(createGemstone("Quartz"));
        fail("'Quartz' is not a valid gemstone!");
      }
      catch (IllegalGemstoneException expected) {
        assertEquals("'Quartz' is not a valid gemstone!", expected.getMessage());
      }

      assertEquals(4, getGemstoneService().countFromDatabase());
      assertEquals(4, getGemstoneService().countFromGemFire());

      Gemstone databasePearl = getGemstoneService().loadFromDatabase(expectedPearl.getId());

      System.out.printf("Database Pearl (%1$s)%n", databasePearl);

      assertNotNull(databasePearl);
      assertEquals(expectedPearl, databasePearl);

      Gemstone gemfirePearl = getGemstoneService().loadFromGemFire(expectedPearl.getId());

      System.out.printf("GemFire Pearl (%1$s)%n", gemfirePearl);

      assertNotNull(gemfirePearl);
      assertEquals(expectedPearl, gemfirePearl);

      List<Gemstone> databaseGemstones = toList(getGemstoneService().listFromDatabase());
      List<Gemstone> gemfireGemstones = toList(getGemstoneService().listFromGemFire());

      assertFalse(databaseGemstones.isEmpty());
      assertFalse(gemfireGemstones.isEmpty());
      assertEquals(databaseGemstones.size(), gemfireGemstones.size());
      assertTrue(databaseGemstones.containsAll(gemfireGemstones));
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
      throw e;
    }
    finally {
      System.out.printf("MySQL 'Gemstones' Table contents (%1$s)%n",
        toString(getGemstoneService().listFromDatabase()));
      System.out.printf("GemFire 'Gemstones' Cache Region contents (%1$s)%n",
        toString(getGemstoneService().listFromGemFire()));
    }
  }
}
