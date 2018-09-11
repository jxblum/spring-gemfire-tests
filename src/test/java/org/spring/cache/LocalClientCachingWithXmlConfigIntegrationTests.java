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

package org.spring.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The LocalClientCachingWithXmlConfigIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class LocalClientCachingWithXmlConfigIntegrationTests {

  @Autowired
  private CustomerService customerService;

  @Resource(name = "Customers")
  private Region<Long, Customer> customers;

  @Before
  public void setup() {
    //this.customers.removeAll(this.customers.keySetOnServer());
  }

  @Test
  public void cachingWorks() {

    assertThat(this.customers).isNotNull();
    assertThat(this.customers).isEmpty();
    assertThat(this.customerService.isCacheMiss()).isFalse();

    Customer jonDoe = this.customerService.load(1L);

    assertThat(jonDoe).isNotNull();
    assertThat(jonDoe.getId()).isEqualTo(1L);
    assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
    assertThat(this.customerService.isCacheMiss()).isTrue();
    //assertThat(this.customers.sizeOnServer()).isEqualTo(1);
    assertThat(this.customers).hasSize(1);

    Customer jonDoeReloaded = this.customerService.load(jonDoe.getId());

    assertThat(jonDoeReloaded).isEqualTo(jonDoe);
    assertThat(this.customerService.isCacheMiss()).isFalse();
    //assertThat(this.customers.sizeOnServer()).isEqualTo(1);
    assertThat(this.customerService.evictAll()).isTrue();
    //assertThat(this.customers.sizeOnServer()).isEqualTo(0);
    assertThat(this.customers).isEmpty();

    Customer jonDoeTwo = this.customerService.load(2L);

    assertThat(jonDoeTwo).isNotNull();
    assertThat(jonDoeTwo.getId()).isEqualTo(2L);
    assertThat(jonDoeTwo.getName()).isEqualTo("Jon Doe");
    assertThat(this.customerService.isCacheMiss()).isTrue();
    //assertThat(this.customers.sizeOnServer()).isEqualTo(1);
    assertThat(this.customers).hasSize(1);

    Customer jonDoeThree = this.customerService.load(3L);

    assertThat(jonDoeThree).isNotNull();
    assertThat(jonDoeThree.getId()).isEqualTo(3L);
    assertThat(jonDoeThree.getName()).isEqualTo("Jon Doe");
    assertThat(this.customerService.isCacheMiss()).isTrue();
    //assertThat(this.customers.sizeOnServer()).isEqualTo(2);
    assertThat(this.customers).hasSize(2);

    Customer jonDoeTwoReloaded = this.customerService.load(2L);

    assertThat(jonDoeTwoReloaded).isEqualTo(jonDoeTwo);
    assertThat(this.customerService.isCacheMiss()).isFalse();
    //assertThat(this.customers.sizeOnServer()).isEqualTo(2);
    assertThat(this.customerService.evictAll()).isTrue();
    //assertThat(this.customers.sizeOnServer()).isEqualTo(0);
    assertThat(this.customers).isEmpty();
  }

  @Service
  public static class CustomerService {

    private AtomicBoolean cacheMiss = new AtomicBoolean(false);

    @Cacheable("Customers")
    public Customer load(long customerId) {
      this.cacheMiss.set(true);
      return Customer.newCustomer(customerId, "Jon Doe");
    }

    public boolean isCacheMiss() {
      return this.cacheMiss.getAndSet(false);
    }

    @CacheEvict(value = "Customers", allEntries = true)
    public boolean evictAll() {
      return true;
    }
  }


  @Data
  @RequiredArgsConstructor(staticName = "newCustomer")
  static class Customer implements Serializable {

    @Id @NonNull
    private Long id;

    @NonNull
    private String name;

  }
}
