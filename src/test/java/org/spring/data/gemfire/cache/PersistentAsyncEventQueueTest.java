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

import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The PersistentAsyncEventQueueTest class tests GemFire AsyncEventListener, persistent Async Event Queue, Disk Store,
 * and Region functionality in Spring Data GemFire for JIRA issue SGF-198.
 *
 * @author John Blum
 * @see org.junit.Assert
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 7.x
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class PersistentAsyncEventQueueTest extends AbstractGemFireTest {

  @Resource(name = "Customers")
  private Region customers;

  @Resource(name = "Accounts")
  private Region accounts;

  @Test
  public void testDiskStoreAsyncEventQueueAndRegionsCreatedSuccessfully() {
    assertRegion(customers, "Customers", "/Customers");
    assertRegion(accounts, "Accounts", "/Accounts");
  }

}
