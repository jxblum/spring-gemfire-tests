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

package org.spring.data.gemfire.app.service.vendor;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.geode.cache.Cache;
import org.spring.data.gemfire.app.beans.Gemstone;
import org.spring.data.gemfire.app.dao.GemstoneDao;
import org.spring.data.gemfire.app.service.GemstoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * The GemFireGemsService class is a Service object implementing business logic and rules, along with data services
 * on Gemstone domain objects.
 *
 * @author John Blum
 * @see javax.transaction.UserTransaction
 * @see org.spring.data.gemfire.app.beans.Gemstone
 * @see org.spring.data.gemfire.app.dao.GemstoneDao
 * @see org.spring.data.gemfire.app.service.GemstoneService
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GemFireGemstoneService extends GemstoneService {

  public static final String USER_TRANSACTION_JNDI_LOCATION = "java:/UserTransaction";

  @Autowired
  private Cache gemfireCache;

  protected static void rollback(final UserTransaction tx) {
    try {
      if (tx != null) {
        tx.rollback();
      }
    }
    catch (SystemException e) {
      throw new RuntimeException("Failed to rollback the UserTransaction!", e);
    }
  }

  public GemFireGemstoneService() {
  }

  public GemFireGemstoneService(final Cache gemfireCache,
                                final GemstoneDao databaseGemstoneDao,
                                final GemstoneDao gemfireGemstoneDao) {
    super(databaseGemstoneDao, gemfireGemstoneDao);
    Assert.notNull(gemfireCache, "The GemFire Cache reference must not be null!");
    this.gemfireCache = gemfireCache;
  }

  protected Cache getCache() {
    Assert.state(gemfireCache != null, "A reference to the GemFire Cache was not properly configured!");
    return gemfireCache;
  }

  protected <IN, OUT> OUT doInTransaction(final TransactionCallback<IN, OUT> callback, IN parameter) {
    UserTransaction userTransaction = null;

    try {
      userTransaction = (UserTransaction) getCache().getJNDIContext().lookup(USER_TRANSACTION_JNDI_LOCATION);
      userTransaction.begin();

      OUT returnValue = callback.doInTransaction(parameter);

      userTransaction.commit();

      return returnValue;
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
      rollback(userTransaction);

      if (e instanceof IllegalGemstoneException) {
        throw (IllegalGemstoneException) e;
      }
      else {
        throw new RuntimeException(String.format("Transaction failed due to unexpected Exception: %1$s!",
          e.getMessage()), e);
      }
    }
  }

  @Override
  public void init() {
    getCache();
    super.init();
  }

  @Override
  public int countFromGemFire() {
    /*
    return doInTransaction(new TransactionCallback<Object, Integer>() {
      @Override public Integer doInTransaction(final Object object) {
        return GemFireGemsService.super.countFromGemFire();
      }
    }, null);
    */
    return super.countFromGemFire();
  }

  @Override
  public Iterable<Gemstone> listFromGemFire() {
    // NOTE GemFire does not allow Region.getAll(Region.keySet()) within a Transactional context even when
    // the Transaction is read-only.
    /*
    return doInTransaction(new TransactionCallback<Object, Iterable<Gemstone>>() {
      @Override public Iterable<Gemstone> doInTransaction(final Object object) {
        return GemFireGemsService.super.listFromGemFire();
      }
    }, null);
    */
    return super.listFromGemFire();
  }

  @Override
  public Gemstone loadFromGemFire(final Long key) {
    return doInTransaction(new TransactionCallback<Long, Gemstone>() {
      @Override public Gemstone doInTransaction(final Long key) {
        return GemFireGemstoneService.super.loadFromGemFire(key);
      }
    }, key);
  }

  @Override
  public Gemstone save(final Gemstone gemstone) {
    return doInTransaction(new TransactionCallback<Gemstone, Gemstone>() {
      @Override public Gemstone doInTransaction(final Gemstone gemstone) {
        return GemFireGemstoneService.super.save(gemstone);
      }
    }, gemstone);
  }

  protected static interface TransactionCallback<IN, OUT> {
    public OUT doInTransaction(IN object);
  }

}
