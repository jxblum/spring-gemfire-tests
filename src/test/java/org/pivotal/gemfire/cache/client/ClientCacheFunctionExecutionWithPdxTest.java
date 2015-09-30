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
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.ServerLauncher;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.pdx.PdxInstance;
import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxSerializer;
import com.gemstone.gemfire.pdx.PdxWriter;
import com.gemstone.gemfire.pdx.ReflectionBasedAutoSerializer;

import org.codeprimate.io.FileSystemUtils;
import org.codeprimate.lang.ClassUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.spring.data.gemfire.AbstractGemFireIntegrationTest;

/**
 * The ClientCacheFunctionExecutionWithPdxTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ClientCacheFunctionExecutionWithPdxTest extends AbstractGemFireIntegrationTest {

  private static ServerLauncher serverLauncher;

  private ClientCache clientCache;

  @BeforeClass
  public static void startGemFireServer() throws IOException {
    String cacheXmlPathname = "server-function-creation-with-pdx-cache.xml";

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty(DistributionConfig.NAME_NAME, ClientCacheFunctionExecutionTest.class.getSimpleName());
    gemfireProperties.setProperty(DistributionConfig.ENABLE_CLUSTER_CONFIGURATION_NAME, "false");
    gemfireProperties.setProperty(DistributionConfig.HTTP_SERVICE_PORT_NAME, "0");
    gemfireProperties.setProperty(DistributionConfig.JMX_MANAGER_NAME, "false");
    gemfireProperties.setProperty(DistributionConfig.JMX_MANAGER_PORT_NAME, "1199");
    gemfireProperties.setProperty(DistributionConfig.JMX_MANAGER_START_NAME, "false");
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

  /*
  <pdx>
    <pdx-serializer>
      <class-name>
        com.gemstone.gemfire.pdx.ReflectionBasedAutoSerializer
      </class-name>
      <parameter name="classes">
        <string>org\.pivotal\.gemfire\.cache\.client\.ClientCacheFunctionExecutionWithPdxTest\$Test.+</string>
        <string>org\.pivotal\.gemfire\.cache\.client\.ClientCacheFunctionExecutionWithPdxTest\$TestDomainClass</string>
      </parameter>
    </pdx-serializer>
  </pdx>
   */
  @Before
  public void setupClientCache() {
    clientCache = new ClientCacheFactory()
      .set(DistributionConfig.CACHE_XML_FILE_NAME, "client-function-execution-with-pdx-cache.xml")
      .create();
  }

  @After
  public void tearDownClientCache() {
    if (clientCache != null) {
      clientCache.close();
    }

    clientCache = null;
  }

  @SuppressWarnings("unchecked")
  protected Class<?>[] extractResults(final ResultCollector resultCollector) {
    assertNotNull(resultCollector);
    assertTrue(resultCollector.getResult() instanceof List);

    List<?> results = (List<?>) resultCollector.getResult();

    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertTrue(results.get(0) instanceof List);

    List<Class<?>> argumentTypes = (List<Class<?>>) results.get(0);

    return argumentTypes.toArray(new Class<?>[argumentTypes.size()]);
  }

  @Test
  //@Ignore
  public void testFunctionArgumentTypes() {
    Object[] arguments = new Object[] { "string", 2, Math.PI, new TestDomainClass("DomainType"), TestEnum.TWO };

    ResultCollector resultCollector = FunctionService.onServer(clientCache).withArgs(arguments)
      .execute(ArgumentTypeCaptureFunction.class.getSimpleName());

    Class<?>[] argumentTypes = extractResults(resultCollector);

    assertNotNull(argumentTypes);
    assertEquals(arguments.length, argumentTypes.length);
    assertEquals(arguments[0].getClass(), argumentTypes[0]);
    assertEquals(arguments[1].getClass(), argumentTypes[1]);
    assertEquals(arguments[2].getClass(), argumentTypes[2]);
    // NOTE toggle the commented assertions depending on the value of PDX 'read-serialized' attribute on the GemFire Server
    assertTrue(PdxInstance.class.isAssignableFrom(argumentTypes[3])); // comment-out when PDX read-serialized is set to false
    //assertEquals(arguments[3].getClass(), argumentTypes[3]); // comment-out when PDX read-serialized is set to true
    //assertTrue(PdxInstanceEnum.class.isAssignableFrom(argumentTypes[4])); // comment-out when PDX read-serialized is set to false
    assertEquals(arguments[4].getClass(), argumentTypes[4]); // comment-out when PDX read-serialized is set to true
  }

  @Test
  @Ignore
  public void testReflectionBasedAutoSerializer() {
    //ReflectionBasedAutoSerializer serializer = new ReflectionBasedAutoSerializer("org.pivotal.gemfire.cache.client.ClientCacheFunctionExecutionWithPdxTest.Test.+");
    ReflectionBasedAutoSerializer serializer = new ReflectionBasedAutoSerializer("org\\.pivotal\\.gemfire\\.cache\\.client\\.ClientCacheFunctionExecutionWithPdxTest\\$Test.+");

    assertTrue(serializer.isClassAutoSerialized(TestDomainClass.class));
    assertFalse(serializer.isClassAutoSerialized(TestEnum.class)); // False, eh?!
  }

  protected static abstract class AbstractDeclarableFunction extends FunctionAdapter implements Declarable {

    @Override
    public String getId() {
      return getClass().getSimpleName();
    }

    @Override
    public void init(final Properties parameters) {
    }
  }

  protected static abstract class AbstractDeclarablePdxSerializer implements PdxSerializer, Declarable {

    @Override
    public void init(final Properties props) {
    }
  }

  public static class ArgumentTypeCaptureFunction extends AbstractDeclarableFunction {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(final FunctionContext context) {
      Object arguments = context.getArguments();

      List<Class<?>> argumentTypes;

      if (arguments instanceof Object[]) {
        argumentTypes = new ArrayList<>(((Object[]) arguments).length);

        for (Object argument : (Object[]) arguments) {
          argumentTypes.add(ClassUtils.getClass(argument));
        }
      }
      else {
        argumentTypes = Collections.<Class<?>>singletonList(ClassUtils.getClass(arguments));
      }

      context.getResultSender().lastResult(argumentTypes);
    }
  }

  public static class TestDomainClass {

    private final String name;

    public TestDomainClass() {
      this(TestDomainClass.class.getSimpleName());
    }

    public TestDomainClass(final String name) {
      assert !(name == null || name.trim().isEmpty()) : "The 'name' of the TestDomainClass instance must be specified!";
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return String.format("%1$s-%2$s", getClass().getName(), getName());
    }
  }

  public static class TestDomainClassPdxSerializer extends AbstractDeclarablePdxSerializer {

    @Override
    public Object fromData(final Class<?> type, final PdxReader in) {
      assert TestDomainClass.class.isAssignableFrom(type) : String.format(
        "The Object types de/serialized by this PdxSerializer (%1$s) must be an instance of (%2$s); but was (%3$s)!",
          getClass().getName(), TestDomainClass.class.getName(), type.getName());

      return new TestDomainClass(in.readString("name"));
    }

    @Override
    public boolean toData(final Object obj, final PdxWriter out) {
      if (obj instanceof TestDomainClass) {
        out.writeString("name", ((TestDomainClass) obj).getName());
        return true;
      }

      return false;
    }
  }

  public enum TestEnum {
    ONE,
    TWO,
    THREE
  }

}
