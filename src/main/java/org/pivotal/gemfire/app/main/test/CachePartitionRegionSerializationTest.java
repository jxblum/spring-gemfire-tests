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

package org.pivotal.gemfire.app.main.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializer;
import org.apache.geode.pdx.PdxWriter;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The CachePartitionRegionSerializationTest class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class CachePartitionRegionSerializationTest {

  public static void main(String[] args) {

    Cache gemfireCache = new CacheFactory()
      .set("name", CachePartitionRegionSerializationTest.class.getSimpleName())
      .set("log-level", "config")
      //.setPdxSerializer(new ReflectionBasedAutoSerializer("org\\.pivotal\\.gemfire\\.*."))
      .setPdxSerializer(new PersonPdxSerializer())
      .create();

    RegionFactory<Long, Person> regionFactory = gemfireCache.createRegionFactory();

    regionFactory.setDataPolicy(DataPolicy.PARTITION);

    Region<Long, Person> people = regionFactory.create("People");

    Person jonDoe = Person.newPerson("Jon", "Doe");

    assertThat(people.put(1L, jonDoe)).isNull();
    assertThat(people.get(1L)).isEqualTo(jonDoe);

    gemfireCache.close();
  }

  @Data
  @RequiredArgsConstructor(staticName = "newPerson")
  static class Person {

    @NonNull private String firstName;
    @NonNull private String lastName;
  }

  static class PersonPdxSerializer implements PdxSerializer {

    @Override
    public boolean toData(Object obj, PdxWriter pdxWriter) {

      return Optional.ofNullable(obj)
        .filter(it -> it instanceof Person)
        .map(it -> {
          pdxWriter.writeString("firstName", ((Person) it).getFirstName());
          pdxWriter.writeString("lastName", ((Person) it).getLastName());
          return true;
        })
        .orElse(false);
    }

    @Override
    public Object fromData(Class<?> type, PdxReader pdxReader) {

      return Optional.ofNullable(type)
        .filter(it -> Person.class.isAssignableFrom(type))
        .map(it -> Person.newPerson(pdxReader.readString("firstName"), pdxReader.readString("lastName")))
        .orElse(null);
    }
  }
}
