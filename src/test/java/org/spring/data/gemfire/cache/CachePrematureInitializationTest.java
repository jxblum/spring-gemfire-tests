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

import java.util.Properties;

import org.apache.geode.cache.Cache;
import org.apache.geode.distributed.DistributedSystem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ObjectUtils;

/**
 * The CachePrematureInitializationTest class is a test suite of test cases testing the initialization of a GemFire
 * Cache setting attributes/properties having default values defined in the Spring Data GemFire XML Schema (XSD).
 * However, in this particular test case, the CacheFactoryBean's getObject method is called erroneously inside a
 * BeanPostProcessor's postProcessBeforeInitialization method causing premature use of the GemFire Cache before the
 * factory can be fully constructed, configured and initialized to properly configure and initialize the GemFire Cache
 * instance!
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see org.apache.geode.cache.Cache
 * @since 1.3.3 (Spring Data GemFire)
 * @since 7.0.1 (GemFire)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class CachePrematureInitializationTest {

  @Autowired
  private Cache cache;

  protected static void printCacheFactoryBeanSettings(final CacheFactoryBean cacheFactoryBean) {
    StringBuilder buffer = new StringBuilder("{copyOnRead = ")
      .append(cacheFactoryBean.getCopyOnRead())
      .append(", lock-lease = ").append(cacheFactoryBean.getLockLease())
      .append(", lock-timeout = ").append(cacheFactoryBean.getLockTimeout())
      .append(", message-sync-interval = ").append(cacheFactoryBean.getMessageSyncInterval())
      .append(", search-timeout = ").append(cacheFactoryBean.getSearchTimeout())
      .append("}");

    System.out.printf("Initialized CacheFactoryBean Settings: %1$s%n", buffer);
  }

  protected static void printCacheSettings(final Cache cache) {
    StringBuilder buffer = new StringBuilder("{copyOnRead = ")
      .append(cache.getCopyOnRead())
      .append(", lock-lease = ").append(cache.getLockLease())
      .append(", lock-timeout = ").append(cache.getLockTimeout())
      .append(", message-sync-interval = ").append(cache.getMessageSyncInterval())
      .append(", search-timeout = ").append(cache.getSearchTimeout())
      .append("}");

    System.out.printf("Initialized Cache Settings: %1$s%n", buffer);
  }

  @Test
  public void testCacheInitialization() {
    assertNotNull(cache);

    printCacheSettings(cache);

    assertTrue(cache.getCopyOnRead());
    assertEquals(300, cache.getLockLease());
    assertEquals(120, cache.getLockTimeout());
    assertEquals(10, cache.getMessageSyncInterval());
    assertEquals(600, cache.getSearchTimeout());

    DistributedSystem distributedSystem = cache.getDistributedSystem();

    assertNotNull(distributedSystem);

    Properties distributionConfigProperties = distributedSystem.getProperties();

    assertEquals("cachePrematureInitializationTest", distributionConfigProperties.getProperty("name"));
    assertEquals("localhost[11235]", distributionConfigProperties.getProperty("locators"));
    assertEquals("warning", distributionConfigProperties.getProperty("log-level"));
    assertEquals("0", distributionConfigProperties.getProperty("mcast-port"));
    assertEquals("localhost[11235]", distributionConfigProperties.getProperty("start-locator"));
  }

  public static final class CacheReferencingBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
      System.out.printf("Bean (%1$s) of Type (%2$s) with Name (%3$s)%n", bean, ObjectUtils.nullSafeClassName(bean),
        beanName);

      //if (bean instanceof CacheFactoryBean && "gemfireCache".equals(beanName)) {
      if (bean instanceof CacheFactoryBean) {
        printCacheFactoryBeanSettings((CacheFactoryBean) bean);

        try {
          // NOTE incorrectly use the CacheFactoryBean instance to get a reference to the GemFire Cache
          // before the CacheFactoryBean has been fully/properly initialized by the Spring container!
          Cache cache = ((CacheFactoryBean) bean).getObject();

          assertTrue("The Cache's 'copyOnRead' property was false!", cache.getCopyOnRead());
          // then, access properties/perform operations on the cache reference...
        }
        catch (Exception ignore) {
          ignore.printStackTrace(System.err);
        }
      }

      return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
      // NOTE, logic in postProcessBeforeInitialization causing the test to fail will not work here since the properties
      // on the CacheFactoryBean would already be set!
      return bean;
    }
  }

}
