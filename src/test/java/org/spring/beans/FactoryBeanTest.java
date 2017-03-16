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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.codeprimate.lang.ObjectUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.beans.FactoryBeanTest.FactoryBeanConfiguration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The FactoryBeanTest class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FactoryBeanConfiguration.class)
@SuppressWarnings("unused")
public class FactoryBeanTest {

  private static final boolean IS_SINGLETON = false;

  @Autowired
  @Qualifier("nodeOne")
  private Node<Integer> nodeOne;

  @Autowired
  @Qualifier("nodeTwo")
  private Node<Integer> nodeTwo;

  @Autowired
  @Qualifier("nodeThree")
  private Node<Integer> nodeThree;

  @Autowired
  @Qualifier("&nodeOne")
  private NodeFactoryBean<Integer> nodeOneFactoryBean;

  protected <T> void assertNode(final Node<T> actualNode, T value, Node<T> link) {
    assertThat(actualNode, is(notNullValue()));
    assertThat(actualNode.getValue(), is(equalTo(value)));
    assertThat(actualNode.getLink(), is(equalTo(link)));
  }

  @Test
  public void factoryBeanGetObjectCallCount() throws Exception {
    assertNode(nodeOne, 1, null);
    assertThat(nodeOneFactoryBean.getObjectCallCount(), is(equalTo(IS_SINGLETON ? 1 : 3)));
    assertThat(nodeOneFactoryBean.getObject(), is(equalTo(nodeOne)));
    assertNode(nodeTwo, 2, nodeOne);
    assertNode(nodeThree, 3, nodeOne);
  }

  public static class Node<T> {

    private final T value;

    private Node<T> node;

    public Node(T value) {
      this.value = value;
    }

    public Node<T> getLink() {
      return node;
    }

    public void setLink(final Node<T> node) {
      this.node = node;
    }

    public T getValue() {
      return value;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof Node)) {
        return false;
      }

      Node that = (Node) obj;

      return ObjectUtils.nullSafeEquals(this.getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + ObjectUtils.hashCode(getValue());
      return hashValue;
    }

    @Override
    public String toString() {
      return String.valueOf(getValue());
    }
  }

  public static class NodeFactoryBean<T> implements FactoryBean<Node>, InitializingBean {

    private final AtomicInteger getObjectCallCount = new AtomicInteger(0);

    private T value;

    private Node<T> link;
    private Node<T> node;

    @Override
    public void afterPropertiesSet() throws Exception {
      this.node = new Node<>(value);
      this.node.setLink(link);
    }

    @Override
    public Node getObject() throws Exception {
      getObjectCallCount.incrementAndGet();
      return node;
    }

    protected int getObjectCallCount() {
      return getObjectCallCount.get();
    }

    @Override
    public Class<?> getObjectType() {
      return (node != null ? node.getClass() : Node.class);
    }

    @Override
    public boolean isSingleton() {
      return IS_SINGLETON;
    }

    public NodeFactoryBean<T> setLink(final Node<T> link) {
      this.link = link;
      return this;
    }

    public NodeFactoryBean<T> setValue(final T value) {
      this.value = value;
      return this;
    }
  }

  @Configuration
  public static class FactoryBeanConfiguration {

    @Bean
    public NodeFactoryBean<Integer> nodeOne() {
      return new NodeFactoryBean<Integer>().setValue(1);
    }

    @Bean
    public NodeFactoryBean<Integer> nodeTwo(@Qualifier("nodeOne") Node<Integer> nodeOne) {
      return new NodeFactoryBean<Integer>().setValue(2).setLink(nodeOne);
    }

    @Bean
    public NodeFactoryBean<Integer> nodeThree(@Qualifier("nodeOne") Node<Integer> nodeOne) {
      return new NodeFactoryBean<Integer>().setValue(3).setLink(nodeOne);
    }
  }

}
