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

import org.junit.runner.RunWith;
import org.pivotal.gemfire.cache.GemFireGlobalTransactionTest;
import org.spring.data.gemfire.app.service.GemstoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The SpringGemFireGlobalTransactionTest class is a test suite of test cases testing the global transactional behavior
 * of both GemFire and an external transactional resource (e.g. RDBMS such as HSQLDB) using JTA and GemFire's
 * Transaction Manager as the JTA implementation.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.pivotal.gemfire.cache.GemFireGlobalTransactionTest
 * @see org.spring.data.gemfire.app.service.GemstoneService
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class SpringGemFireGlobalTransactionTest extends GemFireGlobalTransactionTest {

  @Autowired
  private GemstoneService gemstoneService;

  @Override
  public void setup() {
  }

  @Override
  public void tearDown() {
    gemstoneService = null;
  }

  @Override
  protected GemstoneService getGemstoneService() {
    return gemstoneService;
  }
}
