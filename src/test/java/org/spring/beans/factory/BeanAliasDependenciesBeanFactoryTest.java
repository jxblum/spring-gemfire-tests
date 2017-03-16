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

package org.spring.beans.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.beans.TestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The BeanAliasDependenciesBeanFactoryTest class is a test suite of test cases testing the functionality of Spring's
 * BeanFactory when using bean aliases to declare dependencies between collaborating beans.
 *
 * @author John Blum
 * @see org.junit.Assert
 * @see org.junit.Test
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 3.2.4
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class BeanAliasDependenciesBeanFactoryTest {

  @Autowired
  private ApplicationContext context;

  @Resource(name = "dep")
  private TestBean testBean;

  @Test
  public void testDependenciesSatisfied() {
    assertNotNull(testBean);
    assertEquals("Dependent", testBean.getName());
    assertSame(testBean, context.getBean("dependentBean"));
    assertSame(testBean, context.getBean("Dependent"));
  }

}
