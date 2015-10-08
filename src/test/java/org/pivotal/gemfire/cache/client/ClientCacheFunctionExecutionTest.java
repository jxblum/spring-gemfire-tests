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

package org.pivotal.gemfire.cache.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.ServerLauncher;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;

import org.codeprimate.io.FileSystemUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spring.data.gemfire.AbstractGemFireIntegrationTest;

/**
 * The ClientCacheFunctionExecutionTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ClientCacheFunctionExecutionTest extends AbstractGemFireIntegrationTest {

  private static ServerLauncher serverLauncher;

  @BeforeClass
  public static void startGemFireServer() throws IOException {
    String cacheXmlPathname = "server-function-creation-cache.xml";

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty(DistributionConfig.NAME_NAME, ClientCacheFunctionExecutionTest.class.getSimpleName());
    gemfireProperties.setProperty(DistributionConfig.ENABLE_CLUSTER_CONFIGURATION_NAME, "false");
    gemfireProperties.setProperty(DistributionConfig.HTTP_SERVICE_PORT_NAME, "0");
    gemfireProperties.setProperty(DistributionConfig.JMX_MANAGER_NAME, "true");
    gemfireProperties.setProperty(DistributionConfig.JMX_MANAGER_PORT_NAME, "1199");
    gemfireProperties.setProperty(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    gemfireProperties.setProperty(DistributionConfig.LOCATORS_NAME, "localhost[11235]");
    gemfireProperties.setProperty(DistributionConfig.LOG_LEVEL_NAME, "config");
    gemfireProperties.setProperty(DistributionConfig.MCAST_PORT_NAME, "0");
    gemfireProperties.setProperty(DistributionConfig.START_LOCATOR_NAME, "localhost[11235]");

    serverLauncher = startGemFireServer(cacheXmlPathname, gemfireProperties);
  }

  @AfterClass
  public static void shutdownGemFireServer() {
    stopGemFireServer(serverLauncher);
    deleteServerWorkingDirectory();
    serverLauncher = null;
  }

  private static void deleteServerWorkingDirectory() {
    if (serverLauncher != null) {
      FileSystemUtils.deleteRecursive(FileSystemUtils.createFile(serverLauncher.getWorkingDirectory()));
    }
  }

  private ClientCache clientCache;

  private Region<String, Object> collections;
  private Region<String, Integer> numbers;

  @Before
  public void setup() {
    clientCache = new ClientCacheFactory()
      .set(DistributionConfig.CACHE_XML_FILE_NAME, "client-function-execution-cache.xml")
      .create();

    numbers = clientCache.getRegion("/Numbers");

    assertRegion(numbers, "Numbers");

    numbers.put("one", 1);
    numbers.put("two", 2);
    numbers.put("three", 3);
    numbers.put("four", 4);
    numbers.put("five", 5);

    collections = clientCache.getRegion("/Collections");

    assertRegion(collections, "Collections");

    collections.put("one", new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
    collections.put("two", new ArrayList<>(Arrays.asList("assert", "mock", "test")));
  }

  @After
  public void tearDown() {
    clientCache.close();
    clientCache = null;
  }

  protected void assertResult(final Object expectedAdditionResult, final ResultCollector resultCollector) {
    assertEquals(expectedAdditionResult, extractResult(resultCollector));
  }

  @SuppressWarnings("unchecked")
  protected <T> T extractResult(final ResultCollector resultCollector) {
    assertNotNull(resultCollector);
    assertTrue(resultCollector.getResult() instanceof List);

    List<?> result = (List<?>) resultCollector.getResult();

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    return (T) result.get(0);
  }

  @SuppressWarnings("unchecked")
  protected Object[] extractAllResults(final ResultCollector resultCollector) {
    assertNotNull(resultCollector);
    assertTrue(resultCollector.getResult() instanceof List);

    List<?> results = (List<?>) resultCollector.getResult();

    assertFalse(results.isEmpty());

    List<Object> allResults = new ArrayList<>(results.size());

    for (Object result : results) {
      assertTrue(result instanceof List);
      allResults.addAll((List) result);
    }

    return allResults.toArray();
  }

  @Test
  public void testClientServerFunctionExecution() {
    ResultCollector resultCollector = FunctionService.onRegion(numbers).withArgs(new Object[] { "one", "two" })
      .execute("addition");

    assertResult(3, resultCollector);

    resultCollector = FunctionService.onRegion(collections).withArgs(new Object[] { "one" })
      .execute("streaming");

    Object[] integers = extractAllResults(resultCollector);

    assertNotNull(integers);
    assertEquals(10, integers.length);
    assertTrue(Arrays.asList(integers).containsAll(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));

    resultCollector = FunctionService.onRegion(numbers).withArgs(new Object[] { "four", "five" })
      .execute("addition");

    assertResult(9, resultCollector);

    resultCollector = FunctionService.onRegion(collections).withArgs(new Object[] { "two" })
      .execute("streaming");

    Object[] strings = extractAllResults(resultCollector);

    assertNotNull(strings);
    assertEquals(3, strings.length);
    assertTrue(Arrays.asList(strings).containsAll(Arrays.asList("assert", "mock", "test")));
  }

  public static abstract class AbstractFunctionDeclarable extends FunctionAdapter implements Declarable {

    @Override
    public String getId() {
      return getClass().getSimpleName();
    }

    @Override
    public void init(final Properties parameters) {
    }
  }

  public static class AdditionFunction extends AbstractFunctionDeclarable {

    @Override
    public String getId() {
      return "addition";
    }

    @Override
    public void execute(final FunctionContext context) {
      if (context instanceof RegionFunctionContext) {
        Region<Object, Integer> numbers = ((RegionFunctionContext) context).getDataSet();
        Object[] args = (Object[]) context.getArguments();
        context.getResultSender().lastResult(numbers.get(args[0]) + numbers.get(args[1]));
      }
      else {
        context.getResultSender().sendException(new RuntimeException(String.format(
          "The '%1$s' function can only be invoked on a GemFire Cache Region!", getId())));
      }
    }
  }

  public static class StreamingFunction extends AbstractFunctionDeclarable {

    protected static final int BATCH_SIZE = 2;

    @Override
    public String getId() {
      return "streaming";
    }

    @Override
    public void execute(final FunctionContext context) {
      if (context instanceof RegionFunctionContext) {
        Region<Object, Iterable<Object>> collections = ((RegionFunctionContext) context).getDataSet();
        Object[] args = (Object[]) context.getArguments();

        Iterable<Object> collection = collections.get(args[0]);

        List<Object> batch = new ArrayList<>(BATCH_SIZE);

        for (Object item : collection) {
          batch.add(item);

          if (batch.size() == BATCH_SIZE) {
            context.getResultSender().sendResult(batch);
            batch.clear();
          }
        }

        context.getResultSender().lastResult(batch);
      }
      else {
        context.getResultSender().sendException(new RuntimeException(String.format(
          "The '%1$s' function can only be invoked on a GemFire Cache Region!", getId())));
      }
    }
  }

}
