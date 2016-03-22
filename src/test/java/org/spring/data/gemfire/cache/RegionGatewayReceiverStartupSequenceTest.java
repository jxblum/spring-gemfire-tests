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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.wan.GatewayReceiver;
import com.gemstone.gemfire.cache.wan.GatewayReceiverFactory;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.Role;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.RegionFactoryBean;
import org.springframework.data.gemfire.wan.GatewayReceiverFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The RegionGatewayReceiverStartupSequenceTest class is a test suite of test cases testing that a GemFire
 * GatewayReceiver that is not set to manual start, will start after all GemFire Regions in the Spring context
 * have been instantiated and initialized.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.mockito.Mockito
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.data.gemfire.RegionFactoryBean
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.cache.wan.GatewayReceiver
 * @see com.gemstone.gemfire.distributed.DistributedMember
 * @see com.gemstone.gemfire.distributed.DistributedSystem
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class RegionGatewayReceiverStartupSequenceTest {

  private static final AtomicBoolean GATEWAY_RECEIVER_START_CALLED = new AtomicBoolean(false);

  private static final Map<String, Boolean> regionNameStateMap = new ConcurrentHashMap<>();

  protected static void assertRegionsInitialized() {
    assertEquals(4, regionNameStateMap.size());
    for (Boolean value : regionNameStateMap.values()) {
      assertTrue(Boolean.TRUE.equals(value));
    }
  }

  @Test
  public void testGatewayReceiverStartupSequence() {
    assertRegionsInitialized();
    assertTrue(GATEWAY_RECEIVER_START_CALLED.get());
  }

  public static final class GemfireMocksBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
      if (bean instanceof CacheFactoryBean) {
        bean = new MockCacheFactoryBean((CacheFactoryBean) bean);
      }
      else if (bean instanceof RegionFactoryBean) {
        regionNameStateMap.put(beanName, false);
      }
      else if (bean instanceof GatewayReceiverFactoryBean) {
        assertFalse(String.format("Region states (%1$s)", regionNameStateMap), GATEWAY_RECEIVER_START_CALLED.get());
      }

      return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
      if (bean instanceof RegionFactoryBean) {
        regionNameStateMap.put(beanName, true);
      }
      else if (bean instanceof GatewayReceiverFactoryBean) {
        assertFalse(String.format("Region states (%1$s)", regionNameStateMap), GATEWAY_RECEIVER_START_CALLED.get());
      }

      return bean;
    }
  }

  private static final class MockCacheFactoryBean extends CacheFactoryBean {

    private final CacheFactoryBean delegate;

    public MockCacheFactoryBean(final CacheFactoryBean cacheFactoryBean) {
      this.delegate = cacheFactoryBean;
      setCache(mockCache());
    }

    private Cache mockCache() {
      Cache mockCache = mock(Cache.class, "MockGemFireCache");

      DistributedSystem mockDistributedSystem = mockDistributedSystem();

      when(mockCache.getName()).thenReturn("MockGemFireCache");
      when(mockCache.getDistributedSystem()).thenReturn(mockDistributedSystem);

      when(mockCache.createGatewayReceiverFactory()).thenAnswer(new Answer<GatewayReceiverFactory>() {
        @Override public GatewayReceiverFactory answer(final InvocationOnMock invocationOnMock) throws Throwable {
          return mockGatewayReceiverFactory();
        }
      });

      when(mockCache.createRegionFactory()).thenAnswer(new Answer<RegionFactory>() {
        @Override public RegionFactory answer(final InvocationOnMock invocationOnMock) throws Throwable {
          return mockRegionFactory();
        }
      });

      when(mockCache.createRegionFactory(any(RegionAttributes.class))).thenAnswer(new Answer<RegionFactory>() {
        @Override public RegionFactory answer(final InvocationOnMock invocationOnMock) throws Throwable {
          return mockRegionFactory();
        }
      });

      return mockCache;
    }

    private DistributedSystem mockDistributedSystem() {
      DistributedSystem mockDistributedSystem = mock(DistributedSystem.class, "MockGemFireDistributedSystem");
      DistributedMember mockDistributedMember = mock(DistributedMember.class, "MockGemFireDistributedMember");

      when(mockDistributedSystem.getDistributedMember()).thenReturn(mockDistributedMember);
      when(mockDistributedSystem.getName()).thenReturn("MockGemFireDistributedSystem");
      when(mockDistributedMember.getId()).thenReturn("MockGemFireDistributedMember");
      when(mockDistributedMember.getGroups()).thenReturn(Arrays.asList("TestGroup"));
      when(mockDistributedMember.getHost()).thenReturn("localhost");
      when(mockDistributedMember.getProcessId()).thenReturn(123);
      when(mockDistributedMember.getRoles()).thenReturn(Collections.singleton(mock(Role.class)));

      return mockDistributedSystem;
    }

    private GatewayReceiverFactory mockGatewayReceiverFactory() {
      GatewayReceiverFactory mockGatewayReceiverFactory = mock(GatewayReceiverFactory.class,
        "MockGemFireGatewayReceiverFactory");

      GatewayReceiver mockGatewayReceiver = mockGatewayReceiver();

      when(mockGatewayReceiverFactory.create()).thenReturn(mockGatewayReceiver);

      return mockGatewayReceiverFactory;
    }

    private GatewayReceiver mockGatewayReceiver() {
      GatewayReceiver mockGatewayReceiver = mock(GatewayReceiver.class, "MockGemFireGatewayReceiver");

      try {
        doAnswer(new Answer<Void>() {
          @Override public Void answer(final InvocationOnMock invocationOnMock) throws Throwable {
            GATEWAY_RECEIVER_START_CALLED.set(true);
            assertRegionsInitialized();
            return null;
          }
        }).when(mockGatewayReceiver).start();
      }
      catch (IOException e) {
        throw new RuntimeException("failed to start the GemFire GatewayReceiver", e);
      }

      return mockGatewayReceiver;
    }

    @SuppressWarnings("unchecked")
    private RegionFactory<Object, Object> mockRegionFactory() {
      RegionFactory<Object, Object> mockRegionFactory = mock(RegionFactory.class, "MockGemFireRegionFactory");

      doAnswer(new Answer<Region>() {
        @Override public Region answer(final InvocationOnMock invocationOnMock) throws Throwable {
          String regionName = String.valueOf(invocationOnMock.getArguments()[0]);
          return mockRegion(regionName);
        }
      }).when(mockRegionFactory).create(any(String.class));

      return mockRegionFactory;
    }

    private Region mockRegion(final String regionName) {
      Region mockRegion = mock(Region.class, String.format("MockGemFireRegion.%1$s", regionName));

      when(mockRegion.getName()).thenReturn(regionName);
      when(mockRegion.getFullPath()).thenReturn(String.format("%1$s%2$s", Region.SEPARATOR, regionName));

      return mockRegion;
    }
  }

}
