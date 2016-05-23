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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The ClientCacheUsingDataSourceTest class is a test suite of test cases testing the contract and functionality
 * of the Spring Data GemFire &lt;gfe-data:datasource&gt; element behavior when the Spring Data GemFire configured
 * and bootstrapped GemFire cache client is connected to a non-Spring configured (e.g. Gfsh bootstrapped) GemFire Server
 * where the Spring Data GemFire {@link org.springframework.data.gemfire.support.ListRegionsOnServerFunction} has not
 * been registered and is not available.
 *
 * To run this test class (a GemFire cache client), you first need to run the following Gfsh script...
 *
 * start locator --name=LocatorX --log-level=config
 * start server --name=ServerX --log-level=config
 * list members
 * create region --name=Example --type=PARTITION
 * describe region --name=/Example
 * put --region=Example --key=TestKey --value=TestValue
 * get --region=/Example --key=TestKey
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.AbstractGemFireTest
 * @see org.springframework.data.gemfire.client.GemfireDataSourcePostProcessor
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.7.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class ClientCacheUsingDataSourceTest extends AbstractGemFireTest {

  protected static final Object TEST_KEY = "TestKey";
  protected static final Object TEST_VALUE = "TestValue";

  @Resource(name = "Example")
  private Region<Object, Object> example;

  @Test
  public void clientProxyRegionTestKeyValue() {
    assertRegion(example, "Example", DataPolicy.EMPTY);
    assertThat(example.get(TEST_KEY), is(equalTo(TEST_VALUE)));
  }
}
