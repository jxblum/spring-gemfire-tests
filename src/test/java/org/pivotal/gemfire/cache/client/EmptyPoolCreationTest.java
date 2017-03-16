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

package org.pivotal.gemfire.cache.client;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolFactory;
import org.apache.geode.cache.client.PoolManager;
import org.apache.geode.distributed.DistributedSystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The EmptyPoolCreationTest class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class EmptyPoolCreationTest {

  static DistributedSystem distributedSystem;

  @BeforeClass
  @SuppressWarnings("deprecation")
  public static void setupDistributedSystem() {
    distributedSystem = DistributedSystem.connect(null);
  }

  @AfterClass
  @SuppressWarnings("deprecation")
  public static void tearDownDistributedSystem() {
    if (distributedSystem != null) {
      distributedSystem.disconnect();
    }
  }

  @Test
  public void emptyPool() {
    Pool pool = null;

    try {
      PoolFactory poolFactory = PoolManager.createFactory();

      poolFactory.setFreeConnectionTimeout(5000);
      poolFactory.setIdleTimeout(120000l);
      poolFactory.setLoadConditioningInterval(300000);
      poolFactory.setMaxConnections(50);
      poolFactory.setMinConnections(5);
      poolFactory.setMultiuserAuthentication(false);
      poolFactory.setPingInterval(15000l);
      poolFactory.setPRSingleHopEnabled(true);
      poolFactory.setReadTimeout(20000);
      poolFactory.setRetryAttempts(1);
      poolFactory.setServerGroup("TestGroup");
      poolFactory.setSocketBufferSize(PoolFactory.DEFAULT_SOCKET_BUFFER_SIZE);
      poolFactory.setSubscriptionAckInterval(500);
      poolFactory.setSubscriptionEnabled(true);
      poolFactory.setSubscriptionMessageTrackingTimeout(20000);
      poolFactory.setSubscriptionRedundancy(2);
      poolFactory.setThreadLocalConnections(false);
      //poolFactory.addServer("localhost", 40404);

      pool = poolFactory.create("TestPool");

      assertThat(pool.getFreeConnectionTimeout(), is(equalTo(5000)));
      assertThat(pool.getIdleTimeout(), is(equalTo(120000l)));
      assertThat(pool.getLoadConditioningInterval(), is(equalTo(300000)));
      assertThat(pool.getLocators().isEmpty(), is(true));
      assertThat(pool.getMaxConnections(), is(equalTo(50)));
      assertThat(pool.getMinConnections(), is(equalTo(5)));
      assertThat(pool.getMultiuserAuthentication(), is(equalTo(false)));
      assertThat(pool.getName(), is(equalTo("TestPool")));
      assertThat(pool.getPingInterval(), is(equalTo(15000l)));
      assertThat(pool.getPRSingleHopEnabled(), is(equalTo(true)));
      assertThat(pool.getReadTimeout(), is(equalTo(20000)));
      assertThat(pool.getRetryAttempts(), is(equalTo(1)));
      assertThat(pool.getServerGroup(), is(equalTo("TestGroup")));
      assertThat(pool.getServers().isEmpty(), is(true));
      //assertThat(pool.getServers().size(), is(equalTo(1)));
      assertThat(pool.getSocketBufferSize(), is(equalTo(PoolFactory.DEFAULT_SOCKET_BUFFER_SIZE)));
      assertThat(pool.getSubscriptionAckInterval(), is(equalTo(500)));
      assertThat(pool.getSubscriptionEnabled(), is(equalTo(true)));
      assertThat(pool.getSubscriptionMessageTrackingTimeout(), is(equalTo(20000)));
      assertThat(pool.getSubscriptionRedundancy(), is(equalTo(2)));
      assertThat(pool.getThreadLocalConnections(), is(equalTo(false)));
    }
    finally {
      if (pool != null) {
        pool.destroy();
      }
    }
  }

}
