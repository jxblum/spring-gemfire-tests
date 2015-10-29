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
import static org.junit.Assume.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireIntegrationTest;
import org.spring.data.gemfire.cache.execute.OnMemberEchoFunctionExecution;
import org.spring.data.gemfire.cache.execute.OnRegionFunctionExecutions;
import org.spring.data.gemfire.cache.execute.OnServerEchoFunctionExecution;
import org.spring.data.gemfire.cache.execute.OnServerThrowingExceptionFunctionExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.function.execution.GemfireOnRegionFunctionTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ObjectUtils;

/**
 * The ClientFunctionExecutionTest class is a test suite of test cases testing the execution of Functions using a
 * GemFire client cache.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.AbstractGemFireIntegrationTest
 * @see org.spring.data.gemfire.cache.execute.OnMemberEchoFunctionExecution
 * @see org.spring.data.gemfire.cache.execute.OnRegionFunctionExecutions
 * @see org.spring.data.gemfire.cache.execute.OnServerEchoFunctionExecution
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.4.0.BUILD-SNAPSHOT
 * @since 7.0.1 (GemFire)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class ClientCacheFunctionExecutionTest extends AbstractGemFireIntegrationTest {

  protected static final int EXPECTED_REGION_SIZE = 121;

  @Autowired
  private ClientCache gemfireCache;

  @Autowired(required = false)
  private OnMemberEchoFunctionExecution onMemberEchoFunction;

  @Autowired
  private OnRegionFunctionExecutions onRegionFunctions;

  @Autowired
  private OnServerEchoFunctionExecution onServerEchoFunction;

  @Autowired
  private OnServerThrowingExceptionFunctionExecution onServerThrowingFunction;

  @Resource(name = "AppData")
  private Region<String, Integer> appDataProxy;

  @BeforeClass
  public static void setupGemFireServer() throws IOException {
    startSpringGemFireServer(TimeUnit.SECONDS.toMillis(60), "/peerCacheFunctionCreationExecution.xml");
  }

  @Before
  public void setup() {
    if (appDataProxy.isEmpty()) {
      for (int index = 0; index < EXPECTED_REGION_SIZE; index++) {
        appDataProxy.put(String.valueOf(index), index);
      }
    }
  }

  @Test
  public void testEchoOnMemberUsingGemFire() {
    assumeNotNull(onMemberEchoFunction);

    Execution memberFunctionExecution = FunctionService.onMember("testGroup").withArgs("TEST");

    ResultCollector<?, ?> results = memberFunctionExecution.execute("echo");

    assertNotNull(results);

    Object result = results.getResult();

    assertTrue(String.format("Expected java.util.List; but was (%1$s)!", ObjectUtils.nullSafeClassName(result)),
      result instanceof List);
    assertFalse(((List) result).isEmpty());
    assertEquals("You said 'TEST'!", ((List) result).get(0));
  }

  @Test
  public void testEchoOnMemberUsingSpring() {
    assumeNotNull(onMemberEchoFunction);
    assertEquals("You said 'HELLO'!", onMemberEchoFunction.echo("HELLO"));
  }

  @Test
  public void testEchoOnServerUsingGemFire() {
    assumeNotNull(onServerEchoFunction);

    Execution memberFunctionExecution = FunctionService.onServer(gemfireCache).withArgs("GOODBYE");

    ResultCollector<?, ?> results = memberFunctionExecution.execute("echo");

    assertNotNull(results);

    Object result = results.getResult();

    assertTrue(String.format("Expected java.util.List; but was (%1$s)!", ObjectUtils.nullSafeClassName(result)),
      result instanceof List);
    assertFalse(((List) result).isEmpty());
    assertEquals("You said 'GOODBYE'!", ((List) result).get(0));
  }

  @Test
  public void testEchoOnServerUsingSpring() {
    assumeNotNull(onServerEchoFunction);
    assertEquals("You said 'WHAT'!", onServerEchoFunction.echo("WHAT"));
  }

  @Test
  public void testRegionSizeOnRegionUsingGemFire() {
    Execution regionFunctionExecution = FunctionService.onRegion(appDataProxy).withArgs(new Object[] { "AppData" });

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
    Object result = onRegionFunctions.regionSize(appDataProxy.getName());

    assertTrue(String.format("Expected java.util.List; but was (%1$s)!", ObjectUtils.nullSafeClassName(result)),
      result instanceof List);
    assertFalse(((List) result).isEmpty());
    assertEquals(EXPECTED_REGION_SIZE, ((List) result).get(0));
  }

  @Test
  public void testRegionSizeOnRegionUsingSpringGemfireOnRegionFunctionTemplate() {
    GemfireOnRegionFunctionTemplate template = new GemfireOnRegionFunctionTemplate(appDataProxy);

    assertThat(template.executeAndextract("regionSize", Collections.emptySet(), "AppData"),
      is(equalTo(EXPECTED_REGION_SIZE)));
  }

  @Test(expected = RuntimeException.class)
  public void testThrowingFunctionUsingSpring() {
    String expectedMessage = "TEST";

    try {
      onServerThrowingFunction.throwsExceptionFunction(expectedMessage);
    }
    catch (FunctionException expected) {
      //expected.printStackTrace(System.err);
      assertTrue(expected.getCause() instanceof RuntimeException);
      assertEquals(expectedMessage, expected.getCause().getMessage());
      throw (RuntimeException) expected.getCause();
    }
  }

}
