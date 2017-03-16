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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.beans.TestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The BeanCreationBeanFactoryTest class is a test suite of test case testing the Bean lifecycle of the
 * Spring IoC container.
 *
 * @author John Blum
 * @see org.junit.Assert
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class BeanCreationBeanFactoryTest {

  @Autowired
  private ApplicationContext context;

  @Test
  public void testBeanCreation() {
    final TestBean beanOne = context.getBean("beanOne", TestBean.class);

    assertNotNull(beanOne);
    assertEquals("BeanOne", beanOne.getName());
    assertNull(beanOne.getTestBean());
    assertTrue(beanOne.isInitialized());

    final TestBean beanTwo = context.getBean("beanTwo", TestBean.class);

    assertNotNull(beanTwo);
    assertEquals("BeanTwo", beanTwo.getName());
    assertNull(beanTwo.getTestBean());
    // NOTE assertion failed! beanTwo does not get automatically initialized by the Spring IoC container when created by a FactoryBean!
    // Actually, I thought I remember reading Spring Framework reference documentation explaining that initialization
    // callbacks need to be called explicitly by the FactoryBean, but I cannot find it in the latest reference docs @...
    // http://docs.spring.io/spring/docs/3.2.x/spring-framework-reference/html/beans.html#beans-factory-extension-factorybean
    assertTrue(beanTwo.isInitialized());
    //assertFalse(beanTwo.isInitialized()); // assertTrue if the FactoryBean explicitly initializes the Bean

    final TestBeanFactory beanTwoFactory = context.getBean("&beanTwo", TestBeanFactory.class);

    assertNotNull(beanTwoFactory);
    assertEquals("BeanTwo", beanTwoFactory.getName());
    assertNull(beanTwoFactory.getTestBean());
    assertTrue(beanTwoFactory.isInitialized());

    final TestBean beanThree = context.getBean("beanThree", TestBean.class);

    assertNotNull(beanThree);
    assertEquals("BeanThree", beanThree.getName());
    assertNotNull(beanThree.getTestBean());
    assertTrue(beanThree.isInitialized());
    assertFalse(context.containsBean("childBeanThree"));

    final TestBean childBeanThree = beanThree.getTestBean();

    assertNotNull(childBeanThree);
    assertEquals("ChildBeanThree", childBeanThree.getName());
    assertNotNull(childBeanThree.getTestBean());
    // NOTE assert failed! childBeanThree will get initialized by the Spring IoC container even if an inner bean!
    //assertFalse(childBeanThree.isInitialized());
    assertTrue(childBeanThree.isInitialized());
    assertFalse(context.containsBean("grandchildBeanThree"));

    final TestBean grandchildBeanThree = childBeanThree.getTestBean();

    assertNotNull(grandchildBeanThree);
    assertEquals("GrandchildBeanThree", grandchildBeanThree.getName());
    assertNull(grandchildBeanThree.getTestBean());
    assertTrue(grandchildBeanThree.isInitialized());

    final TestBean beanFour = context.getBean("beanFour", TestBean.class);

    assertNotNull(beanFour);
    assertEquals("BeanFour", beanFour.getName());
    assertNotNull(beanFour.getTestBean());
    assertTrue(beanFour.isInitialized());
    //assertFalse(beanFour.isInitialized()); // assertTrue if the FactoryBean explicitly initializes the Bean
    assertFalse(context.containsBean("childBeanFour"));

    final TestBean childBeanFour = beanFour.getTestBean();

    assertNotNull(childBeanFour);
    assertEquals("ChildBeanFour", childBeanFour.getName());
    assertNotNull(childBeanFour.getTestBean());
    assertTrue(childBeanFour.isInitialized());
    //assertFalse(childBeanFour.isInitialized()); // assertTrue if the FactoryBean explicitly initializes the Bean
    assertFalse(context.containsBean("grandchildBeanFour"));

    final TestBean grandchildBeanFour = childBeanFour.getTestBean();

    assertNotNull(grandchildBeanFour);
    assertEquals("GrandchildBeanFour", grandchildBeanFour.getName());
    assertNull(grandchildBeanFour.getTestBean());
    assertTrue(grandchildBeanFour.isInitialized());
    //assertFalse(grandchildBeanFour.isInitialized()); // assertTrue if the FactoryBean explicitly initializes the Bean

    final TestBeanFactory beanFourFactory = context.getBean("&beanFour", TestBeanFactory.class);

    assertNotNull(beanFourFactory);
    assertEquals("BeanFour", beanFourFactory.getName());
    assertNotNull(beanFourFactory.getTestBean());
    assertSame(childBeanFour, beanFourFactory.getTestBean());
    assertTrue(beanFourFactory.isInitialized());
  }

}
