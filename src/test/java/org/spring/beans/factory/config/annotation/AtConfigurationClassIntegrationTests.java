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

package org.spring.beans.factory.config.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.beans.factory.config.annotation.AtConfigurationClassIntegrationTests.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Integration tests exposing Spring's {@link Configuration @Configuration} class behavior.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see lombok.Data
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@SuppressWarnings("unused")
public class AtConfigurationClassIntegrationTests {

  @Autowired(required = false)
  private ParentConfiguration parentConfiguration;

  @Autowired(required = false)
  private ChildConfiguration childConfiguration;

  @Autowired(required = false)
  private GrandchildConfiguration grandchildConfiguration;

  @Autowired(required = false)
  @Qualifier("Parent")
  private NamedBean parent;

  @Autowired(required = false)
  @Qualifier("Child")
  private NamedBean child;

  @Autowired(required = false)
  @Qualifier("Grandchild")
  private NamedBean grandchild;

  @Test
  public void configurationIsCorrect() {
    assertThat(parentConfiguration).isNotNull();
    assertThat(childConfiguration).isNotNull();
    assertThat(grandchildConfiguration).isNotNull();
    assertThat(parent).isNotNull();
    assertThat(child).isNotNull();
    assertThat(grandchild).isNotNull();
  }

  @Configuration
  @EnableConfiguration
  static class TestConfiguration {
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Import({ ParentConfiguration.class })
  //@Import({ ParentConfiguration.class, ChildConfiguration.class, GrandchildConfiguration.class })
  @interface EnableConfiguration {
  }

  @Configuration
  @Import(ChildConfiguration.class)
  //@Import(GrandchildConfiguration.class)
  //@Import({ ChildConfiguration.class, GrandchildConfiguration.class })
  static class ParentConfiguration {

    @Autowired(required = false)
    private ParentConfiguration parentConfiguration;

    @Autowired(required = false)
    private ChildConfiguration childConfiguration;

    //@Autowired
    private List<AbstractBean> beans = Collections.emptyList();

    @Bean("Parent")
    NamedBean parent() {
      return NamedBean.newBean("Parent");
    }

    @PostConstruct
    public void postProcess() {
      System.err.printf("[%1$s] Named Beans [%2$s]%n", getClass().getSimpleName(), beans);
    }
  }

  @Configuration
  @Import(GrandchildConfiguration.class)
  static class ChildConfiguration {

    //@Autowired(required = false)
    @Qualifier("Parent")
    private NamedBean parent;

    //@Autowired(required = false)
    @Qualifier("Child")
    private NamedBean child;

    //@Autowired(required = false)
    @Qualifier("Grandchild")
    private NamedBean grandchild;

    //@Autowired
    @Qualifier("Child")
    private AbstractBean someBean;

    //@Autowired(required = false)
    private ParentConfiguration parentConfiguration;

    @Autowired(required = false)
    private ChildConfiguration childConfiguration;

    @Autowired(required = false)
    private GrandchildConfiguration grandchildConfiguration;

    @Autowired
    private List<NamedBean> beans = Collections.emptyList();

    @Bean("Child")
    NamedBean child() {
      return NamedBean.newBean("Child");
    }

    @PostConstruct
    public void postProcess() {
      if (childConfiguration != null) {
        System.err.printf("Configuration [%1$s] is null [%2$s]%n",
          GrandchildConfiguration.class.getSimpleName(), (grandchildConfiguration == null));

        System.err.printf("Configuration [%1$s] is null [%2$s]%n",
          ChildConfiguration.class.getSimpleName(), (childConfiguration == null));

        System.err.printf("Configuration [%1$s] is null [%2$s]%n",
          ParentConfiguration.class.getSimpleName(), (parentConfiguration == null));

        System.err.printf("Bean [%1$s] is null [%2$s]%n", grandchild, (grandchild == null));
        System.err.printf("Bean [%1$s] is null [%2$s]%n", child, (child == null));
        System.err.printf("Bean [%1$s] is null [%2$s]%n", someBean, (someBean == null));
        System.err.printf("Bean [%1$s] is null [%2$s]%n", parent, (parent == null));
      }

      System.err.printf("[%1$s] Named Beans [%2$s]%n", getClass().getSimpleName(), beans);
    }
  }

  @Configuration
  static class GrandchildConfiguration {

    //@Autowired(required = false)
    private ChildConfiguration childConfiguration;

    @Autowired
    private GrandchildConfiguration grandchildConfiguration;

    @Autowired
    private List<NamedBean> beans = Collections.emptyList();

    @Bean("Grandchild")
    NamedBean grandChild() {
      return NamedBean.newBean("Grandchild");
    }

    @PostConstruct
    public void postProcess() {
      System.err.printf("[%1$s] Named Beans [%2$s]%n", getClass().getSimpleName(), beans);
    }
  }

  public static abstract class AbstractBean {
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @RequiredArgsConstructor(staticName = "newBean")
  public static class NamedBean extends AbstractBean {

    @NonNull private final String name;

    @Override
    public String toString() {
      return getName();
    }
  }
}
