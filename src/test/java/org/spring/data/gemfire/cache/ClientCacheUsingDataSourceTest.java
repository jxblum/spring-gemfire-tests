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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The ClientCacheUsingDataSourceTest class is a test suite of test cases testing the contract and functionality
 * of the Spring Data GemFire &lt;gfe-data:datasource&gt; element behavior when the Spring Data GemFire configured
 * and bootstrapped GemFire ClientCache is connected to a non-Spring configured GemFire Server where
 * the Spring Data GemFire ListRegionsOnServerFunction has not been registered and is not available.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.data.gemfire.client.GemfireDataSourcePostProcessor
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.7.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class ClientCacheUsingDataSourceTest {

  protected static final Object TEST_KEY = "TestKey";
  protected static final Object TEST_VALUE = "TestValue";

  @Resource(name = "Example")
  private Region<Object, Object> example;

  @Test
  public void clientProxyRegionTestKeyValue() {
    assertThat(example, is(not(nullValue())));
    assertThat(example.getName(), is(equalTo("Example")));
    assertThat(example.getFullPath(), is(equalTo(String.format("%1$s%2$s", Region.SEPARATOR, "Example"))));
    assertThat(example.get(TEST_KEY), is(equalTo(TEST_VALUE)));
  }

}
