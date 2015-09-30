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
import static org.junit.Assume.*;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.config.GemfireConstants;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The SubRegionTest class is a test suite of test cases testing GemFire's Cache Sub-Region functionality
 * in Spring Data GemFire for JIRA issue SGF-194.
 *
 * @author John Blum
 * @see org.junit.Assert
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.AbstractGemFireTest
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @link https://jira.spring.io/browse/SGF-194
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class SubRegionTest extends AbstractGemFireTest {

  @Autowired
  private ApplicationContext applicationContext;

  @Resource
  private Cache gemfireCache;

  //@Autowired
  private GemfireTemplate customersTemplate;

  //@Resource(name = "Customers")
  //@Resource(name = "customersRegion")
  private Region customersRegion;

  // NOTE injecting the Region beans using auto-wiring (with the @Resource annotation) causes afterPropertiesSet to be called
  // on the corresponding FactoryBeans and the Region to be created in GemFire appropriately
  //@Resource(name = "/Customers/Accounts")
  private Region accountsRegion;

  //@Resource(name = "/Customers/Accounts/Orders")
  private Region ordersRegion;

  //@Resource(name = "/Customers/Accounts/Orders/Items")
  private Region itemsRegion;

  @Before
  public void setup() {
    gemfireCache = initializeGemFireCache();
    //customersRegion = initializeCustomersRegion();
  }

  @After
  public void tearDown() {
    System.out.printf("Spring Container Region Bean Names:%n");
    printBeanNames(applicationContext, Region.class);
  }

  private Cache initializeGemFireCache() {
    if (gemfireCache == null && applicationContext != null) {
      gemfireCache = applicationContext.getBean(GemfireConstants.DEFAULT_GEMFIRE_CACHE_NAME, Cache.class);
    }

    assertNotNull("The 'gemfireCache' Cache bean was null!", gemfireCache);

    return gemfireCache;
  }

  private Region initializeCustomersRegion() {
    if (customersRegion == null && customersTemplate != null) {
      customersRegion = customersTemplate.getRegion();
    }
    if (customersRegion == null && gemfireCache != null) {
      customersRegion = gemfireCache.getRegion("Customers");
    }
    if (customersRegion == null && applicationContext != null) {
      customersRegion = applicationContext.getBean("Customers", Region.class);
    }

    assertTrue("The 'Customers' Region was null!", customersRegion != null);

    return customersRegion;
  }

  @Test
  //@Ignore
  public void testSpringApplicationContextContainsRegionBeans() {
    assumeNotNull(applicationContext);
    assertTrue(applicationContext.containsBean("Customers"));
    assertFalse(applicationContext.containsBean("/Customers"));
    assertTrue(applicationContext.containsBean("/Customers/Accounts"));
    assertTrue(applicationContext.containsBean("/Customers/Accounts/Orders"));
    assertTrue(applicationContext.containsBean("/Customers/Accounts/Orders/Items"));

    System.out.printf("testSpringApplicationContextContainsRegionBeans Region Hierarchy:%n");
    printRegionHierarchy(applicationContext.getBean("Customers", Region.class));
  }

  @Test
  //@Ignore
  public void testGemFireContainsRegions() {
    assumeNotNull(gemfireCache);
    assertNotNull(gemfireCache.getRegion("Customers"));
    assertNotNull(gemfireCache.getRegion("/Customers"));
    assertNotNull(gemfireCache.getRegion("/Customers/Accounts"));
    assertNotNull(gemfireCache.getRegion("/Customers/Accounts/Orders"));
    assertNotNull(gemfireCache.getRegion("/Customers/Accounts/Orders/Items"));

    System.out.printf("testGemFireContainsRegions Region Hierarchy:%n");
    printRegionHierarchy(gemfireCache.getRegion("Customers"));
  }

  @Test
  //@Ignore
  public void testSpringApplicationContextAndGemFireContainRegions() throws Exception {
    assumeNotNull(applicationContext);
    assumeNotNull(gemfireCache);

    assertTrue(applicationContext.containsBean("Customers"));
    assertTrue(applicationContext.containsBean("/Customers/Accounts"));
    assertTrue(applicationContext.containsBean("/Customers/Accounts/Orders"));
    assertTrue(applicationContext.containsBean("/Customers/Accounts/Orders/Items"));

    // NOTE The cause of JIRA issue SGF-194 is that the SubRegionFactoryBean.afterPropertiesSet() method is not being called
    // during the initialization phase, after the instantiation and (then) configuration phases of bean creation by the
    // Spring IoC container.
    /*
    SubRegionFactoryBean springAccountsRegionFactoryBean = applicationContext.getBean("&/Customers/Accounts", SubRegionFactoryBean.class);

    assertNotNull(springAccountsRegionFatoryBean);

    // NOTE the following causes the /Customers/Accounts Region to be created in GemFire's Cache!
    springAccountsRegionFactoryBean.afterPropertiesSet();
    */

    //Region gemfireCustomersRegion = gemfireCache.getRegion("Customers"); // this works too!
    Region gemfireCustomersRegion = gemfireCache.getRegion("/Customers");
    Region gemfireAccountsRegion = gemfireCache.getRegion("/Customers/Accounts");
    Region gemfireOrdersRegion = gemfireCache.getRegion("/Customers/Accounts/Orders");
    Region gemfireItemsRegion = gemfireCache.getRegion("/Customers/Accounts/Orders/Items");

    assertRegionExists("Customers", "/Customers", gemfireCustomersRegion);
    assertRegionExists("Accounts", "/Customers/Accounts", gemfireAccountsRegion);
    assertRegionExists("Orders", "/Customers/Accounts/Orders", gemfireOrdersRegion);
    assertRegionExists("Items", "/Customers/Accounts/Orders/Items", gemfireItemsRegion);

    // The following triggers the null Regions (Accounts, Orders, Items) to be created in GemFire's Cache!
    Region springCustomersRegion = applicationContext.getBean("Customers", Region.class);
    Region springAccountsRegion = applicationContext.getBean("/Customers/Accounts", Region.class);
    Region springOrdersRegion = applicationContext.getBean("/Customers/Accounts/Orders", Region.class);
    Region springItemsRegion = applicationContext.getBean("/Customers/Accounts/Orders/Items", Region.class);

    assertRegionExists("Customers", "/Customers", springCustomersRegion);
    assertRegionExists("Accounts", "/Customers/Accounts", springAccountsRegion);
    assertRegionExists("Orders", "/Customers/Accounts/Orders", springOrdersRegion);
    assertRegionExists("Items", "/Customers/Accounts/Orders/Items", springItemsRegion);

    gemfireCustomersRegion = gemfireCache.getRegion("/Customers");
    gemfireAccountsRegion = gemfireCache.getRegion("/Customers/Accounts");
    gemfireOrdersRegion = gemfireCache.getRegion("/Customers/Accounts/Orders");
    gemfireItemsRegion = gemfireCache.getRegion("/Customers/Accounts/Orders/Items");

    assertRegionExists("Customers", "/Customers", gemfireCustomersRegion);
    assertSame(springCustomersRegion, gemfireCustomersRegion);
    assertRegionExists("Accounts", "/Customers/Accounts", gemfireAccountsRegion);
    assertSame(springAccountsRegion, gemfireAccountsRegion);
    assertRegionExists("Orders", "/Customers/Accounts/Orders", gemfireOrdersRegion);
    assertSame(springOrdersRegion, gemfireOrdersRegion);
    assertRegionExists("Items", "/Customers/Accounts/Orders/Items", gemfireItemsRegion);
    assertSame(springItemsRegion, gemfireItemsRegion);

    assertSame(gemfireAccountsRegion, gemfireCustomersRegion.getSubregion("Accounts"));
    assertSame(gemfireOrdersRegion, gemfireCustomersRegion.getSubregion("Accounts/Orders"));
    assertSame(gemfireItemsRegion, gemfireCustomersRegion.getSubregion("Accounts/Orders/Items"));
    assertSame(gemfireOrdersRegion, gemfireAccountsRegion.getSubregion("Orders"));
    assertSame(gemfireItemsRegion, gemfireAccountsRegion.getSubregion("Orders/Items"));
    assertSame(gemfireItemsRegion, gemfireOrdersRegion.getSubregion("Items"));
  }

  @Test
  //@Ignore
  public void testSubRegionCreation() throws Exception {
    assertNotNull("The GemFire Cache cannot be null!", gemfireCache);

    customersRegion = (customersRegion != null ? customersRegion : (gemfireCache.getRegion("Customers")));

    System.out.printf("testSubRegionCreation Region Hierarchy:%n");
    printRegionHierarchy(customersRegion);

    assertRegionExists("Customers", "/Customers", customersRegion);

    final Region accountsRegion = customersRegion.getSubregion("Accounts");

    assertRegionExists("Accounts", "/Customers/Accounts", accountsRegion);

    final Region ordersRegion = accountsRegion.getSubregion("Orders");

    assertRegionExists("Orders", "/Customers/Accounts/Orders", ordersRegion);

    final Region itemsRegion = ordersRegion.getSubregion("Items");

    assertRegionExists("Items", "/Customers/Accounts/Orders/Items", itemsRegion);
  }

}
