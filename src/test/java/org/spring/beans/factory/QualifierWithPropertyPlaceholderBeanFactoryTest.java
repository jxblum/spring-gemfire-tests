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
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;

import java.util.Optional;
import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

/**
 * The QualifierWithPropertyPlaceholderBeanFactoryTest class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class QualifierWithPropertyPlaceholderBeanFactoryTest {

  private static final String QUALIFIER = "testServiceOne";

  @BeforeClass
  public static void setup() {
    System.setProperty("test.service.qualifier", QUALIFIER);
  }

  //@Autowired
  //@Qualifier("testServiceOne")
  //@Qualifier("${test.service.qualifier:testServiceZero}")
  @Resource(name = "${test.service.qualifier:testServiceZero}")
  @SuppressWarnings("all")
  private TestService testService;

  @Test
  public void qualifiedTestServiceIsOne() {
    assertThat(this.testService).isNotNull();
    assertThat(this.testService.toString()).isEqualTo("ONE");
  }

  @Configuration
  @SuppressWarnings("unused")
  static class TestConfiguration {

    @Bean
    TestService testServiceOne() {
      return new TestService("ONE");
    }

    @Bean
    TestService testServiceTwo() {
      return new TestService("TWO");
    }
  }

  @Service
  static class TestService {

    private final String description;

    TestService(String description) {
      this.description = Optional.ofNullable(description)
        .filter(StringUtils::hasText)
        .orElseThrow(() -> newIllegalArgumentException("Description is required"));
    }

    @Override
    public String toString() {
      return description;
    }
  }
}
