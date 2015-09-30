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

import org.spring.beans.TestBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * The TestFactoryBean class is a Spring FactoryBean constructing an instance of the TestBean Spring bean component.
 *
 * @author John Blum
 * @see org.spring.beans.TestBean
 * @see org.springframework.beans.factory.FactoryBean
 * @see org.springframework.beans.factory.InitializingBean
 */
@SuppressWarnings("unused")
public class TestBeanFactory implements FactoryBean<TestBean>, InitializingBean {

  private volatile boolean initialized = false;

  private String name;

  private TestBean bean;
  private TestBean testBean;

  protected TestBean getBean() {
    Assert.state(bean != null, "The TestBean was not created!");
    return bean;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public final void setName(final String name) {
    this.name = name;
  }

  protected String getName() {
    return name;
  }

  public final void setTestBean(final TestBean testBean) {
    this.testBean = testBean;
  }

  protected TestBean getTestBean() {
    return testBean;
  }

  public TestBean getObject() throws Exception {
    return getBean();
  }

  public Class<?> getObjectType() {
    return TestBean.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void afterPropertiesSet() throws Exception {
    bean = new TestBean();
    bean.setName(getName());
    bean.setTestBean(getTestBean());
    bean.afterPropertiesSet();
    this.initialized = true;
  }

}
