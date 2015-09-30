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

package org.spring.data.gemfire.app.dao.repo.support;

import java.util.List;

import org.spring.data.gemfire.app.beans.Programmer;
import org.spring.data.gemfire.app.dao.repo.CustomProgrammerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.util.Assert;

/**
 * The ProgrammerRepositoryImpl class is an implementation of the CustomProgrammerRepository interface.
 *
 * @author John Blum
 * @see org.spring.data.gemfire.app.dao.repo.CustomProgrammerRepository
 * @link http://en.wikipedia.org/wiki/List_of_JVM_languages#JVM_languages
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ProgrammerRepositoryImpl implements CustomProgrammerRepository {

  @Autowired
  private GemfireTemplate programmersTemplate;

  protected GemfireTemplate getTemplate() {
    Assert.state(programmersTemplate != null,
      "the GemfireTemplate for the '/Programmers' Region was not properly initialized");
    return programmersTemplate;
  }

  @Override
  public List<Programmer> findByJvmBasedLanguages() {
    return getTemplate().<Programmer>query(
      "programmingLanguage IN SET ('Java', 'Clojure', 'Groovy', 'JRuby', 'Jython', 'Scala')")
        .asList();
  }

}
