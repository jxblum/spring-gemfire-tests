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

package org.spring.data.gemfire.app.main;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.client.Pool;

import org.spring.data.gemfire.app.beans.Person;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;

/**
 * The SpringGemFireDataClient class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Configuration
@Import(SpringGemFireClient.class)
@SuppressWarnings("all")
public class SpringGemFireDataClient implements CommandLineRunner {

  public static void main(final String[] args) {
    System.setProperty("gemfire.name", SpringGemFireDataClient.class.getSimpleName());
    SpringApplication.run(SpringGemFireDataClient.class, args);
  }

  @Resource(name = "People")
  private Region<Long, Person> people;

  public void run(final String... args) throws Exception {
    System.err.printf("Person is [%1$s]%n", people.get(1l));
  }

  @Bean(name = "People")
  ClientRegionFactoryBean<Long, Person> peopleRegion(GemFireCache clientCache, Pool gemfirePool,
    RegionAttributes<Long, Person> peopleRegionAttributes)
  {
    ClientRegionFactoryBean<Long, Person> peopleRegion = new ClientRegionFactoryBean<>();

    peopleRegion.setAttributes(peopleRegionAttributes);
    peopleRegion.setCache(clientCache);
    peopleRegion.setClose(false);
    peopleRegion.setPool(gemfirePool);
    peopleRegion.setPersistent(false);
    peopleRegion.setShortcut(ClientRegionShortcut.PROXY);

    return peopleRegion;
  }

  @Bean
  @SuppressWarnings("unchecked") RegionAttributesFactoryBean peopleRegionAttributes() {
    RegionAttributesFactoryBean peopleRegionAttributes = new RegionAttributesFactoryBean();

    peopleRegionAttributes.setKeyConstraint(Long.class);
    peopleRegionAttributes.setValueConstraint(Person.class);

    return peopleRegionAttributes;
  }

}
