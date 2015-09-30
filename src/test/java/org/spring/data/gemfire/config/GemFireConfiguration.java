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

package org.spring.data.gemfire.config;

import java.util.Properties;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.EvictionAction;
import com.gemstone.gemfire.cache.EvictionAttributes;
import com.gemstone.gemfire.cache.PartitionAttributes;
import com.gemstone.gemfire.cache.RegionAttributes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.EvictionAttributesFactoryBean;
import org.springframework.data.gemfire.EvictionPolicyType;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.PartitionAttributesFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;

/**
 * The GemFireConfiguration class is a Spring context configuration meta-data class (using Spring Container,
 * Java-based configuration, or JavaConfig).
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class GemFireConfiguration {

  @Bean
  public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
    PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();

    Properties placeholders = new Properties();
    placeholders.setProperty("app.gemfire.defaults.region.eviction.threshold", "4096");
    placeholders.setProperty("app.gemfire.defaults.region.partition.local-max-memory", "16384");
    placeholders.setProperty("app.gemfire.defaults.region.partition.total-max-memory", "32768");

    // NOTE ideally, "placeholder" properties used by Spring's Configurer would be externalized in order to
    // avoid re-compilation on property value changes (this is just an example)!
    propertyPlaceholderConfigurer.setProperties(placeholders);

    return propertyPlaceholderConfigurer;
  }

  @Bean
  public Properties gemfireProperties() {
    Properties gemfireProperties = new Properties();
    gemfireProperties.setProperty("name", "SpringGemFireJavaConfigTest");
    gemfireProperties.setProperty("mcast-port", "0");
    gemfireProperties.setProperty("log-level", "config");
    return gemfireProperties;
  }

  @Bean
  @Autowired
  public CacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) throws Exception {
    CacheFactoryBean cacheFactory = new CacheFactoryBean();
    cacheFactory.setProperties(gemfireProperties);
    return cacheFactory;
  }

  @Autowired
  @Bean(name = "ExampleLocal")
  public LocalRegionFactoryBean<Long, String> exampleLocalRegion(Cache gemfireCache) throws Exception {
    LocalRegionFactoryBean<Long, String> exampleLocalRegion = new LocalRegionFactoryBean<>();
    exampleLocalRegion.setCache(gemfireCache);
    exampleLocalRegion.setName("ExampleLocal");
    return exampleLocalRegion;
  }

  @Autowired
  @Bean(name = "ExampleEvictionLocal")
  public LocalRegionFactoryBean<Object, Object> exampleEvictionLocalRegion(Cache gemfireCache,
    @Qualifier("defaultRegionAttributes") RegionAttributes<Object, Object> regionAttributes)
      throws Exception
  {
    LocalRegionFactoryBean<Object, Object> anotherExampleLocalRegion = new LocalRegionFactoryBean<>();
    anotherExampleLocalRegion.setAttributes(regionAttributes);
    anotherExampleLocalRegion.setCache(gemfireCache);
    anotherExampleLocalRegion.setName("ExampleEvictionLocal");
    return anotherExampleLocalRegion;
  }

  /*
  NOTE injecting GemFire Regions using Spring JavaConfig seems to be a problem for Spring (since a Region
  is a java.util.Map) even when the @Resource annotation is used, specifically in a @Configuration class
  with @Bean definitions (argh!) Not sure how to fix this at the moment.
  */
  /*
  @Bean
  @Resource(name = "ExampleLocal")
  public GemfireTemplate exampleLocalRegionTemplate(Region<Long, String> exampleLocal) throws Exception {
    return new GemfireTemplate(exampleLocal);
  }
  */

  @Bean
  @Autowired
  public GemfireTemplate exampleLocalRegionTemplate(Cache gemfireCache) throws Exception {
    return new GemfireTemplate(exampleLocalRegion(gemfireCache).getObject());
  }

  @Autowired
  @Bean(name = "ExamplePartition")
  /*
  NOTE need to qualify the RegionAttributes bean definition reference since GemFire's
  com.gemstone.gemfire.internal.cache.AbstractRegion class "implements" RegionAttributes (face-palm),
  which led Spring to the following Exception...

  java.lang.IllegalStateException: Failed to load ApplicationContext ...
  Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException:
  Error creating bean with name 'ExamplePartition' defined in class org.spring.data.gemfire.config.GemFireConfiguration:
  Unsatisfied dependency expressed through constructor argument with index 1 of type [com.gemstone.gemfire.cache.RegionAttributes]:
  No qualifying bean of type [com.gemstone.gemfire.cache.RegionAttributes] is defined:
  expected single matching bean but found 2: ExampleLocal,defaultRegionAttributes;
  nested exception is org.springframework.beans.factory.NoUniqueBeanDefinitionException:
  No qualifying bean of type [com.gemstone.gemfire.cache.RegionAttributes] is defined:
  expected single matching bean but found 2: ExampleLocal,defaultRegionAttributes
  */
  public PartitionedRegionFactoryBean<Object, Object> examplePartitionRegion(Cache gemfireCache,
    @Qualifier("partitionRegionAttributes") RegionAttributes<Object, Object> regionAttributes)
      throws Exception
  {
    PartitionedRegionFactoryBean<Object, Object> examplePartitionRegion = new PartitionedRegionFactoryBean<>();
    examplePartitionRegion.setAttributes(regionAttributes);
    examplePartitionRegion.setCache(gemfireCache);
    examplePartitionRegion.setName("ExamplePartition");
    examplePartitionRegion.setPersistent(false);
    return examplePartitionRegion;
  }

  @Bean
  @Autowired
  public RegionAttributesFactoryBean defaultRegionAttributes(EvictionAttributes evictionAttributes) {
    RegionAttributesFactoryBean regionAttributes = new RegionAttributesFactoryBean();
    regionAttributes.setEvictionAttributes(evictionAttributes);
    regionAttributes.setStatisticsEnabled(true);
    return regionAttributes;
  }

  @Bean
  @Autowired
  public RegionAttributesFactoryBean partitionRegionAttributes(PartitionAttributes partitionAttributes, EvictionAttributes evictionAttributes) {
    RegionAttributesFactoryBean regionAttributes = new RegionAttributesFactoryBean();
    regionAttributes.setEvictionAttributes(evictionAttributes);
    regionAttributes.setPartitionAttributes(partitionAttributes);
    return regionAttributes;
  }

  @Bean
  @Autowired
  public PartitionAttributesFactoryBean defaultPartitionAttributes(@Value("${app.gemfire.defaults.region.partition.local-max-memory}") int localMaxMemory,
                                                                   @Value("${app.gemfire.defaults.region.partition.total-max-memory}") int totalMaxMemory)
  {
    PartitionAttributesFactoryBean partitionAttributes = new PartitionAttributesFactoryBean();
    partitionAttributes.setLocalMaxMemory(localMaxMemory);
    partitionAttributes.setTotalMaxMemory(totalMaxMemory);
    return partitionAttributes;
  }

  @Bean
  @Autowired
  public EvictionAttributesFactoryBean defaultEvictionAttributes(@Value("${app.gemfire.defaults.region.eviction.threshold}") int threshold) {
    EvictionAttributesFactoryBean evictionAttributes = new EvictionAttributesFactoryBean();
    evictionAttributes.setAction(EvictionAction.LOCAL_DESTROY);
    evictionAttributes.setThreshold(threshold);
    evictionAttributes.setType(EvictionPolicyType.MEMORY_SIZE);
    return evictionAttributes;
  }

}
