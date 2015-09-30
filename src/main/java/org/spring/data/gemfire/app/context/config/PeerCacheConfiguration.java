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

package org.spring.data.gemfire.app.context.config;

import java.util.Arrays;
import java.util.Properties;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.EvictionAction;
import com.gemstone.gemfire.cache.EvictionAttributes;
import com.gemstone.gemfire.cache.ExpirationAction;
import com.gemstone.gemfire.cache.ExpirationAttributes;
import com.gemstone.gemfire.cache.PartitionAttributes;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.Scope;

import org.spring.data.gemfire.app.beans.Customer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.DiskStoreFactoryBean;
import org.springframework.data.gemfire.DiskStoreFactoryBean.DiskDir;
import org.springframework.data.gemfire.EvictionAttributesFactoryBean;
import org.springframework.data.gemfire.EvictionPolicyType;
import org.springframework.data.gemfire.ExpirationAttributesFactoryBean;
import org.springframework.data.gemfire.IndexFactoryBean;
import org.springframework.data.gemfire.IndexType;
import org.springframework.data.gemfire.PartitionAttributesFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.data.gemfire.ReplicatedRegionFactoryBean;

/**
 * The PeerCacheConfiguration class...
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class PeerCacheConfiguration {

  @Bean
  public PropertyPlaceholderConfigurer gemfireApplicationSettings() {
    PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();

    Properties placeholders = new Properties();
    placeholders.setProperty("app.gemfire.region.eviction.threshold", "50");
    placeholders.setProperty("app.gemfire.region.entry.expiration.timeout", "120000");
    placeholders.setProperty("app.gemfire.region.partition.local-max-memory", "1024000");
    placeholders.setProperty("app.gemfire.region.partition.total-max-memory", "81920000");

    // NOTE ideally, "placeholder" properties used by Spring's Configurer would be externalized in order to
    // avoid re-compilation on property value changes (this is just an example)!
    propertyPlaceholderConfigurer.setProperties(placeholders);

    return propertyPlaceholderConfigurer;
  }

  @Bean
  public Properties gemfireProperties() {
    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", "SpringApacheGeodePeerCacheExample");
    gemfireProperties.setProperty("mcast-port", "0");
    gemfireProperties.setProperty("log-level", "trace");
    gemfireProperties.setProperty("http-service-port", "8181");
    gemfireProperties.setProperty("locators", "localhost[11235]");
    gemfireProperties.setProperty("jmx-manager", "true");
    gemfireProperties.setProperty("jmx-manager-port", "1199");
    gemfireProperties.setProperty("jmx-manager-start", "true");
    gemfireProperties.setProperty("start-locator", "localhost[11235]");

    return gemfireProperties;
  }

  @Bean
  public CacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) throws Exception {
    CacheFactoryBean cacheFactory = new CacheFactoryBean();
    cacheFactory.setProperties(gemfireProperties);
    return cacheFactory;
  }

  @Bean(name = "StaticReferenceData")
  public ReplicatedRegionFactoryBean<?, ?> exampleReplicateRegion(Cache gemfireCache) {
    ReplicatedRegionFactoryBean<?, ?> staticReferenceData = new ReplicatedRegionFactoryBean<>();

    staticReferenceData.setCache(gemfireCache);
    staticReferenceData.setName("StaticReferenceData");
    staticReferenceData.setPersistent(false);
    staticReferenceData.setScope(Scope.DISTRIBUTED_NO_ACK);

    return staticReferenceData;
  }

  @Bean(name = "CustomersDataStore")
  public DiskStoreFactoryBean exampleDiskStore(Cache gemfireCache) {
    DiskStoreFactoryBean customersDiskStore = new DiskStoreFactoryBean();
    customersDiskStore.setCache(gemfireCache);
    customersDiskStore.setBeanName("CustomerDataStore");
    customersDiskStore.setAutoCompact(true);
    customersDiskStore.setDiskDirs(Arrays.asList(new DiskDir("./gemfire/disk-stores/customers")));
    customersDiskStore.setCompactionThreshold(75);
    customersDiskStore.setMaxOplogSize(10);
    customersDiskStore.setQueueSize(100);
    customersDiskStore.setTimeInterval(300000);
    return customersDiskStore;
  }

  @Bean(name = "Customers")
  public PartitionedRegionFactoryBean<Long, Customer> examplePartitionRegion(Cache gemfireCache,
    @Qualifier("partitionRegionAttributes") RegionAttributes<Long, Customer> regionAttributes)
      throws Exception
  {
    PartitionedRegionFactoryBean<Long , Customer> customers = new PartitionedRegionFactoryBean<>();
    customers.setAttributes(regionAttributes);
    customers.setCache(gemfireCache);
    customers.setName("Customers");
    customers.setPersistent(true);
    return customers;
  }

  @Bean
  public RegionAttributesFactoryBean partitionRegionAttributes(PartitionAttributes partitionAttributes,
      EvictionAttributes evictionAttributes, ExpirationAttributes expirationAttributes) {

    RegionAttributesFactoryBean partitionRegionAttributes = new RegionAttributesFactoryBean();

    partitionRegionAttributes.setPartitionAttributes(partitionAttributes);
    partitionRegionAttributes.setEvictionAttributes(evictionAttributes);
    partitionRegionAttributes.setEntryIdleTimeout(expirationAttributes);
    partitionRegionAttributes.setStatisticsEnabled(true);

    return partitionRegionAttributes;
  }

  @Bean
  public PartitionAttributesFactoryBean examplePartitionAttributes(@Value("${app.gemfire.region.partition.local-max-memory}") int localMaxMemory,
                                                                   @Value("${app.gemfire.region.partition.total-max-memory}") int totalMaxMemory)
  {
    PartitionAttributesFactoryBean partitionAttributes = new PartitionAttributesFactoryBean();

    partitionAttributes.setLocalMaxMemory(localMaxMemory);
    partitionAttributes.setTotalMaxMemory(totalMaxMemory);

    return partitionAttributes;
  }

  @Bean
  public EvictionAttributesFactoryBean exampleEvictionAttributes(@Value("${app.gemfire.region.eviction.threshold}") int threshold)
  {
    EvictionAttributesFactoryBean exampleEvictionAttributes = new EvictionAttributesFactoryBean();

    exampleEvictionAttributes.setAction(EvictionAction.OVERFLOW_TO_DISK);
    exampleEvictionAttributes.setThreshold(threshold);
    exampleEvictionAttributes.setType(EvictionPolicyType.MEMORY_SIZE);

    return exampleEvictionAttributes;
  }

  @Bean
  public ExpirationAttributesFactoryBean exampleExpirationAttributes(@Value("${app.gemfire.region.entry.expiration.timeout}") int timeout)
  {
    ExpirationAttributesFactoryBean exampleExpirationAttributes = new ExpirationAttributesFactoryBean();

    exampleExpirationAttributes.setAction(ExpirationAction.LOCAL_DESTROY);
    exampleExpirationAttributes.setTimeout(timeout);

    return exampleExpirationAttributes;
  }

  @Bean
  public IndexFactoryBean exampleIndex(Cache gemfireCache) {
    IndexFactoryBean customerId = new IndexFactoryBean();

    customerId.setCache(gemfireCache);
    customerId.setName("CustomerIdIdx");
    customerId.setExpression("id");
    customerId.setFrom("/Customers");
    customerId.setType(IndexType.PRIMARY_KEY);

    return customerId;
  }

}
