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

package org.spring.data.gemfire.app.main.server;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.RegionAttributes;
import org.spring.data.gemfire.app.beans.Person;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;

/**
 * The SpringGemFireDataServer class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Configuration
@Import(SpringGemFireServer.class)
@SuppressWarnings("unused")
public class SpringGemFireDataServer {

  public static void main(String[] args) {
    System.setProperty("gemfire.name", SpringGemFireDataServer.class.getSimpleName());
    SpringApplication.run(SpringGemFireDataServer.class, args);
  }

  @Bean
  PartitionedRegionFactoryBean<Long, Person> peopleRegion(Cache gemfireCache,
      RegionAttributes<Long, Person> peopleRegionAttributes) {

    PartitionedRegionFactoryBean<Long, Person> peopleRegion = new PartitionedRegionFactoryBean<>();

    peopleRegion.setAttributes(peopleRegionAttributes);
    peopleRegion.setCache(gemfireCache);
    peopleRegion.setClose(false);
    peopleRegion.setName("People");
    peopleRegion.setPersistent(false);

    return peopleRegion;
  }

  @Bean
  @SuppressWarnings("unchecked")
  RegionAttributesFactoryBean peopleRegionAttributes() {
    RegionAttributesFactoryBean peopleRegionAttributes = new RegionAttributesFactoryBean();

    peopleRegionAttributes.setKeyConstraint(Long.class);
    peopleRegionAttributes.setValueConstraint(Person.class);

    return peopleRegionAttributes;
  }
}
