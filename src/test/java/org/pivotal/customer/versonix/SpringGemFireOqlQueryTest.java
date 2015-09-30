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

package org.pivotal.customer.versonix;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.query.SelectResults;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pivotal.customer.versonix.support.PaxReserveType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The SpringGemFireOqlQueryTest class is a test suite of test case testing the contract and functionality of GemFire's
 * OQL Querying using Spring Data GemFire's GemfireTemplate class.
 *
 * @author jblum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see com.gemstone.gemfire.cache.Region
 * @see org.pivotal.customer.versonix.support.PaxReserveType
 * @since 1.5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("gf-server-context.xml")
@SuppressWarnings("unused")
public class SpringGemFireOqlQueryTest {

  private final AtomicLong idSequence = new AtomicLong(0l);

  @Autowired
  private GemfireTemplate paxReserveTypesTemplate;

  private PaxReserveType one;
  private PaxReserveType two;
  private PaxReserveType three;

  @Resource(name = "paxReserveTypes")
  private Region<String, PaxReserveType> paxReserveTypesRegion;

  protected PaxReserveType newPaxReserveType() {
    return newPaxReserveType(String.valueOf(idSequence.incrementAndGet()));
  }

  protected PaxReserveType newPaxReserveType(final String id) {
    return newPaxReserveType(id, false);
  }

  protected PaxReserveType newPaxReserveType(final String id, final boolean linked) {
    PaxReserveType instance = new PaxReserveType();
    instance.setId(id);
    instance.setLinked(linked);
    return instance;
  }

  protected PaxReserveType put(final PaxReserveType paxReserveType) {
    return put(paxReserveType.getId(), paxReserveType);
  }

  protected PaxReserveType put(final String key, final PaxReserveType paxReserveType) {
    paxReserveTypesTemplate.put(key, paxReserveType);
    return paxReserveType;
  }

  @Before
  public void setup() {
    one = put(newPaxReserveType());
    two = put(newPaxReserveType());
    three = put(newPaxReserveType());
    assertEquals(3, paxReserveTypesRegion.size());
  }

  @After
  public void tearDown() {
    idSequence.set(0l);
    one = two = three = null;
    paxReserveTypesRegion.clear();
  }

  protected void assertSelectResults(final SelectResults<PaxReserveType> actualResults, final PaxReserveType... expectedResults) {
    assertNotNull(actualResults);
    assertFalse(actualResults.isEmpty());
    assertEquals(expectedResults.length, actualResults.size());

    List<PaxReserveType> resultList = actualResults.asList();

    assertNotNull(resultList);
    assertTrue(resultList.containsAll(Arrays.asList(expectedResults)));
  }

  @Test
  public void testGemfireTemplateQuery() {
    SelectResults<PaxReserveType> results = paxReserveTypesTemplate.query("1 = 1");
    assertSelectResults(results, one, two, three);
  }

  @Test
  public void testGemfireTemplateFind() {
    SelectResults<PaxReserveType> results = paxReserveTypesTemplate.find("SELECT * FROM /paxReserveTypes");
    assertSelectResults(results, one, two, three);
  }

  @Test
  public void testPaxReservesTypesWithDuplicateIdsDifferentKeys() {
    PaxReserveType four = put("4", newPaxReserveType("2", true));

    assertEquals(4, paxReserveTypesRegion.size());

    SelectResults<PaxReserveType> results = paxReserveTypesTemplate.query("1 = 1");

    assertSelectResults(results, one, two, three, four);

    results = paxReserveTypesTemplate.find("SELECT * FROM /paxReserveTypes");

    assertSelectResults(results, one, two, three, four);

    results = paxReserveTypesTemplate.query("id = '2'");

    assertSelectResults(results, two, four);

    results = paxReserveTypesTemplate.find("SELECT * FROM /paxReserveTypes WHERE id = $1", "2");

    assertSelectResults(results, two, four);
  }

}
