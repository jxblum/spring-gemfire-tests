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

package org.spring.data.gemfire.cache.execute;

import java.util.Collections;
import java.util.List;

import org.apache.geode.cache.Region;
import org.spring.data.gemfire.app.beans.Programmer;
import org.spring.data.gemfire.app.dao.repo.ProgrammerRepository;
import org.spring.data.gemfire.app.dao.repo.support.ProgrammerRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.data.gemfire.function.annotation.RegionData;
import org.springframework.data.gemfire.mapping.GemfireMappingContext;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * The ProgrammerFunctions class is a POJO containing a set of GemFire Functions for operating on Programmers.
 *
 * @author John Blum
 * @see org.spring.data.gemfire.app.beans.Programmer
 * @see org.spring.data.gemfire.app.dao.repo.ProgrammerRepository
 * @see org.spring.data.gemfire.app.dao.repo.support.ProgrammerRepositoryImpl
 * @see org.springframework.data.gemfire.function.annotation.GemfireFunction
 * @see org.springframework.data.gemfire.repository.support.GemfireRepositoryFactory
 * @see org.springframework.stereotype.Component
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.execute.FunctionContext
 * @see org.apache.geode.cache.execute.RegionFunctionContext
 * @since 1.0.0
 */
@Component
@SuppressWarnings("unused")
public class ProgrammerFunctions {

  @Autowired
  private GemfireTemplate programmersTemplate;

  @Autowired
  private ProgrammerRepository programmerRepository;

  protected static <T> List<T> nullSafeList(List<T> list) {
    return (list != null ? list : Collections.<T>emptyList());
  }

  protected ProgrammerRepository getProgrammerRepository() {
    Assert.state(programmerRepository != null, "'programmerRepository' was not properly initialized");
    return programmerRepository;
  }

  protected ProgrammerRepository getProgrammerRepository(Region<Long, Programmer> programmers) {
    if (!programmersTemplate.getRegion().equals(programmers)) {
      GemfireRepositoryFactory repositoryFactory = new GemfireRepositoryFactory(
        Collections.singleton(programmers), new GemfireMappingContext());

      return repositoryFactory.getRepository(ProgrammerRepository.class, new ProgrammerRepositoryImpl());
    }

    return getProgrammerRepository();
  }

  @GemfireFunction
  public List<Programmer> updateReputation(@RegionData Region<Long, Programmer> filteredProgrammers,
      String programmingLanguage, int reputationDelta) {

    try {
      ProgrammerRepository localProgrammerRepository = getProgrammerRepository(filteredProgrammers);

      //List<Programmer> programmers = nullSafeList(
      //  localProgrammerRepository.findDistinctByProgrammingLanguageOrderByNameAsc(programmingLanguage));

      List<Programmer> programmers = nullSafeList(filteredProgrammers.<Programmer>query(
        String.format("programmingLanguage = '%s'", programmingLanguage))
          .asList());

      if (!CollectionUtils.isEmpty(programmers)) {
        for (Programmer programmer : programmers) {
          programmer.setReputation(programmer.getReputation() + reputationDelta);
        }

        localProgrammerRepository.save(programmers);
      }

      return programmers;
    }
    catch (Exception e) {
      throw new RuntimeException("?", e);
    }
  }
}
