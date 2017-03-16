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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.annotation.Resource;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireTest;
import org.spring.data.gemfire.cache.execute.OnMemberEchoFunctionExecution;
import org.spring.data.gemfire.cache.execute.OnRegionFunctionExecutions;
import org.spring.data.gemfire.cache.execute.OnServerEchoFunctionExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.function.execution.GemfireOnRegionFunctionTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ObjectUtils;

/**
 * The FunctionCreationExecutionTest class is test suite of test cases testing the creation of a GemFire Function
 * and the subsequent execution that Function in a peer Cache context.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.AbstractGemFireTest
 * @see org.spring.data.gemfire.cache.execute.OnMemberEchoFunctionExecution
 * @see org.spring.data.gemfire.cache.execute.OnRegionFunctionExecutions
 * @see org.springframework.data.gemfire.function.execution.GemfireOnRegionFunctionTemplate
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since GemFire 7.0.1
 * @since Spring Data GemFire 1.4.0.BUILD-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class PeerCacheFunctionCreationExecutionTest extends AbstractGemFireTest {

  protected static final int EXPECTED_REGION_SIZE = 151;

  @Autowired
  private Cache gemfireCache;

  @Autowired
  private OnMemberEchoFunctionExecution onMemberEchoFunction;

  @Autowired
  private OnRegionFunctionExecutions onRegionFunctions;

  @Autowired(required = false)
  private OnServerEchoFunctionExecution onServerEchoFunction;

  @Resource(name = "peerCacheConfigurationSettings")
  private Properties gemfireConfigurationSettings;

  @Resource(name = "AppData")
  private Region<String, Integer> appData;

  @Before
  public void setup() {
    if (appData.isEmpty()) {
      for (int index = 0; index < EXPECTED_REGION_SIZE; index++) {
        appData.put(String.valueOf(index), index);
      }
    }
  }

  @Test
  public void testEchoOnMemberUsingGemFire() {
    Execution memberFunctionExecution = FunctionService.onMember(gemfireConfigurationSettings.getProperty("groups"))
      .withArgs("Hello");

    ResultCollector<?, ?> results = memberFunctionExecution.execute("echo");

    assertNotNull(results);

    Object result = results.getResult();

    assertTrue(String.format("Expected java.util.List; but was (%1$s)!", ObjectUtils.nullSafeClassName(result)),
      result instanceof List);
    assertFalse(((List) result).isEmpty());
    assertEquals("You said 'Hello'!", ((List) result).get(0));
  }

  @Test
  public void testEchoOnMemberUsingSpring() {
    assertEquals("You said 'Goodbye'!", onMemberEchoFunction.echo("Goodbye"));
  }

  @Ignore
  @Test
  public void testEchoOnServerUsingGemFire() {
    Execution memberFunctionExecution = FunctionService.onServer(gemfireCache).withArgs("Hola");

    ResultCollector<?, ?> results = memberFunctionExecution.execute("echo");

    assertNotNull(results);

    Object result = results.getResult();

    assertTrue(String.format("Expected java.util.List; but was (%1$s)!", ObjectUtils.nullSafeClassName(result)),
      result instanceof List);
    assertFalse(((List) result).isEmpty());
    assertEquals("You said 'Hola'!", ((List) result).get(0));
  }

  @Test
  public void testEchoOnServerUsingSpring() {
    assumeNotNull(onServerEchoFunction);
    assertEquals("You said 'Adios'!", onServerEchoFunction.echo("Adios"));
  }

  @Test
  public void testRegionSizeOnRegionUsingGemFire() {
    // TODO file JIRA bug for Spring Data GemFire due to NullPointerException in
    // DefaultFunctionArgumentResolver.resolveFunctionArguments(30) since the default value for the "arguments" Object
    // array in GemFire is erroneously null(!) thus requiring a empty Object array to be passed even when the Function
    // does not require any arguments.
    Execution regionFunctionExecution = FunctionService.onRegion(appData).withArgs(new Object[] { appData.getName() });

    ResultCollector<?, ?> results = regionFunctionExecution.execute("regionSize");

    assertNotNull(results);

    Object result = results.getResult();

    assertTrue(String.format("Expected java.util.List; but was (%1$s)!", ObjectUtils.nullSafeClassName(result)),
      result instanceof List);
    assertFalse(((List) result).isEmpty());
    assertEquals(EXPECTED_REGION_SIZE, ((List) result).get(0));
  }

  @Test
  public void testRegionSizeOnRegionUsingSpring() {
    Object results = onRegionFunctions.regionSize(appData.getName());

    // NOTE reviewing the Spring Data GemFire source code, this is expected but a bit surprising...
    assertTrue(String.format("Expected java.util.List; but was (%1$s)!", ObjectUtils.nullSafeClassName(results)),
      results instanceof List);
    assertFalse(((List) results).isEmpty());
    assertEquals(EXPECTED_REGION_SIZE, ((List) results).get(0));
  }

  @Test
  public void testRegionSizeOnRegionUsingSpringGemfireOnRegionFunctionTemplate() {
    GemfireOnRegionFunctionTemplate template = new GemfireOnRegionFunctionTemplate(appData);

    assertThat(template.executeAndextract("regionSize", Collections.emptySet(), appData.getName()),
      is(equalTo(EXPECTED_REGION_SIZE)));
  }

}
