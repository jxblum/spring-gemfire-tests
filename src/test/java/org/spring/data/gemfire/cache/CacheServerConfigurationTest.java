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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import javax.annotation.Resource;

import org.apache.geode.cache.server.CacheServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The CacheServerConfigurationTest class is a test suite of test cases testing the contract and functionality of
 * GemFire Cache Server configuration using Spring Data GemFire's XML namespace configuration meta-data.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class CacheServerConfigurationTest {

  @Autowired
  private CacheServer cacheServer;

  @Resource(name = "gemfireCacheServerProperties")
  private Map<String, Object> gemfireCacheServerProperties;

  @SuppressWarnings("unchecked")
  protected String get(final String propertyName) {
    return String.valueOf(gemfireCacheServerProperties.get(propertyName));
  }

  @Test
  public void cacheServerConfiguration() throws Exception {
    assertNotNull("The GemFire CacheServer was not properly configured and initialized!", cacheServer);
    assertTrue(cacheServer.isRunning());
    assertEquals(get("gemfire.cache.server.bind-address"), cacheServer.getBindAddress());
    assertEquals(Integer.parseInt(get("gemfire.cache.server.port")), cacheServer.getPort());
    assertEquals(get("gemfire.cache.server.hostname-for-clients"), cacheServer.getHostnameForClients());
    assertEquals(Long.parseLong(get("gemfire.cache.server.load-poll-interval")), cacheServer.getLoadPollInterval());
    assertEquals(Integer.parseInt(get("gemfire.cache.server.max-connections")), cacheServer.getMaxConnections());
    assertEquals(Integer.parseInt(get("gemfire.cache.server.max-message-count")), cacheServer.getMaximumMessageCount());
    assertEquals(Integer.parseInt(get("gemfire.cache.server.max-threads")), cacheServer.getMaxThreads());
    assertEquals(Integer.parseInt(get("gemfire.cache.server.max-time-between-pings")), cacheServer.getMaximumTimeBetweenPings());
    assertEquals(Integer.parseInt(get("gemfire.cache.server.message-time-to-live")), cacheServer.getMessageTimeToLive());
    assertEquals(Integer.parseInt(get("gemfire.cache.server.socket-buffer-size")), cacheServer.getSocketBufferSize());
  }

}
