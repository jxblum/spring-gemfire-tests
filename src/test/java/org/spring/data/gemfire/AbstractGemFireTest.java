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

package org.spring.data.gemfire;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.springframework.context.ApplicationContext;

/**
 * The AbstractGemFireTest class is an abstract base class encapsulating functionality common to servicing all
 * Spring Data GemFire test cases and test suites (test classes).
 *
 * @author John Blum
 * @see org.junit.Assert
 * @see org.springframework.context.ApplicationContext
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.Region
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractGemFireTest {

  protected static volatile boolean debug = false;

  protected static final boolean SUBREGION_RECURSIVE = false;

  protected static final String REGION_PATH_SEPARATOR = Region.SEPARATOR;

  protected static boolean isDebugging() {
    return debug;
  }

  protected static void debug(String message, Object... arguments) {
    if (isDebugging()) {
      System.err.printf(message, arguments);
    }
  }

  protected static void disableDebugging() {
    debug = false;
  }

  protected static void enableDebugging() {
    debug = true;
  }

  protected static void info(String message, Object... arguments) {
    System.out.printf(message, arguments);
  }

  protected static void assertRegion(Region region, String expectedName) {
    assertRegion(region, expectedName, toRegionPath(expectedName));
  }

  protected static void assertRegion(Region region, String expectedName, String expectedPath) {
    assertThat(String.format("Expected Region with name (%1$s); but was null!", expectedName),
      region, is(notNullValue(Region.class)));

    String actualRegionName = region.getName();
    String actualRegionPath = region.getFullPath();

    assertThat(String.format("Expected a Region named (%1$s); but was (%2$s)!", expectedName, actualRegionName),
      actualRegionName, is(equalTo(expectedName)));

    assertThat(String.format("Expected a Region path of (%1$s); but was (%2$s)!", expectedName, actualRegionPath),
      actualRegionPath, is(equalTo(expectedPath)));

    debug("Region [%1$s] found%n", actualRegionName);
  }

  protected static void assertRegion(Region region, String expectedName, DataPolicy expectedDataPolicy) {
    assertRegion(region, expectedName, toRegionPath(expectedName), expectedDataPolicy);
  }

  protected static void assertRegion(Region region, String expectedName, String expectedPath, DataPolicy expectedDataPolicy) {
    assertRegion(region, expectedName, expectedPath);
    assertThat(region.getAttributes(), is(notNullValue(RegionAttributes.class)));
    assertThat(region.getAttributes().getDataPolicy(), is(equalTo(expectedDataPolicy)));
  }

  protected void printBeanNames(ApplicationContext applicationContext, Class<?> beanType) {
    for (String beanName : applicationContext.getBeanNamesForType(beanType)) {
      System.out.printf("%1$s%n", beanName);
    }
  }

  protected static void printRegionHierarchy(Region<?, ?> region) {
    if (region != null) {
      info("%1$s%n", region.getFullPath());

      for (Region subRegion : region.subregions(SUBREGION_RECURSIVE)) {
        printRegionHierarchy(subRegion);
      }
    }
    else {
      info("Region was null%n");
    }
  }

  protected static String toPathname(Class<?> type) {
    return type.getName().replaceAll("\\.", File.separator);
  }

  protected static String toRegionPath(String regionName) {
    return String.format("%1$s%2$s", REGION_PATH_SEPARATOR, regionName);
  }
}
