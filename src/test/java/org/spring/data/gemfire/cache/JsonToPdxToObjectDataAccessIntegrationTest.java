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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxSerializable;
import com.gemstone.gemfire.pdx.PdxSerializer;
import com.gemstone.gemfire.pdx.PdxWriter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.cache.JsonToPdxToObjectDataAccessIntegrationTest.GemFireConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.mapping.Region;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ObjectUtils;

/**
 * The JsonToPdxToObjectDataAccessIntegrationTest class is a test suite of test cases testing the contract
 * and functionality of GemFire's JSON support and GemFire's ability to convert the resulting PDX data back
 * to the desired application domain object type on Region gets and queries.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.cache.JsonToPdxToObjectDataAccessIntegrationTest.GemFireConfiguration
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.mapping.MappingPdxSerializer
 * @see org.springframework.data.gemfire.repository.GemfireRepository
 * @see org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.pdx.JSONFormatter
 * @see com.gemstone.gemfire.pdx.PdxInstance
 * @see com.gemstone.gemfire.pdx.PdxSerializable
 * @see com.gemstone.gemfire.pdx.PdxSerializer
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = GemFireConfiguration.class)
@SuppressWarnings("unused")
public class JsonToPdxToObjectDataAccessIntegrationTest {

  protected static final AtomicLong ID_SEQUENCE = new AtomicLong(0l);

  private Order amazon;
  private Order bestBuy;
  private Order target;
  private Order walmart;

  @Autowired
  private OrderRepository orderRepository;

  @Resource(name = "Orders")
  private com.gemstone.gemfire.cache.Region<Long, Object> orders;

  protected Order createOrder(String name) {
    return createOrder(ID_SEQUENCE.incrementAndGet(), name);
  }

  protected Order createOrder(Long id, String name) {
    return new Order(id, name);
  }

  protected <T> T fromPdx(Object pdxInstance, Class<T> toType) {
    try {
      if (pdxInstance == null) {
        return null;
      }
      else if (toType.isInstance(pdxInstance)) {
        return toType.cast(pdxInstance);
      }
      else if (pdxInstance instanceof PdxInstance) {
        return new ObjectMapper().readValue(JSONFormatter.toJSON(((PdxInstance) pdxInstance)), toType);
      }
      else {
        throw new IllegalArgumentException(String.format("Expected object of type PdxInstance; but was (%1$s)",
          pdxInstance.getClass().getName()));
      }
    }
    catch (IOException e) {
      throw new RuntimeException(String.format("Failed to convert PDX to object of type (%1$s)", toType), e);
    }
  }

  protected void log(Object value) {
    System.out.printf("Object of Type (%1$s) has Value (%2$s)", ObjectUtils.nullSafeClassName(value), value);
  }

  protected Order put(Order order) {
    Object existingOrder = orders.putIfAbsent(order.getTransactionId(), toPdx(order));
    return (existingOrder != null ? fromPdx(existingOrder, Order.class) : order);
  }

  protected PdxInstance toPdx(Object obj) {
    try {
      return JSONFormatter.fromJSON(new ObjectMapper().writeValueAsString(obj));
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException(String.format("Failed to convert object (%1$s) to JSON", obj), e);
    }
  }

  @Before
  public void setup() {
    amazon = put(createOrder("Amazon Order"));
    bestBuy = put(createOrder("BestBuy Order"));
    target = put(createOrder("Target Order"));
    walmart = put(createOrder("Wal-Mart Order"));
  }

  @Test
  public void regionGet() {
    assertThat((Order) orders.get(amazon.getTransactionId()), is(equalTo(amazon)));
  }

  @Test
  public void repositoryFindOneMethod() {
    log(orderRepository.findOne(target.getTransactionId()));
    assertThat(orderRepository.findOne(target.getTransactionId()), is(equalTo(target)));
  }

  @Test
  public void repositoryQueryMethod() {
    assertThat(orderRepository.findByTransactionId(amazon.getTransactionId()), is(equalTo(amazon)));
    assertThat(orderRepository.findByTransactionId(bestBuy.getTransactionId()), is(equalTo(bestBuy)));
    assertThat(orderRepository.findByTransactionId(target.getTransactionId()), is(equalTo(target)));
    assertThat(orderRepository.findByTransactionId(walmart.getTransactionId()), is(equalTo(walmart)));
  }

  @Region("Orders")
  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
  public static class Order implements PdxSerializable {

    protected static final OrderPdxSerializer pdxSerializer = new OrderPdxSerializer();

    @Id
    private Long transactionId;

    private String name;

    public Order() {
    }

    public Order(Long transactionId) {
      this.transactionId = transactionId;
    }

    public Order(Long transactionId, String name) {
      this.transactionId = transactionId;
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public Long getTransactionId() {
      return transactionId;
    }

    public void setTransactionId(final Long transactionId) {
      this.transactionId = transactionId;
    }

    @Override
    public void fromData(PdxReader reader) {
      Order order = (Order) pdxSerializer.fromData(Order.class, reader);

      if (order != null) {
        this.transactionId = order.getTransactionId();
        this.name = order.getName();
      }
    }

    @Override
    public void toData(PdxWriter writer) {
      pdxSerializer.toData(this, writer);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof Order)) {
        return false;
      }

      Order that = (Order) obj;

      return ObjectUtils.nullSafeEquals(this.getTransactionId(), that.getTransactionId());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getTransactionId());
      return hashValue;
    }

    @Override
    public String toString() {
      return String.format("{ @type = %1$s, id = %2$d, name = %3$s }",
        getClass().getName(), getTransactionId(), getName());
    }
  }

  public static class OrderPdxSerializer implements PdxSerializer {

    @Override
    public Object fromData(Class<?> type, PdxReader in) {
      if (Order.class.equals(type)) {
        return new Order(in.readLong("transactionId"), in.readString("name"));
      }

      return null;
    }

    @Override
    public boolean toData(Object obj, PdxWriter out) {
      if (obj instanceof Order) {
        Order order = (Order) obj;
        out.writeLong("transactionId", order.getTransactionId());
        out.writeString("name", order.getName());
        return true;
      }

      return false;
    }
  }

  public interface OrderRepository extends GemfireRepository<Order, Long> {
    Order findByTransactionId(Long transactionId);
  }

  @Configuration
  protected static class GemFireConfiguration {

    @Bean
    public Properties gemfireProperties() {
      Properties gemfireProperties = new Properties();

      gemfireProperties.setProperty("name", JsonToPdxToObjectDataAccessIntegrationTest.class.getSimpleName());
      gemfireProperties.setProperty("mcast-port", "0");
      gemfireProperties.setProperty("log-level", "warning");

      return gemfireProperties;
    }

    @Bean
    public CacheFactoryBean gemfireCache(Properties gemfireProperties) {
      CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

      cacheFactoryBean.setProperties(gemfireProperties);
      //cacheFactoryBean.setPdxSerializer(new MappingPdxSerializer());
      cacheFactoryBean.setPdxSerializer(new OrderPdxSerializer());
      cacheFactoryBean.setPdxReadSerialized(false);

      return cacheFactoryBean;
    }

    @Bean(name = "Orders")
    public PartitionedRegionFactoryBean ordersRegion(Cache gemfireCache) {
      PartitionedRegionFactoryBean regionFactoryBean = new PartitionedRegionFactoryBean();

      regionFactoryBean.setCache(gemfireCache);
      regionFactoryBean.setName("Orders");
      regionFactoryBean.setPersistent(false);

      return regionFactoryBean;
    }

    @Bean
    public GemfireRepositoryFactoryBean orderRepository() {
      GemfireRepositoryFactoryBean<OrderRepository, Order, Long> repositoryFactoryBean =
        new GemfireRepositoryFactoryBean<>();

      repositoryFactoryBean.setRepositoryInterface(OrderRepository.class);

      return repositoryFactoryBean;
    }
  }

}
