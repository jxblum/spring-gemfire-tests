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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.codeprimate.lang.ObjectUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.convert.converter.Converter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.springframework.util.StringValueResolver;

/**
 * The BeanAtValueAnnotationTest class is a test suite of test cases testing the contract and functionality
 * of the Spring Bean @Value annotation.
 *
 * @author John Blum
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class BeanAtValueAnnotationTest implements EmbeddedValueResolverAware {

  @Autowired
  private ConfigurableBeanFactory beanFactory;

  //@Value("${app.object.value}")
  @Value("#{@applicationProperties['app.object.value']}")
  private Person person;

  @Autowired
  @Qualifier("otherProperties")
  private Properties otherProperties;

  @Value("${app.string.value}")
  private String value;

  private StringValueResolver stringValueResolver;

  @Override
  public void setEmbeddedValueResolver(final StringValueResolver stringValueResolver) {
    this.stringValueResolver = stringValueResolver;
  }

  @Test
  public void otherProperties() {
    assertEquals(Person.JON_DOE, otherProperties.get("my.key"));
  }

  @Test
  public void person() {
    assertThat(person, is(equalTo(Person.JON_DOE)));
  }

  @Test
  public void resolveEmbeddedValue() {
    assertThat(beanFactory, is(not(nullValue())));
    assertThat(beanFactory.resolveEmbeddedValue("${app.string.value}"), is(equalTo("test")));
  }

  @Test
  public void resolveStringValue() {
    assertThat(stringValueResolver, is(not(nullValue())));
    assertThat(stringValueResolver.resolveStringValue("${app.string.value}"), is(equalTo("test")));
  }

  @Test
  public void value() {
    assertThat(value, is(equalTo("test")));
  }

  public static class Person {

    public static final Person JON_DOE = new Person("Jon", "Doe");

    private final String firstName;
    private final String lastName;

    public Person(final String firstName, final String lastName) {
      Assert.hasText(firstName, "a person's first name must be specified");
      Assert.hasText(lastName, "a person's last name must be specified");
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public String getName() {
      return String.format("%1$s %2$s", getFirstName(), getLastName());
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof Person)) {
        return false;
      }

      Person that = (Person) obj;

      return ObjectUtils.nullSafeEquals(this.getFirstName(), that.getFirstName())
        && ObjectUtils.nullSafeEquals(this.getLastName(), that.getLastName());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + ObjectUtils.hashCode(getFirstName());
      hashValue = 37 * hashValue + ObjectUtils.hashCode(getLastName());
      return hashValue;
    }

    @Override
    public String toString() {
      return getName();
    }
  }

  public static class PersonToStringConverter implements Converter<Person, String> {

    @Override
    public String convert(final Person source) {
      return source.toString();
    }
  }

}
