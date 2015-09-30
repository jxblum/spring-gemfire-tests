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

package org.spring.data.gemfire.app.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.gemstone.gemfire.GemFireException;
import com.gemstone.gemfire.cache.CacheWriterException;
import com.gemstone.gemfire.cache.LowMemoryException;
import com.gemstone.gemfire.cache.PartitionedRegionStorageException;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.TimeoutException;
import com.gemstone.gemfire.distributed.LeaseExpiredException;

import org.spring.data.gemfire.app.beans.Customer;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.gemfire.GemfireCacheUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * The GemfireRegionCustomerDao class is a Repository bean for managing data access operation on Customers.
 *
 * @author John Blum
 * @see org.springframework.stereotype.Repository
 * @see com.gemstone.gemfire.cache.Region
 * @since 1.0.0
 */
@Repository
@SuppressWarnings("unused")
public class GemfireRegionCustomerDao {

  protected static final AtomicLong ID_SEQUENCE = new AtomicLong(0l);

  @Resource(name = "Customers")
  private Region<Long, Customer> customers;

  protected Region<Long, Customer> customersRegion() {
    Assert.state(customers != null, "The 'Customers' Region bean was not properly initialized!");
    return customers;
  }

  @PostConstruct
  public void init() {
    Region customers = customersRegion();
    Assert.isTrue("Customers".equals(customers.getName()));
    System.out.printf("%1$s initialized!%n", this);
  }

  // Implement all CRUD (CREATE, READ, UPDATE, DELETE) operations (ops)

  // Naive impl tightly coupled to the GemFire/Geode API, no Exception Handling/Translation,
  // no Transaction Management, etc.

  public Customer save(Customer customer) {
    try {
      if (customer.isNew()) {
        customer.setId(ID_SEQUENCE.incrementAndGet());
        customersRegion().putIfAbsent(customer.getId(), customer);
      }
      else {
        customersRegion().put(customer.getId(), customer);
      }

      return customer;
    }
    catch (CacheWriterException e) {
      throw new DataAccessResourceFailureException("write-through failed", e);
    }
    catch (LeaseExpiredException e) {
      throw new PessimisticLockingFailureException("global lock lease expired", e);
    }
    catch (LowMemoryException e) {
      throw new DataAccessResourceFailureException("low memory", e);
    }
    catch (PartitionedRegionStorageException e) {
      throw new DataAccessResourceFailureException("PR op failure", e);
    }
    catch (TimeoutException e) {
      throw new DeadlockLoserDataAccessException("global lock acquisition timeout", e);
    }
    catch (GemFireException e) {
      throw GemfireCacheUtils.convertGemfireAccessException(e);
    }
  }

  public Iterable<Customer> save(Iterable<Customer> customers) {
    for (Customer customer : customers) {
      save(customer);
    }

    return customers;
  }

  public int count() {
    return customersRegion().size();
  }

  public boolean exists(Long id) {
    return (findOne(id) != null);
  }

  public Customer findOne(Long id) {
    return customersRegion().get(id);
  }

  public List<Customer> findAll() {
    return new ArrayList<>(customersRegion().values());
  }

  public List query(String predicate) throws Exception {
    return customersRegion().query(predicate).asList();
  }

  public boolean delete(Customer customer) {
    return (customersRegion().remove(customer.getId()) != null);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
