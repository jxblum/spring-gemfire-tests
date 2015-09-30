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

package org.spring.beans;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ObjectUtils;

/**
 * The TestBean class is a Spring bean component.
 *
 * @author John Blum
 * @see org.springframework.beans.factory.InitializingBean
 */
@SuppressWarnings("unused")
public class TestBean implements InitializingBean {

  private volatile boolean initialized = false;

  private String name;

  private TestBean testBean;

  public TestBean() {
  }

  public TestBean(final TestBean testBean) {
    this.testBean = testBean;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public TestBean getTestBean() {
    return testBean;
  }

  public void setTestBean(final TestBean testBean) {
    this.testBean = testBean;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.initialized = true;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof TestBean)) {
      return false;
    }

    final TestBean that = (TestBean) obj;

    return ObjectUtils.nullSafeEquals(this.getName(), that.getName());
  }

  @Override
  public int hashCode() {
    int hashValue = 17;
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.getName());
    return hashValue;
  }

  @Override
  public String toString() {
    return getName();
  }

}
