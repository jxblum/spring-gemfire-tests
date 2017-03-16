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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Properties;
import javax.annotation.Resource;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The DisableConcurrencyChecksCacheRegionTest class...
 *
 * @author John Blum
 * @see org.junit.Test
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DisableConcurrencyChecksCacheRegionTest.GemFireConfiguration.class)
@SuppressWarnings("unused")
public class DisableConcurrencyChecksCacheRegionTest {

  @Resource(name = "consistentRegion")
  private Region<Object, Object> consistentRegion;

  @Resource(name = "inconsistentRegion")
  private Region<Object, Object> inconsistentRegion;

  protected void assertRegion(Region<?, ?> region, String name, DataPolicy dataPolicy, boolean consistent) {
    assertRegion(region, name, String.format("%1$s%2$s", Region.SEPARATOR, name), dataPolicy, consistent);
  }

  protected void assertRegion(Region<?, ?> region, String name, String path, DataPolicy dataPolicy, boolean consistent) {
    assertThat(region, is(notNullValue()));
    assertThat(region.getName(), is(equalTo(name)));
    assertThat(region.getFullPath(), is(equalTo(path)));
    assertThat(region.getAttributes(), is(notNullValue()));
    assertThat(region.getAttributes().getDataPolicy(), is(equalTo(dataPolicy)));
    assertThat(region.getAttributes().getConcurrencyChecksEnabled(), is(consistent));
  }

  @Test
  public void consistentRegionConfiguration() {
    assertRegion(consistentRegion, "Consistent", DataPolicy.PARTITION, true);
  }

  @Test
  public void inconsistentRegionConfiguration() {
    assertRegion(inconsistentRegion, "Inconsistent", DataPolicy.PARTITION, false);
  }

  @Configuration
  static class GemFireConfiguration {

    @Bean
    PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
      return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    Properties gemfireProperties(@Value("${gemfire.log.level:warning}") String logLevel) {
      Properties gemfireProperties = new Properties();

      gemfireProperties.setProperty("name", DisableConcurrencyChecksCacheRegionTest.class.getSimpleName());
      gemfireProperties.setProperty("mcast-port", "0");
      gemfireProperties.setProperty("log-level", logLevel);

      return gemfireProperties;
    }

    @Bean
    CacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) {
      CacheFactoryBean gemfireCache = new CacheFactoryBean();

      gemfireCache.setClose(true);
      gemfireCache.setProperties(gemfireProperties);
      gemfireCache.setUseBeanFactoryLocator(false);

      return gemfireCache;
    }

    @Bean
    PartitionedRegionFactoryBean<Object, Object> consistentRegion(Cache gemfireCache) {
      PartitionedRegionFactoryBean<Object, Object> exampleRegion = new PartitionedRegionFactoryBean<>();

      exampleRegion.setCache(gemfireCache);
      exampleRegion.setClose(false);
      exampleRegion.setName("Consistent");

      return exampleRegion;
    }

    @Bean
    PartitionedRegionFactoryBean<Object, Object> inconsistentRegion(Cache gemfireCache,
        @Qualifier("inconsistentRegionAttributes") RegionAttributes<Object, Object> inconsistentRegionAttributes)
    {
      PartitionedRegionFactoryBean<Object, Object> inconsistentRegion = new PartitionedRegionFactoryBean<>();

      inconsistentRegion.setAttributes(inconsistentRegionAttributes);
      inconsistentRegion.setCache(gemfireCache);
      inconsistentRegion.setClose(false);
      inconsistentRegion.setName("Inconsistent");

      return inconsistentRegion;
    }

    @Bean
    RegionAttributesFactoryBean inconsistentRegionAttributes() {
      return new RegionAttributesFactoryBean();
    }

    @Bean
    BeanPostProcessor disableConcurrencyChecks() {
      return new BeanPostProcessor() {
        public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
          if (bean instanceof RegionAttributesFactoryBean && "inconsistentRegionAttributes".equals(beanName)) {
            ((RegionAttributesFactoryBean) bean).setConcurrencyChecksEnabled(false);
          }

          return bean;
        }

        public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
          return bean;
        }
      };
    }
  }

}
