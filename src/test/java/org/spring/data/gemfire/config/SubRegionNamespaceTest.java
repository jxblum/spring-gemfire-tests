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

package org.spring.data.gemfire.config;

import static org.junit.Assert.*;

import com.gemstone.gemfire.cache.Region;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author David Turanski
 * @author John Blum
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings({ "unused", "deprecation" })
public class SubRegionNamespaceTest extends AbstractGemFireTest {

  @Autowired
  private ApplicationContext context;

  @Test
  @SuppressWarnings("rawtypes")
  public void testNestedReplicatedRegions() throws Exception {
    Region parent = context.getBean("parent", Region.class);
    Region child = context.getBean("/parent/child", Region.class);
    Region sibling = context.getBean("/parent/sibling", Region.class);
    Region grandchild = context.getBean("/parent/child/grandchild", Region.class);

    printRegionHierarchy(parent);

    assertRegionExists("parent", "/parent", parent);
    assertRegionExists("child", "/parent/child", child);
    assertSame(child, parent.getSubregion("child"));
    assertRegionExists("sibling", "/parent/sibling", sibling);
    assertSame(sibling, parent.getSubregion("sibling"));
    assertRegionExists("grandchild", "/parent/child/grandchild", grandchild);
    assertSame(grandchild, child.getSubregion("grandchild"));
  }

}
