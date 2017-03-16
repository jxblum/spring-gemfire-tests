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

package org.pivotal.gemfire.cache;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.query.Index;
import org.apache.geode.cache.query.IndexExistsException;
import org.apache.geode.cache.query.IndexNameConflictException;
import org.apache.geode.cache.query.IndexType;
import org.apache.geode.cache.query.QueryService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The CacheRegionIndexDefinitionAndNameConflictsTest class is a test suite of test cases testing the contract
 * and functionality of GemFire's QueryService Index creation when Index Definitions or Names have been duplicated.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.query.Index
 * @see org.apache.geode.cache.query.QueryService
 * @since 1.0.0
 */
@SuppressWarnings({ "deprecation" })
public class CacheRegionIndexDefinitionAndNameConflictsTest {

  private static QueryService queryService;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  protected static void assertRegion(Region<?, ?> region, String expectedNamePath, DataPolicy expectedDataPolicy) {
    assertRegion(region, expectedNamePath, String.format("%1$s%2$s", Region.SEPARATOR, expectedNamePath),
      expectedDataPolicy);
  }

  protected static void assertRegion(Region<?, ?> region, String expectedName, String expectedPath,
    DataPolicy expectedDataPolicy)
  {
    assertThat(region, is(notNullValue()));
    assertThat(region.getName(), is(equalTo(expectedName)));
    assertThat(region.getFullPath(), is(equalTo(expectedPath)));
    assertThat(region.getAttributes(), is(notNullValue()));
    assertThat(region.getAttributes().getDataPolicy(), is(equalTo(expectedDataPolicy)));
  }

  @BeforeClass
  public static void setupGemFireServer() {
    Cache gemfireCache = new CacheFactory()
      .set("name", CacheRegionIndexDefinitionAndNameConflictsTest.class.getSimpleName())
      .set("log-level", "warning")
      .set("mcast-port", "0")
      .create();

    RegionFactory exampleRegionFactory = gemfireCache.createRegionFactory(RegionShortcut.REPLICATE);

    Region exampleRegion = exampleRegionFactory.create("Example");

    assertRegion(exampleRegion, "Example", DataPolicy.REPLICATE);

    queryService = gemfireCache.getQueryService();

    assertThat(queryService, is(notNullValue()));
    assertThat(queryService.getIndexes().isEmpty(), is(true));
  }

  @AfterClass
  public static void tearDownGemFire() {
    assertThat(queryService.getIndexes().size(), is(equalTo(2)));

    try {
      CacheFactory.getAnyInstance().close();
    }
    catch (Exception ignore) {
    }
  }

  protected void assertIndex(Index index, String expectedName, String expectedExpression, String expectedFromClause,
    IndexType expectedType)
  {
    assertThat(index, is(notNullValue()));
    assertThat(index.getName(), is(equalTo(expectedName)));
    assertThat(index.getIndexedExpression(), is(equalTo(expectedExpression)));
    assertThat(index.getFromClause(), is(equalTo(expectedFromClause)));
    assertThat(index.getType(), is(equalTo(expectedType)));
  }

  protected Index createIndex(String name, String expression, String fromClause, IndexType indexType) throws Exception {
    if (IndexType.PRIMARY_KEY.equals(indexType)) {
      return queryService.createKeyIndex(name, expression, fromClause);
    }
    else if (IndexType.HASH.equals(indexType)) {
      return queryService.createHashIndex(name, expression, fromClause);
    }
    else {
      return queryService.createKeyIndex(name, expression, fromClause);
    }
  }

  @Test
  public void indexesWithDuplicateDefinitionHandling() throws Exception {
    Index index = createIndex("exampleIdIdx", "id", "/Example", IndexType.PRIMARY_KEY);

    assertIndex(index, "exampleIdIdx", "id", "/Example", IndexType.PRIMARY_KEY);

    expectedException.expect(IndexExistsException.class);
    expectedException.expectCause(is(nullValue(Throwable.class)));
    expectedException.expectMessage("Similar Index Exists");

    index = createIndex("anotherExampleIdIdx", "id", "/Example", IndexType.PRIMARY_KEY);

    assertIndex(index, "anotherExampleIdIdx", "id", "/Example", IndexType.PRIMARY_KEY);
  }

  @Test
  public void indexesWithDuplicateNameHandling() throws Exception {
    String indexName = "exampleNameIdx";

    Index index = createIndex(indexName, "lastName", "/Example", IndexType.HASH);

    assertIndex(index, indexName, "lastName", "/Example", IndexType.HASH);

    expectedException.expect(IndexNameConflictException.class);
    expectedException.expectCause(is(nullValue(Throwable.class)));
    expectedException.expectMessage(String.format("Index named ' %1$s ' already exists.", indexName));

    index = createIndex(indexName, "firstName", "/Example", IndexType.FUNCTIONAL);

    assertIndex(index, indexName, "firstName", "/Example", IndexType.FUNCTIONAL);
  }

}
