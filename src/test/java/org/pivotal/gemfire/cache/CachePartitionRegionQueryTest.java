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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.query.Index;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxSerializable;
import com.gemstone.gemfire.pdx.PdxWriter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The CachePartitionRegionQueryTest class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class CachePartitionRegionQueryTest {

  private static final AtomicInteger expectedCount = new AtomicInteger(0);

  protected static final Boolean ENABLE_QUERY_DEBUGGING = true;

  private static Cache gemfireCache;

  private static Session jonDoeOne;
  private static Session jonDoeTwo;
  private static Session jonDoeThree;
  private static Session jonDoeFour;
  private static Session jonDoeFive;

  protected static final String COUNT_REGION_ENTRIES_QUERY = "SELECT count(*) FROM %1$s";
  protected static final String FIND_BY_PRINCIPAL_NAME_QUERY = "SELECT s FROM %1$s s WHERE s.principalName = $1";
  protected static final String LOG_LEVEL = "config";
  protected static final String SESSION_REGION_NAME = "Sessions";

  protected static <T> T log(String message, T value) {
    System.out.println(String.format(message, value));
    return value;
  }

  protected static String toRegionFullPath(Region<?, ?> region) {
    return toRegionFullPath(region.getName());
  }

  protected static String toRegionFullPath(String regionName) {
    return String.format("%1$s%2$s", Region.SEPARATOR, regionName);
  }

  @BeforeClass
  public static void setupGemFirePeerCache() throws Exception {
    System.setProperty("gemfire.Query.VERBOSE", ENABLE_QUERY_DEBUGGING.toString());

    gemfireCache = new CacheFactory()
      .set("name", CachePartitionRegionQueryTest.class.getName())
      .set("mcast-port", "0")
      .set("log-level", LOG_LEVEL)
      .create();

    assertThat(gemfireCache, is(notNullValue()));

    RegionFactory<String, Session> sessionRegionFactory = gemfireCache.createRegionFactory(RegionShortcut.PARTITION);

    sessionRegionFactory.setKeyConstraint(String.class);
    sessionRegionFactory.setValueConstraint(Session.class);
    sessionRegionFactory.create(SESSION_REGION_NAME);

    Region<String, Session> sessionRegion = gemfireCache.getRegion(SESSION_REGION_NAME);

    assertThat(sessionRegion, is(notNullValue()));
    assertThat(sessionRegion.getName(), is(equalTo(SESSION_REGION_NAME)));
    assertThat(sessionRegion.getFullPath(), is(equalTo(toRegionFullPath(SESSION_REGION_NAME))));
    assertThat(sessionRegion.getAttributes(), is(notNullValue()));
    assertThat(sessionRegion.getAttributes().getDataPolicy(), is(equalTo(DataPolicy.PARTITION)));

    QueryService queryService = gemfireCache.getQueryService();

    Index principalNameIdx = queryService.createHashIndex("principalNameIdx", "principalName", toRegionFullPath(sessionRegion));

    assertThat(principalNameIdx, is(notNullValue()));
    assertThat(principalNameIdx.getIndexedExpression(), is(equalTo("principalName")));
    assertThat(principalNameIdx.getName(), is(equalTo("principalNameIdx")));
    assertThat(principalNameIdx.getRegion(), is(equalTo(sessionRegion)));

    setupSessionData();
  }

  private static void setupSessionData() {
    jonDoeOne = save(touch(createSession("jonDoe")));
    save(touch(createSession("janeDoe")));
    jonDoeTwo = save(touch(createSession("jonDoe")));
    save(touch(createSession("cookieDoe")));
    save(touch(createSession("froDoe")));
    jonDoeThree = save(touch(createSession("jonDoe")));
    save(touch(createSession("pieDoe")));
    save(touch(createSession("joeDoe")));
    jonDoeFour = save(touch(createSession("jonDoe")));
    save(touch(createSession("ryeDoe")));
    save(touch(createSession("johnDoe")));
    jonDoeFive = save(touch(createSession("jonDoe")));
    save(touch(createSession("sourDoe")));
  }

  @AfterClass
  public static void tearDownGemFirePeerCache() {
    if (gemfireCache != null) {
      gemfireCache.close();
    }
  }

  protected static Session createSession() {
    return new Session();
  }

  protected static Session createSession(String principalName) {
    Session session = createSession();
    session.setPrincipalName(principalName);
    return session;
  }

  protected static Session save(Session session) {
    gemfireCache.getRegion(SESSION_REGION_NAME).put(session.getId(), session);
    assertThat(gemfireCache.getRegion(SESSION_REGION_NAME).get(session.getId()), is(equalTo(session)));
    expectedCount.incrementAndGet();
    return session;
  }

  protected static Session touch(Session session) {
    session.setLastAccessedTimeToNow();
    return session;
  }

  @Test
  public void countRegionEntries() {
    assertThat(doCountRegionEntries(), is(equalTo(log("Expected Region Count is (%1$d)%n", expectedCount.get()))));
  }

  protected int doCountRegionEntries() {
    return this.<Integer>executeQuery(COUNT_REGION_ENTRIES_QUERY).asList().get(0);
  }

  @Test
  public void findSessionsByPrincipalNameUsingQuery() {
    Map<String, Session> jonDoeSessions = log(doFindByPrincipalName("jonDoe"));

    assertThat(jonDoeSessions, is(notNullValue()));
    assertThat(jonDoeSessions.size(), is(equalTo(5)));
    assertThat(jonDoeSessions.get(jonDoeOne.getId()), is(equalTo(jonDoeOne)));
    assertThat(jonDoeSessions.get(jonDoeTwo.getId()), is(equalTo(jonDoeTwo)));
    assertThat(jonDoeSessions.get(jonDoeThree.getId()), is(equalTo(jonDoeThree)));
    assertThat(jonDoeSessions.get(jonDoeFour.getId()), is(equalTo(jonDoeFour)));
    assertThat(jonDoeSessions.get(jonDoeFive.getId()), is(equalTo(jonDoeFive)));
  }

  protected Map<String, Session> doFindByPrincipalName(String principalName) {
    SelectResults<Session> results = executeQuery(FIND_BY_PRINCIPAL_NAME_QUERY, principalName);

    Map<String, Session> sessions = new HashMap<>(results.size());

    for (Session session : results.asList()) {
      sessions.put(session.getId(), session);
    }

    return sessions;
  }

  protected <K, V extends Comparable<V>> Map<K, V> log(Map<K, V> map) {
    if (map != null) {
      List<V> mapValues = new ArrayList<>(map.values());
      Collections.sort(mapValues);
      System.out.printf("Map values include (%1$s)%n", toString(mapValues));
    }

    return map;
  }

  protected String toString(Iterable<?> collection) {
    StringBuilder buffer = new StringBuilder("[");

    int count = 0;

    if (collection != null) {
      for (Object element : collection) {
        buffer.append(count++ > 0 ? ",\n" : "\n").append(String.format("\t%1$s", element));
      }
    }

    buffer.append("\n]");

    return buffer.toString();
  }

  @SuppressWarnings("unchecked")
  protected <T> SelectResults<T> executeQuery(String queryString , Object... args) {
    try {
      Region<String, T> sessionRegion = gemfireCache.getRegion(SESSION_REGION_NAME);

      assertThat(sessionRegion, is(notNullValue()));

      QueryService queryService = sessionRegion.getRegionService().getQueryService();

      Query query = queryService.newQuery(String.format(queryString, toRegionFullPath(sessionRegion)));

      return (SelectResults<T>) query.execute(args);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  public static final class Session implements Comparable<Session>, PdxSerializable {

    protected static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-dd-MM-hh-mm-ss.S");

    protected static final String PRINCIPAL_NAME_ATTRIBUTE_NAME = Session.class.getName() + ".PRINCIPAL_NAME";

    private int maxInactiveIntervalInSeconds;

    private long creationTime;
    private long lastAccessedTime;

    private final Map<String, Object> attributes = new HashMap<>();

    public String id;

    public Session() {
      this(UUID.randomUUID().toString());
    }

    public Session(String id) {
      this.id = String.valueOf(assertValidId(id));
      this.creationTime = System.currentTimeMillis();
    }

    private Object assertValidId(Object id) {
      if (isInvalidId(id)) {
        throw new IllegalArgumentException(String.format("(%1$s) is not valid", id));
      }

      return id;
    }

    private boolean isInvalidId(Object id) {
      return (id == null || id.toString().trim().isEmpty());
    }

    public String getId() {
      return id;
    }

    public void setAttribute(String name, Object value) {
      if (value == null) {
        removeAttribute(name);
      }
      else {
        attributes.put(name, value);
      }
    }

    public void removeAttribute(String name) {
      attributes.remove(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
      return (T) attributes.get(name);
    }

    public Set<String> getAttributeNames() {
      return Collections.unmodifiableSet(attributes.keySet());
    }

    public long getCreationTime() {
      return creationTime;
    }

    public void setLastAccessedTimeToNow() {
      setLastAccessedTime(System.currentTimeMillis());
    }

    public void setLastAccessedTime(long lastAccessedTime) {
      this.lastAccessedTime = lastAccessedTime;
    }

    public long getLastAccessedTime() {
      return Math.max(lastAccessedTime, 0);
    }

    public void setMaxInactiveIntervalInSeconds(int maxInactiveIntervalInSeconds) {
      this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
    }

    public int getMaxInactiveIntervalInSeconds() {
      return maxInactiveIntervalInSeconds;
    }

    public void setPrincipalName(String principalName) {
      setAttribute(PRINCIPAL_NAME_ATTRIBUTE_NAME, principalName);
    }

    public String getPrincipalName() {
      return getAttribute(PRINCIPAL_NAME_ATTRIBUTE_NAME);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fromData(final PdxReader reader) {
      id = reader.readString("id");
      creationTime = reader.readLong("creationTime");
      lastAccessedTime = reader.readLong("lastAccessedTime");
      maxInactiveIntervalInSeconds = reader.readInt("maxInactiveIntervalInSeconds");
      setPrincipalName(reader.readString("principalName"));

      Set<String> attributeNames = nullSafeSet((Set<String>) reader.readObject("attributeNames"));

      for (String attributeName : attributeNames) {
        setAttribute(attributeName, reader.readObject(attributeName));
      }
    }

    @Override
    public void toData(final PdxWriter writer) {
      writer.writeString("id", getId());
      writer.writeLong("creationTime", getCreationTime());
      writer.writeLong("lastAccessedTime", getLastAccessedTime());
      writer.writeInt("maxInactiveIntervalInSeconds", getMaxInactiveIntervalInSeconds());
      writer.writeString("principalName", getPrincipalName());

      Set<String> attributeNames = new HashSet<>(nullSafeSet(getAttributeNames()));

      writer.writeObject("attributeNames", attributeNames);

      for (String attributeName : attributeNames) {
        writer.writeObject(attributeName, getAttribute(attributeName));
      }
    }

    protected <T> Set<T> nullSafeSet(Set<T> set) {
      return (set != null ? set : Collections.<T>emptySet());
    }

    public boolean isExpired() {
      final long lastAccessedTime = getLastAccessedTime();
      final long maxInactiveIntervalInSeconds = getMaxInactiveIntervalInSeconds();

      return (maxInactiveIntervalInSeconds >= 0
        && (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(maxInactiveIntervalInSeconds) >= lastAccessedTime));
    }

    @Override
    @SuppressWarnings("all")
    public int compareTo(Session session) {
      return Long.valueOf(getCreationTime()).compareTo(session.getCreationTime());
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof Session)) {
        return false;
      }

      Session that = (Session) obj;

      return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + getId().hashCode();
      return hashValue;
    }

    @Override
    public String toString() {
      return String.format("{ @type = %1$s, id = %2$s, creationTime = %3$s, lastAccessedTime = %4$s, maxInactiveIntervalInSeconds = %5$d, principalName = %6$s }",
        getClass().getName(), getId(), toString(getCreationTime()), toString(getLastAccessedTime()), getMaxInactiveIntervalInSeconds(), getPrincipalName());
    }

    protected String toString(final long timestamp) {
      return TIMESTAMP_FORMAT.format(new Date(timestamp));
    }
  }

}
