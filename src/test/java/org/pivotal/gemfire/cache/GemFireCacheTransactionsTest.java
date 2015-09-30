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

import static org.junit.Assert.*;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheTransactionManager;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The GemFireCacheTransactionsTest class is a test suite of test cases testing GemFire Cache Region operations
 * in the context of a GemFire Cache Transaction.
 *
 * @author John Blum
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.CacheTransactionManager
 * @see com.gemstone.gemfire.cache.DataPolicy
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.cache.Scope
 * @see com.gemstone.gemfire.distributed.internal.DistributionConfig
 * @since 1.0.0
 * @link http://gemfire.docs.pivotal.io/latest/userguide/index.html#developing/transactions/run_a_cache_transaction.html
 */
public class GemFireCacheTransactionsTest {

  private static Cache gemfireCache;

  @BeforeClass
  public static void setup() {
    gemfireCache = new CacheFactory()
      .set(DistributionConfig.NAME_NAME, "GemFireCacheTransactionsTest")
      .set(DistributionConfig.MCAST_PORT_NAME, "0")
      .set(DistributionConfig.LOG_LEVEL_NAME, "config")
      .create();

    RegionFactory<Long, String> regionFactory = gemfireCache.createRegionFactory();

    regionFactory.setDataPolicy(DataPolicy.REPLICATE);
    regionFactory.setScope(Scope.DISTRIBUTED_ACK);
    regionFactory.setKeyConstraint(Long.class);
    regionFactory.setValueConstraint(String.class);

    Region<Long, String> example = regionFactory.create("Example");

    assertNotNull("The 'Example' Region was not properly configured and initialized!", example);
    assertEquals("Example", example.getName());
    assertEquals("/Example", example.getFullPath());
    assertTrue(example.isEmpty());

    example.put(0l, "zero");
    example.put(1l, "one");
    example.put(2l, "two");

    assertFalse(example.isEmpty());
    assertEquals(3, example.size());
  }

  @AfterClass
  public static void tearDown() {
    if (gemfireCache != null) {
      gemfireCache.close();
      gemfireCache = null;
    }
  }

  // only rollback on unchecked Exceptions (i.e. RuntimeExceptions), NOT checked Exceptions
  // this is consistent with the Java EE transaction management strategy
  protected <T> T doInTransaction(final Cache gemfireCache, final TransactionCallback<T> callback) {
    CacheTransactionManager cacheTransactionManager = gemfireCache.getCacheTransactionManager();
    boolean rollbackOnly = false;

    cacheTransactionManager.begin();

    try {
      return callback.doInTransaction();
    }
    catch (RuntimeException e) {
      cacheTransactionManager.rollback();
      rollbackOnly = true;
      throw e;
    }
    finally {
      if (!rollbackOnly) {
        cacheTransactionManager.commit();
      }
    }
  }

  @Test
  public void testTransactionalRegionClear() {
    final Region<Long, String> example = gemfireCache.getRegion("/Example");

    assertNotNull(example);
    assertFalse(example.isEmpty());
    assertEquals(3, example.size());

    try {
      doInTransaction(gemfireCache, new TransactionCallback<Object>() {
        @Override public Object doInTransaction() {
          example.clear();
          throw new IllegalStateException("cause transaction to rollback");
        }
      });
    }
    catch (IllegalStateException ignore) {
      // expected
    }

    assertFalse(example.isEmpty());
    assertEquals(3, example.size());

    doInTransaction(gemfireCache, new TransactionCallback<Void>() {
      @Override public Void doInTransaction() {
        example.clear();
        return null;
      }
    });

    assertTrue(example.isEmpty());
  }

  protected static interface TransactionCallback<T> {
    T doInTransaction();
  }

}
