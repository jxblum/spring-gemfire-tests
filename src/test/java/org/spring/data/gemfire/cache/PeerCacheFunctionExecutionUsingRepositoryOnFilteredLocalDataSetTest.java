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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

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
 * The PeerCacheFunctionExecutionUsingRepositoryOnFilteredLocalDataSetTest class is a test suite of test cases testing
 * the functionality of creating a Repository based on a local, filtered data set within the context
 * of a GemFire Function Execution.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
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
    Assert.state(programmersTemplate != null, "the GemfireTemplate for the '/Programmers' Region was not properly initialized");
    return programmersTemplate;
  }

  protected void assertReputation(final Iterable<Programmer> programmers, final int expectedReputation) {
    for (Programmer programmer : programmers) {
      assertEquals(expectedReputation, programmer.getReputation().intValue());
    }
  }

  protected Programmer createProgrammer(final String firstName, final String lastName, final String programmingLanguage) {
    Programmer programmer = new Programmer(firstName, lastName);
    programmer.setId(ID_SEQUENCE.incrementAndGet());
    programmer.setProgrammingLanguage(programmingLanguage);
    programmer.setReputation(DEFAULT_REPUTATION);
    return programmer;
  }

  protected Programmer save(final Programmer programmer) {
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
    save(createProgrammer("Jon", "Doe", "Java"));
    save(createProgrammer("Jane", "Doe", "Groovy"));
    save(createProgrammer("Jack", "Black", "Java"));
    save(createProgrammer("Cookie", "Doe", "JRuby"));
    save(createProgrammer("Jack", "Handy", "Java"));
    save(createProgrammer("Pie", "Doe", "Scala"));
    save(createProgrammer("Sandy", "Handy", "Java"));
    save(createProgrammer("Ima", "Pigg", "JavaScript"));
    save(createProgrammer("Sour", "Doe", "Java"));
    save(createProgrammer("Agent", "Smith", "Java"));
    save(createProgrammer("Ada", "Lovelace", "Java"));
    save(createProgrammer("Jason", "Bourne", "Java"));
  }

  @Test
  public void doFunctionExecution() {
    List<Programmer> programmers = getRepository().findDistinctByLastName("Doe");

    assertThat(programmers, is(notNullValue()));
    assertThat(programmers.isEmpty(), is(false));
    assertThat(programmers.size(), is(equalTo(5)));
    assertReputation(programmers, 5);

    programmers = getFunctionResults(getFunctions().updateReputation(toKeys(programmers), "Java", 10));

    Collections.sort(programmers);

    assertNotNull(programmers);
    assertFalse(programmers.isEmpty());
    assertEquals(String.format("Expected ([Jon Doe, Sour Doe]); but was (%1$s)", toNames(programmers)),
      2, programmers.size());
    assertTrue(toNames(programmers).containsAll(Arrays.asList("Jon Doe", "Sour Doe")));
    assertReputation(programmers, 15);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> getFunctionResults(final List<T> results) {
    if (!CollectionUtils.isEmpty(results)) {
      if (results.get(0) instanceof List) {
        assertEquals(1, results.size());
        return getFunctionResults((List<T>) results.get(0));
      }
    }

    return results;
  }

}
