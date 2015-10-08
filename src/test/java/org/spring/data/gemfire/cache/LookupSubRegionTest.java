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

import com.gemstone.gemfire.cache.Region;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The LookupSubRegionTest class is a test suite of test cases testing the contract and functionality of Region lookups
 * using Spring Data GemFire configuration and GemFire native cache.xml.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.spring.data.gemfire.AbstractGemFireTest
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see com.gemstone.gemfire.cache.Region
 * @since 1.3.3
 * @since 7.5 (GemFire)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class LookupSubRegionTest extends AbstractGemFireTest {

  @Autowired
  private ApplicationContext context;

  @Test
  public void testLookup() {
    Region parent = context.getBean("Parent", Region.class);

    assertRegion(parent, "Parent", "/Parent");

    Region child = context.getBean("/Parent/Child", Region.class);

    assertRegion(child, "Child", "/Parent/Child");

    Region grandchild = context.getBean("/Parent/Child/Grandchild", Region.class);

    assertRegion(grandchild, "Grandchild", "/Parent/Child/Grandchild");
  }

}
