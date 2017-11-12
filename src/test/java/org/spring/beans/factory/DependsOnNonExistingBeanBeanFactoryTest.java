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

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The DependsOnNonExistingBeanBeanFactoryTest class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class DependsOnNonExistingBeanBeanFactoryTest {

  @Resource(name = "DependentBean")
  private Object dependentBean;

  @Test
  public void testBeanIsNotNull() {
    assertThat(this.dependentBean).isEqualTo("test");
  }

  @Configuration
  static class TestConfiguration {

    @Bean(name = "DependentBean")
    @DependsOn("NonExistingBean")
    Object dependentBean() {
      return "test";
    }
  }
}
