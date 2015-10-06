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

package org.lab.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The SystemPropertiesCloneTest class is a test suite of test cases testing the contract and functionality
 * of java.util.Properties.clone() method on the Properties object returned from java.lang.System.getProperties().
 *
 * @author John Blum
 * @see java.lang.System#getProperties()
 * @see java.util.Properties#clone()
 * @see org.junit.Test
 * @since 1.0.0
 */
public class SystemPropertiesCloneTest {

  private int expectedSystemPropertiesSize;

  private Properties originalSystemProperties = System.getProperties();

  @Before
  public void setup() {
    originalSystemProperties.setProperty("TEST_PROPERTY", "TEST_VALUE");
    expectedSystemPropertiesSize = originalSystemProperties.size();
  }

  @After()
  public void tearDown() {
    assertThat(originalSystemProperties.size(), is(equalTo(expectedSystemPropertiesSize)));
    assertThat(originalSystemProperties.getProperty("TEST_PROPERTY"), is(equalTo("TEST_VALUE")));

    originalSystemProperties.remove("TEST_PROPERTY");

    assertThat(originalSystemProperties.containsKey("TEST_PROPERTY"), is(false));
  }

  @Test
  public void systemPropertiesCloneAndThenClear() {
    Properties clonedSystemProperties = (Properties) originalSystemProperties.clone();

    assertThat(clonedSystemProperties, is(notNullValue()));
    assertThat(clonedSystemProperties.isEmpty(), is(false));
    assertThat(clonedSystemProperties.size(), is(equalTo(expectedSystemPropertiesSize)));
    assertThat(clonedSystemProperties.getProperty("TEST_PROPERTY"), is(equalTo("TEST_VALUE")));

    clonedSystemProperties.clear();

    assertThat(clonedSystemProperties.containsKey("TEST_PROPERTY"), is(false));
    assertThat(clonedSystemProperties.isEmpty(), is(true));
  }

}
