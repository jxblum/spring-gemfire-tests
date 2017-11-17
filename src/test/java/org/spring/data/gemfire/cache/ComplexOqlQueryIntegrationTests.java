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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.cache.query.Struct;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Integration tests testing Pivotal GemFire's OQL query capabilities on a complex object structure.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see lombok
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see <a href="https://stackoverflow.com/questions/47357875/gemfire-getting-query-values-from-complex-data">GemFire getting query values from complex data</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ComplexOqlQueryIntegrationTests {

  private static <T> T[] asArray(T... array) {
    return array;
  }

  @Autowired
  private GemfireTemplate workerTemplate;

  @Before
  public void setup() {

    CustomWorkLocation customWorkLocation =
      CustomWorkLocation.create(12345L, "11|123|54321");

    UserArea userArea = UserArea.create(customWorkLocation);

    Address address = Address.create("12345", "some name");

    WorkLocation workLocation = WorkLocation.create(userArea);

    workLocation.setAddress(address);
    workLocation.setLocationName("CA");

    MasterDeployment masterDeployment = MasterDeployment.create(asArray(workLocation));

    MasterPersonDossier masterPersonDossier = MasterPersonDossier.create(asArray(masterDeployment));

    HrMasterData masterData = HrMasterData.create(masterPersonDossier);

    this.workerTemplate.put(UUID.randomUUID().toString(), masterData);

    assertThat(this.workerTemplate.getRegion()).hasSize(1);
  }

  @Test
  public void runComplexQueryIsCorrect() {

    String query =
      "<TRACE> SELECT p.key, e.workspace"
      + " FROM /Worker.entrySet p, p.value.masterPersonDossier.masterDeployments[0].workLocations[0].userArea.customWorkLocation e";

    SelectResults<Object> queryResults = this.workerTemplate.find(query);

    assertThat(queryResults).isNotNull();

    System.err.printf("SELECT RESULTS [%s]%n", queryResults);

    List<Object> resultList = queryResults.asList();

    assertThat(resultList).isNotNull();
    assertThat(resultList).hasSize(1);

    System.err.printf("RESULT OBJECT TYPE [%s]%n", resultList.get(0).getClass().getName());

    Struct struct = (Struct) resultList.get(0);

    String key = String.valueOf(struct.get("key"));
    Object workspace = struct.get("workspace");

    assertThat(struct).isNotNull();
    assertThat(key).isNotNull();
    assertThat(workspace).isEqualTo(12345L);

    System.err.printf("WORKSPACE DETAILS { key = %1$s, workspace = %2$s }%n", key, workspace);

  }

  @PeerCacheApplication
  static class TestConfiguration {

    @Bean("Worker")
    public PartitionedRegionFactoryBean<Object, Object> workerRegion(GemFireCache gemfireCache) {

      PartitionedRegionFactoryBean<Object, Object> workerRegion = new PartitionedRegionFactoryBean<>();

      workerRegion.setCache(gemfireCache);
      workerRegion.setClose(false);
      workerRegion.setPersistent(false);

      return workerRegion;
    }

    @Bean
    public GemfireTemplate workerTemplate(GemFireCache gemfireCache) {
      return new GemfireTemplate(gemfireCache.getRegion("Worker"));
    }
  }

  @Data
  @RequiredArgsConstructor(staticName = "create")
  static class HrMasterData implements Serializable {

    @NonNull MasterPersonDossier masterPersonDossier;

  }

  @Data
  @RequiredArgsConstructor(staticName = "create")
  static class MasterPersonDossier implements Serializable {

    @NonNull MasterDeployment[] masterDeployments;

  }

  @Data
  @RequiredArgsConstructor(staticName = "create")
  static class MasterDeployment implements Serializable {

    @NonNull WorkLocation[] workLocations;

  }

  @Data
  @RequiredArgsConstructor(staticName = "create")
  static class WorkLocation implements Serializable {

    Address address;

    String locationName;

    @NonNull UserArea userArea;

  }

  @Data
  @RequiredArgsConstructor(staticName = "create")
  static class Address implements Serializable {

    @NonNull String addressLine;
    @NonNull String buildingName;

  }

  @Data
  @RequiredArgsConstructor(staticName = "create")
  static class UserArea implements Serializable {

    @NonNull CustomWorkLocation customWorkLocation;

  }

  @Data
  @RequiredArgsConstructor(staticName = "create")
  static class CustomWorkLocation implements Serializable {

    @NonNull Long workspace;
    @NonNull String workspaceId;

  }
}
