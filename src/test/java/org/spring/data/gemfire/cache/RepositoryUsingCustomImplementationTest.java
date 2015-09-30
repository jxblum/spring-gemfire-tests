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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.app.beans.Programmer;
import org.spring.data.gemfire.app.dao.repo.ProgrammerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * The RepositoryUsingCustomImplementationTest class...
 * @author John Blum
 * @see org.springframework.data.gemfire.
 * @since 1.7.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class RepositoryUsingCustomImplementationTest {

  protected static final AtomicLong ID_SEQUENCE = new AtomicLong(0l);

  @Autowired
  private GemfireTemplate programmersTemplate;

  @Autowired
  private ProgrammerRepository programmerRepository;

  protected GemfireTemplate getTemplate() {
    Assert.state(programmersTemplate != null,
      "the GemfireTemplate for the '/Programmers' Region was not properly initialized");
    return programmersTemplate;
  }

  protected Programmer createProgrammer(final String firstName, final String lastName, final String programmingLanguage) {
    Programmer programmer = new Programmer(firstName, lastName);
    programmer.setId(ID_SEQUENCE.incrementAndGet());
    programmer.setProgrammingLanguage(programmingLanguage);
    return programmer;
  }

  protected Programmer save(final Programmer programmer) {
    getTemplate().put(programmer.getId(), programmer);
    return getTemplate().get(programmer.getId());
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
    save(createProgrammer("Jack", "Black", "C#"));
    save(createProgrammer("Cookie", "Doe", "JRuby"));
    save(createProgrammer("Jack", "Handy", "C"));
    save(createProgrammer("Pie", "Doe", "Scala"));
    save(createProgrammer("Sandy", "Handy", "C++"));
    save(createProgrammer("Ima", "Pigg", "JavaScript"));
    save(createProgrammer("Sour", "Doe", "Java"));
    save(createProgrammer("Agent", "Smith", "Assembly"));
    save(createProgrammer("Ada", "Lovelace", "Assembly"));
    save(createProgrammer("Jason", "Bourne", "Ruby"));
  }

  @Test
  public void testCustomImplementation() {
    List<Programmer> programmers = programmerRepository.findDistinctByProgrammingLanguageLikeOrderByNameAsc("Java");

    assertNotNull(programmers);
    assertFalse(programmers.isEmpty());
    assertEquals(2, programmers.size());
    assertTrue(String.format("Expected (%1$s); but was (%2$s)!", "[Jon Doe, Sour Doe]", toNames(programmers)),
      toNames(programmers).containsAll(Arrays.asList("Jon Doe", "Sour Doe")));

    programmers = programmerRepository.findByJvmBasedLanguages();

    assertNotNull(programmers);
    assertFalse(programmers.isEmpty());
    assertEquals(5, programmers.size());
    assertTrue(String.format("Expected (%1$s); but was (%2$s)!", "[Jon Doe, Jane Doe, Cookie Doe, Pie Doe, Sour Doe]",
      toNames(programmers)), toNames(programmers).containsAll(
        Arrays.asList("Jon Doe", "Jane Doe", "Cookie Doe", "Pie Doe", "Sour Doe")));
  }

}
