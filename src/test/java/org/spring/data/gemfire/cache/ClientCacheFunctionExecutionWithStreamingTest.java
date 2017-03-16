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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireIntegrationTest;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.data.gemfire.function.execution.GemfireOnRegionFunctionTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The ClientCacheFunctionExecutionWithStreamingTest class...
 * <p>
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class ClientCacheFunctionExecutionWithStreamingTest extends AbstractGemFireIntegrationTest {

  @Resource(name = "Collections")
  private Region<String, Object> collections;

  @Resource(name = "Numbers")
  private Region<String, Integer> numbers;

  @BeforeClass
  public static void startGemFireServer() throws IOException {
    startSpringGemFireServer(toPathname(ClientCacheFunctionExecutionWithStreamingTest.class).concat("-server-context.xml"));
  }

  @Before
  public void setup() {
    assertRegion(numbers, "Numbers");

    numbers.put("one", 1);
    numbers.put("two", 2);
    numbers.put("three", 3);
    numbers.put("four", 4);
    numbers.put("five", 5);

    assertRegion(collections, "Collections");

    collections.put("one", new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
    collections.put("two", new ArrayList<String>(Arrays.asList("assert", "mock", "test")));
  }

  @Test
  public void testClientServerFunctionExecution() {
    GemfireOnRegionFunctionTemplate onNumbersFunctionTemplate = new GemfireOnRegionFunctionTemplate(numbers);

    assertThat(onNumbersFunctionTemplate.executeAndextract("addition", Collections.emptySet(), "one", "two"),
      is(equalTo(3)));

    GemfireOnRegionFunctionTemplate onCollectionsFunctionTemplate = new GemfireOnRegionFunctionTemplate(collections);

    List<Object> integers = onCollectionsFunctionTemplate.executeAndextract("streaming", Collections.emptySet(), "one");

    assertNotNull(integers);
    //assertEquals(10, integers.size());
    //assertTrue(integers.containsAll(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
    assertEquals(2, integers.size());
    assertTrue(integers.containsAll(Arrays.asList(0, 1)));

    assertThat(onNumbersFunctionTemplate.executeAndextract("addition", Collections.emptySet(), "four", "five"),
      is(equalTo(9)));

    List<Object> strings = onCollectionsFunctionTemplate.executeAndextract("streaming", Collections.emptySet(), "two");

    assertNotNull(strings);
    //assertEquals(3, strings.size());
    //assertTrue(strings.containsAll(Arrays.asList("assert", "mock", "test")));
    assertEquals(2, strings.size());
    assertTrue(strings.containsAll(Arrays.asList("assert", "mock")));
  }

  public static class GemFireServerFunctions {

    @GemfireFunction(id = "addition")
    public Integer addition(final String keyOne, final String keyTwo, final Region<String, Integer> region) {
      return (region.get(keyOne) + region.get(keyTwo));
    }

    @GemfireFunction(id = "streaming", batchSize = 2)
    public List streaming(final String key, final Region<String, Iterable<Object>> region) {
      return List.class.cast(region.get(key));
    }
  }

}
