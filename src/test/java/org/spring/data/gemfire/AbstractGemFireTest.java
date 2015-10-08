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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;

import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;

import org.springframework.context.ApplicationContext;

/**
 * The AbstractGemFireTest class is an abstract base class encapsulating functionality common to servicing all
 * Spring Data GemFire test cases and test suites (test classes).
 *
 * @author John Blum
 * @see org.junit.Assert
 * @see org.springframework.context.ApplicationContext
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.Region
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

  protected static void disableDebugging() {
    debug = false;
  }

  protected static void enableDebugging() {
    debug = true;
  }

  protected static void assertRegion(final Region region, final String expectedRegionName) {
    assertRegion(region, expectedRegionName, toRegionPath(expectedRegionName));
  }

  protected static void assertRegion(final Region region, final String expectedRegionName, final String expectedRegionPath) {
    assertThat(String.format("The Region with the expected name (%1$s) was null!", expectedRegionName), region, is(notNullValue()));

    String actualRegionName = region.getName();
    String actualRegionPath = region.getFullPath();

    assertThat(String.format("Expected a Region named (%1$s); but was (%2$s)!", expectedRegionName, actualRegionName),
      actualRegionName, is(equalTo(expectedRegionName)));

    assertThat(String.format("Expected a Region path of (%1$s); but was (%2$s)!", expectedRegionName, actualRegionPath),
      actualRegionPath, is(equalTo(expectedRegionPath)));

    if (isDebugging()) {
      System.out.printf("Region (%1$s) found!%n", actualRegionName);
    }
  }

  protected static void assertRegion(final Region region, final String expectedRegionName, final DataPolicy expectedDataPolicy) {
    assertRegion(region, expectedRegionName, toRegionPath(expectedRegionName), expectedDataPolicy);
  }

  protected static void assertRegion(final Region region,
                                     final String expectedRegionName,
                                     final String expectedRegionPath,
                                     final DataPolicy expectedDataPolicy)
  {
    assertRegion(region, expectedRegionName, expectedRegionPath);
    assertThat(region.getAttributes(), is(notNullValue()));
    assertThat(region.getAttributes().getDataPolicy(), is(equalTo(expectedDataPolicy)));
  }

  protected void printBeanNames(final ApplicationContext applicationContext, final Class<?> beanType) {
    for (String beanName : applicationContext.getBeanNamesForType(beanType)) {
      System.out.printf("%1$s%n", beanName);
    }
  }

  protected static void printRegionHierarchy(final Region<?, ?> region) {
    if (region != null) {
      System.out.printf("%1$s%n", region.getFullPath());

      for (Region subRegion : region.subregions(SUBREGION_RECURSIVE)) {
        printRegionHierarchy(subRegion);
      }
    }
    else {
      System.out.printf("Region was null!%n");
    }
  }

  protected static String toPathname(final Class<?> type) {
    return type.getName().replaceAll("\\.", File.separator);
  }

  protected static String toRegionPath(final String regionName) {
    return String.format("%1$s%2$s", REGION_PATH_SEPARATOR, regionName);
  }

}
