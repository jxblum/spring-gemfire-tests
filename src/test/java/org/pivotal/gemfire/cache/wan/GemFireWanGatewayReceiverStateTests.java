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

package org.pivotal.gemfire.cache.wan;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.wan.GatewayReceiver;
import com.gemstone.gemfire.cache.wan.GatewayReceiverFactory;

import org.junit.After;
import org.junit.Test;

/**
 * The GemFireWanGatewayReceiverStateTests class is a test suite of test cases testing the contract and functionality
 * of GemFire's WAN GatewayReceiver startup behavior and functionality.
 * @author John Blum
 * @see org.junit.Test
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.CacheFactory
 * @see com.gemstone.gemfire.cache.wan.GatewayReceiver
 * @see com.gemstone.gemfire.cache.wan.GatewayReceiverFactory
 * @since 1.0.0
 */
public class GemFireWanGatewayReceiverStateTests {

  private Cache cache;

  @After
  public void tearDown() {
    closeCache(cache);
  }

  protected Cache createCache(final String name) {
    return new CacheFactory()
      .set("name", getClass().getSimpleName().concat("_").concat(name))
      .set("mcast-port", "0")
      .set("log-level", "config")
      .create();
  }

  protected GatewayReceiver createGatewayReceiver(final Cache cache,
                                                  final String bindAddress,
                                                  final String hostnameForSenders,
                                                  final int startPort,
                                                  final int endPort,
                                                  final long maxTimeBetweenPings,
                                                  final int socketBufferSize)
  {
    assertNotNull("The GemFire 'Cache' instance must not be null!", cache);

    GatewayReceiverFactory gatewayReceiverFactory = cache.createGatewayReceiverFactory();

    gatewayReceiverFactory.setBindAddress(bindAddress);
    gatewayReceiverFactory.setHostnameForSenders(hostnameForSenders);
    gatewayReceiverFactory.setStartPort(startPort);
    gatewayReceiverFactory.setEndPort(endPort);
    gatewayReceiverFactory.setMaximumTimeBetweenPings((int) maxTimeBetweenPings);
    gatewayReceiverFactory.setSocketBufferSize(socketBufferSize);

    return gatewayReceiverFactory.create();
  }

  protected void closeCache(final Cache cache) {
    if (cache != null) {
      cache.close();
    }
  }

  protected void stopGatewayReceiver(final GatewayReceiver gatewayReceiver) {
    if (gatewayReceiver != null) {
      gatewayReceiver.stop();
    }
  }

  // TEST for Trac bug #51367
  @Test(timeout = 30000) // 30 Second Timeout
  public void testGatewayReceiverStart() {
    GatewayReceiver gatewayReceiver = null;

    try {
      cache = createCache("testGatewayReceiverConfiguration");

      assertNotNull("The GemFire 'Cache' instance was not properly configured and initialized!", cache);

      // The "bug" (hang) is to set the bind address to an invalid IP address not matching any NIC on the host System.
      gatewayReceiver = createGatewayReceiver(cache, "10.224.112.76", "skullbox", 1024, 8192,
        TimeUnit.SECONDS.toMillis(5), 32768);

      assertNotNull("The 'GatewayReceiver' was not properly configure and initialized!", gatewayReceiver);

      gatewayReceiver.start();

      assertEquals("10.224.112.76", gatewayReceiver.getBindAddress());
      assertEquals("skullbox", gatewayReceiver.getHost());
      assertEquals(1024, gatewayReceiver.getStartPort());
      assertEquals(8192, gatewayReceiver.getEndPort());
      assertEquals(TimeUnit.SECONDS.toMillis(5), gatewayReceiver.getMaximumTimeBetweenPings());
      assertEquals(32768, gatewayReceiver.getSocketBufferSize());
      assertTrue(gatewayReceiver.isRunning());
    }
    catch (Exception unexpected) {
      System.err.printf("%1$s: %2$s", unexpected.getClass().getName(), unexpected.getMessage());
      unexpected.printStackTrace(System.err);
      throw new RuntimeException(unexpected);
    }
    finally {
      stopGatewayReceiver(gatewayReceiver);
    }
  }

  @Test
  public void testIsNotRunning() {
    GatewayReceiver gatewayReceiver = null;

    try {
      cache = createCache("testIsNotRunning");

      assertNotNull("The GemFire 'Cache' instance was not properly configured and initialized!", cache);

      // The "bug" (hang) is to set the bind address to an invalid IP address not matching any NIC on the host System.
      gatewayReceiver = createGatewayReceiver(cache, null, "neo", 4096, 4096, TimeUnit.SECONDS.toMillis(10), 16384);

      assertNotNull("The 'GatewayReceiver' was not properly configure and initialized!", gatewayReceiver);
      assertNull(gatewayReceiver.getBindAddress());
      assertEquals("neo", gatewayReceiver.getHost());
      assertEquals(4096, gatewayReceiver.getStartPort());
      assertEquals(4096, gatewayReceiver.getEndPort());
      assertEquals(TimeUnit.SECONDS.toMillis(10), gatewayReceiver.getMaximumTimeBetweenPings());
      assertEquals(16384, gatewayReceiver.getSocketBufferSize());
      assertFalse(gatewayReceiver.isRunning());
    }
    catch (Exception unexpected) {
      //System.err.printf("%1$s: %2$s", unexpected.getClass().getName(), unexpected.getMessage());
      //unexpected.printStackTrace(System.err);
      throw new RuntimeException(unexpected);
    }
  }

  // TEST for Trac bug #51368
  @Test
  public void testProperClose() {
    Cache cache = null;
    GatewayReceiver gatewayReceiver = null;

    try {
      cache = createCache("testIsNotRunning");

      assertNotNull("The GemFire 'Cache' instance was not properly configured and initialized!", cache);

      // The "bug" (hang) is to set the bind address to an invalid IP address not matching any NIC on the host System.
      gatewayReceiver = createGatewayReceiver(cache, null, "TheOne", 2048, 2048, TimeUnit.SECONDS.toMillis(15), 8192);

      assertNotNull("The 'GatewayReceiver' was not properly configure and initialized!", gatewayReceiver);
      assertNull(gatewayReceiver.getBindAddress());
      assertEquals("TheOne", gatewayReceiver.getHost());
      assertEquals(2048, gatewayReceiver.getStartPort());
      assertEquals(2048, gatewayReceiver.getEndPort());
      assertEquals(TimeUnit.SECONDS.toMillis(15), gatewayReceiver.getMaximumTimeBetweenPings());
      assertEquals(8192, gatewayReceiver.getSocketBufferSize());

      stopGatewayReceiver(gatewayReceiver);

      // Perhaps...
      //fail("Stopping an unstarted GatewayReceiver should have thrown an IllegalStateException!");
    }
    catch (IllegalStateException expected) {
      assertEquals("Something like... 'start()' must be called before 'stop()'!", expected.getMessage());
    }
    catch (Exception unexpected) {
      System.err.printf("%1$s: %2$s", unexpected.getClass().getName(), unexpected.getMessage());
      unexpected.printStackTrace(System.err);
      throw new RuntimeException(unexpected);
    }
  }

}
