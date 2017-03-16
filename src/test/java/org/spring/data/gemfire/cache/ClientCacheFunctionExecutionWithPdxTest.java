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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializer;
import org.apache.geode.pdx.PdxWriter;
import org.apache.geode.pdx.internal.PdxInstanceEnum;
import org.codeprimate.lang.ClassUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireIntegrationTest;
import org.spring.data.gemfire.cache.execute.ArgumentTypeCaptureFunctionExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * The ClientCacheFunctionExecutionWithPdxTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.AbstractGemFireIntegrationTest
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class ClientCacheFunctionExecutionWithPdxTest extends AbstractGemFireIntegrationTest {

  @Autowired
  private ArgumentTypeCaptureFunctionExecution functionExecution;

  @BeforeClass
  public static void setupSpringGemFireServer() throws IOException {
    startSpringGemFireServer(toPathname(ClientCacheFunctionExecutionWithPdxTest.class).concat("-server-context.xml"));
  }

  @Test
  public void testFunctionArgumentTypes() {
    Class[] argumentTypes = functionExecution.captureArgumentTypes("test", 1, Math.PI,
      new TestDomainClass("DomainType"), TestEnum.TWO);

    assertNotNull(argumentTypes);
    assertEquals(5, argumentTypes.length);
    assertEquals(String.class, argumentTypes[0]);
    assertEquals(Integer.class, argumentTypes[1]);
    assertEquals(Double.class, argumentTypes[2]);
    //assertEquals(TestDomainClass.class, argumentTypes[3]);
    assertTrue(PdxInstance.class.isAssignableFrom(argumentTypes[3]));
    //assertEquals(TestEnum.class, argumentTypes[4]);
    assertEquals(PdxInstanceEnum.class, argumentTypes[4]);
  }

  public static class ArgumentTypeCaptureFunction {

    @GemfireFunction
    public Class[] captureArgumentTypes(final String stringValue,
                                        final Integer integerValue,
                                        final Double doubleValue,
                                        final Object domainObject,
                                        final Object enumValue)
    {
      List<Class<?>> argumentTypes = new ArrayList<>(5);

      argumentTypes.add(ClassUtils.getClass(stringValue));
      argumentTypes.add(ClassUtils.getClass(integerValue));
      argumentTypes.add(ClassUtils.getClass(doubleValue));
      argumentTypes.add(ClassUtils.getClass(domainObject));
      argumentTypes.add(ClassUtils.getClass(enumValue));

      return argumentTypes.toArray(new Class[argumentTypes.size()]);
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

  public static class TestDomainClassPdxSerializer implements PdxSerializer {

    @Override
    public boolean toData(final Object obj, final PdxWriter out) {
      if (obj instanceof TestDomainClass) {
        out.writeString("name", ((TestDomainClass) obj).getName());
        return true;
      }

      return false;
    }

    @Override
    public Object fromData(final Class<?> type, final PdxReader in) {
      Assert.isAssignable(TestDomainClass.class, type, String.format(
        "The Object types de/serialized by this PdxSerializer (%1$s) must be an instance of (%2$s); but was (%3$s)!",
          getClass().getName(), TestDomainClass.class.getName(), type.getName()));

      return new TestDomainClass(in.readString("name"));
    }
  }

  public enum TestEnum {
    ONE,
    TWO,
    THREE
  }

}
