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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.app.beans.Programmer;
import org.spring.data.gemfire.app.dao.repo.ProgrammerRepository;
import org.spring.data.gemfire.cache.execute.ProgrammerFunctionExecutions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Test suite of test cases testing the functionality of creating and using a
 * {@link org.springframework.data.repository.Repository} based on a local, filtered data set within the context
 * of a GemFire Function Execution.
 *
 * @author John Blum
 * @see org.junit.Test
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class PeerCacheFunctionExecutionUsingRepositoryOnFilteredLocalDataSetTest {

  protected static final AtomicLong ID_SEQUENCE = new AtomicLong(0l);

  protected static final Integer DEFAULT_REPUTATION = 5;

  @Autowired
  private GemfireTemplate programmersTemplate;

  @Autowired
  private ProgrammerFunctionExecutions programmerFunctions;

  @Autowired
  private ProgrammerRepository programmerRepository;

  protected ProgrammerFunctionExecutions getFunctions() {
    Assert.state(programmerFunctions != null, "ProgrammerFunctionExecutions was not properly initialized");
    return programmerFunctions;
  }

  protected ProgrammerRepository getRepository() {
    Assert.state(programmerRepository != null, "ProgrammerRepository was not properly initialized");
    return programmerRepository;
  }

  protected GemfireTemplate getTemplate() {
    Assert.state(programmersTemplate != null, "GemfireTemplate for the '/Programmers' Region was not properly initialized");
    return programmersTemplate;
  }

  protected void assertReputation(Iterable<Programmer> programmers, int expectedReputation) {
    for (Programmer programmer : programmers) {
      assertThat(programmer.getReputation().intValue(), is(equalTo(expectedReputation)));
    }
  }

  protected Programmer newProgrammer(String firstName, String lastName, String programmingLanguage) {
    Programmer programmer = new Programmer(firstName, lastName);
    programmer.setId(ID_SEQUENCE.incrementAndGet());
    programmer.setProgrammingLanguage(programmingLanguage);
    programmer.setReputation(DEFAULT_REPUTATION);
    return programmer;
  }

  protected Programmer save(Programmer programmer) {
    getTemplate().put(programmer.getId(), programmer);
    return getTemplate().get(programmer.getId());
  }

  protected Set<Long> toKeys(Iterable<Programmer> programmers) {
    Set<Long> keys = new HashSet<>();

    for (Programmer programmer : programmers) {
      keys.add(programmer.getId());
    }

    return keys;
  }

  protected List<String> toNames(Iterable<Programmer> programmers) {
    List<String> names = new ArrayList<>();

    for (Programmer programmer : programmers) {
      names.add(programmer.getName());
    }

    return names;
  }

  @Before
  public void setup() {
    save(newProgrammer("Jon", "Doe", "Java"));
    save(newProgrammer("Jane", "Doe", "Groovy"));
    save(newProgrammer("Jack", "Black", "Java"));
    save(newProgrammer("Cookie", "Doe", "JRuby"));
    save(newProgrammer("Jack", "Handy", "Java"));
    save(newProgrammer("Pie", "Doe", "Scala"));
    save(newProgrammer("Sandy", "Handy", "Java"));
    save(newProgrammer("Ima", "Pigg", "JavaScript"));
    save(newProgrammer("Sour", "Doe", "Java"));
    save(newProgrammer("Agent", "Smith", "Java"));
    save(newProgrammer("Ada", "Lovelace", "Java"));
    save(newProgrammer("Jason", "Bourne", "Java"));
  }

  @Test
  public void doFunctionExecution() {
    List<Programmer> programmers = getRepository().findDistinctByLastName("Doe");

    assertThat(programmers, is(notNullValue()));
    assertThat(programmers.size(), is(equalTo(5)));
    assertReputation(programmers, 5);

    programmers = getFunctionResults(getFunctions().updateReputation(toKeys(programmers), "Java", 10));

    Collections.sort(programmers);

    assertThat(programmers, is(notNullValue(List.class)));
    assertThat(String.format("Expected ([Jon Doe, Sour Doe]); but was (%s)", toNames(programmers)),
      programmers.size(), is(equalTo(2)));
    assertThat(toNames(programmers).containsAll(Arrays.asList("Jon Doe", "Sour Doe")), is(true));
    assertReputation(programmers, 15);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> getFunctionResults(List<T> results) {
    if (!CollectionUtils.isEmpty(results)) {
      if (results.get(0) instanceof List) {
        assertThat(results.size(), is(equalTo(1)));
        return getFunctionResults((List<T>) results.get(0));
      }
    }

    return results;
  }
}
