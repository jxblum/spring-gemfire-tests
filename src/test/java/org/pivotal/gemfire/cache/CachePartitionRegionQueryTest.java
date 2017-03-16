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
import static org.hamcrest.number.OrderingComparisons.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

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

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.query.Index;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The CachePartitionRegionQueryTest class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class CachePartitionRegionQueryTest {

  private static final AtomicInteger expectedCount = new AtomicInteger(0);

  protected static final Boolean ENABLE_QUERY_DEBUGGING = true;

  private static Cache gemfireCache;

  private static Object cookieDoe;
  private static Object froDoe;
  private static Object janeDoe;
  private static Object joeDoe;
  private static Object johnDoe;
  private static Object jonDoeOne;
  private static Object jonDoeTwo;
  private static Object jonDoeThree;
  private static Object jonDoeFour;
  private static Object jonDoeFive;
  private static Object pieDoe;
  private static Object ryeDoe;
  private static Object sourDoe;

  protected static final String COUNT_REGION_ENTRIES_QUERY = "SELECT count(*) FROM %1$s";
  protected static final String FIND_BY_CUSTOM_SESSION_ATTRIBUTE = "SELECT s FROM %1$s s WHERE s.%2$s = $1";
  protected static final String FIND_BY_INDEXED_SESSION_ATTRIBUTE = "SELECT s FROM %1$s s WHERE s.attributes['%2$s'] = $1";
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
      .setPdxReadSerialized(true)
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

    Index principalNameIndex = queryService.createHashIndex("principalNameIndex", "principalName", toRegionFullPath(sessionRegion));

    assertThat(principalNameIndex, is(notNullValue()));
    assertThat(principalNameIndex.getIndexedExpression(), is(equalTo("principalName")));
    assertThat(principalNameIndex.getName(), is(equalTo("principalNameIndex")));
    assertThat(principalNameIndex.getRegion(), is(equalTo(sessionRegion)));

    Index sessionAttributesIndex = queryService.createIndex("sessionAttributesIndex", "s.attributes[*]",
      String.format("%1$s s", toRegionFullPath(sessionRegion)));

    assertThat(sessionAttributesIndex, is(notNullValue()));
    assertThat(sessionAttributesIndex.getIndexedExpression(), is(equalTo("s.attributes[*]")));
    assertThat(sessionAttributesIndex.getName(), is(equalTo("sessionAttributesIndex")));
    assertThat(sessionAttributesIndex.getRegion(), is(equalTo(sessionRegion)));

    setupSessionData();
  }

  private static void setupSessionData() {
    jonDoeOne = save(touch(setAttribute(createSession("jonDoe"), "custom", "1")));
    janeDoe = save(touch(setAttribute(createSession("janeDoe"), "custom", "2")));
    jonDoeTwo = save(touch(setAttribute(createSession("jonDoe"), "custom", "3")));
    cookieDoe = save(touch(setAttribute(createSession("cookieDoe"), "custom", "4")));
    froDoe = save(touch(setAttribute(createSession("froDoe"), "custom", "5")));
    jonDoeThree = save(touch(setAttribute(createSession("jonDoe"), "custom", "6")));
    pieDoe = save(touch(setAttribute(createSession("pieDoe"), "custom", "7")));
    joeDoe = save(touch(setAttribute(createSession("joeDoe"), "custom", "8")));
    jonDoeFour = save(touch(setAttribute(createSession("jonDoe"), "custom", "9")));
    ryeDoe = save(touch(setAttribute(createSession("ryeDoe"), "custom", "10")));
    johnDoe = save(touch(setAttribute(createSession("johnDoe"), "custom", "11")));
    jonDoeFive = save(touch(setAttribute(createSession("jonDoe"), "custom", "12")));
    sourDoe = save(touch(setAttribute(createSession("sourDoe"), "custom", "13")));
  }

  @AfterClass
  public static void tearDownGemFirePeerCache() {
    if (gemfireCache != null) {
      gemfireCache.close();
    }
  }

  protected void assertSession(Object session, String principalName, Object customAttributeValue) {
    if (session instanceof Session) {
      assertSession((Session) session, principalName, customAttributeValue);
    }
    else if (session instanceof PdxInstance) {
      assertSession((PdxInstance) session, principalName, customAttributeValue);
    }
    else {
      throw new AssertionError(String.format("(%1$s) is not an instance of (%2$s) or (%3$s)",
        session, Session.class.getName(), PdxInstance.class.getName()));
    }
  }

  protected void assertSession(PdxInstance session, String principalName, Object customAttributeValue) {
    assertThat(session, is(notNullValue()));
    assertThat(String.valueOf(session.getField("id")), is(notNullValue()));
    assertThat(Long.valueOf(String.valueOf(session.getField("creationTime"))),
      is(lessThanOrEqualTo(System.currentTimeMillis())));
    assertThat(String.valueOf(session.getField("principalName")), is(equalTo(principalName)));
    assertThat(session.getField("custom"), is(equalTo(customAttributeValue)));
  }

  protected void assertSession(Session actualSession, String principalName, Object customAttributeValue) {
    assertThat(actualSession, is(notNullValue()));
    assertThat(actualSession.getId(), is(notNullValue()));
    assertThat(actualSession.getCreationTime(), is(lessThanOrEqualTo(System.currentTimeMillis())));
    assertThat(actualSession.getPrincipalName(), is(equalTo(principalName)));
    assertThat(actualSession.getAttribute("custom"), is(equalTo(customAttributeValue)));
  }

  protected static Session createSession() {
    return new Session();
  }

  protected static Session createSession(String principalName) {
    Session session = createSession();
    session.setPrincipalName(principalName);
    return session;
  }

  protected static Session setAttribute(Session session, String name, Object value) {
    session.setAttribute(name, value);
    return session;
  }

  @SuppressWarnings("unused")
  protected static Session setAttributes(Session session, Map<String, Object> attributes) {
    if (attributes != null) {
      for (Map.Entry<String, Object> entry : attributes.entrySet()) {
        setAttribute(session, entry.getKey(), entry.getValue());
      }
    }

    return session;
  }

  protected static String getSessionId(Object session) {
    return (session instanceof Session ? ((Session) session).getId() : (session instanceof PdxInstance ?
      String.valueOf(((PdxInstance) session).getField("id")) : null));
  }

  protected static Session save(Session session) {
    gemfireCache.getRegion(SESSION_REGION_NAME).put(session.getId(), session);
    assertThat(getSessionId(gemfireCache.getRegion(SESSION_REGION_NAME).get(session.getId())),
      is(equalTo(session.getId())));
    expectedCount.incrementAndGet();
    return session;
  }

  protected static Session touch(Session session) {
    session.setLastAccessedTimeToNow();
    return session;
  }

  protected static Session toSession(Object session) {
    return (session instanceof Session ? (Session) session : (session instanceof PdxInstance
      ? Session.from((PdxInstance) session) : null));
  }

  @Test
  public void countRegionEntries() {
    assertThat(doCountRegionEntries(), is(equalTo(log("Expected Region Count is (%1$d)%n", expectedCount.get()))));
  }

  protected int doCountRegionEntries() {
    return this.<Integer>executeQuery(COUNT_REGION_ENTRIES_QUERY).asList().get(0);
  }

  @Test
  public void findSessionsbyCustomSessionAttributeUsingIndexedMapKeyBasedPredicate() {
    Object johnDoe = gemfireCache.<String, Session>getRegion(SESSION_REGION_NAME).get(
      getSessionId(CachePartitionRegionQueryTest.johnDoe));

    assertSession(johnDoe, "johnDoe", "11");

    johnDoe = doFindByIndexedSessionAttribute("custom", "11");

    assertSession(johnDoe, "johnDoe", "11");
  }

  protected Session doFindByIndexedSessionAttribute(String attributeName, Object attributeValue) {
    String queryString = String.format(FIND_BY_INDEXED_SESSION_ATTRIBUTE, "%1$s", attributeName);

    SelectResults<Object> results = executeQuery(queryString, attributeValue);

    return (results.size() > 0 ? toSession(results.asList().get(0)) : null);
  }

  @Test
  public void findSessionsByCustomSessionAttributeUsingPropertyBasedPredicate() {
    Object froDoe = gemfireCache.<String, Session>getRegion(SESSION_REGION_NAME).get(
      getSessionId(CachePartitionRegionQueryTest.froDoe));

    assertSession(froDoe, "froDoe", "5");

    froDoe = doFindByCustomSessionAttribute("custom", "5");

    assertSession(froDoe, "froDoe", "5");
  }

  protected Session doFindByCustomSessionAttribute(String attributeName, Object attributeValue) {
    String queryString = String.format(FIND_BY_CUSTOM_SESSION_ATTRIBUTE, "%1$s", attributeName);

    SelectResults<Object> results = executeQuery(queryString, attributeValue);

    return (results.size() > 0 ? toSession(results.asList().get(0)) : null);
  }

  @Test
  public void findSessionsByPrincipalNameUsingQuery() {
    Map<String, Session> jonDoeSessions = log(doFindByPrincipalName("jonDoe"));

    assertThat(jonDoeSessions, is(notNullValue()));
    assertThat(jonDoeSessions.size(), is(equalTo(5)));
    assertThat(jonDoeSessions.get(getSessionId(jonDoeOne)), is(equalTo(jonDoeOne)));
    assertThat(jonDoeSessions.get(getSessionId(jonDoeTwo)), is(equalTo(jonDoeTwo)));
    assertThat(jonDoeSessions.get(getSessionId(jonDoeThree)), is(equalTo(jonDoeThree)));
    assertThat(jonDoeSessions.get(getSessionId(jonDoeFour)), is(equalTo(jonDoeFour)));
    assertThat(jonDoeSessions.get(getSessionId(jonDoeFive)), is(equalTo(jonDoeFive)));
  }

  protected Map<String, Session> doFindByPrincipalName(String principalName) {
    SelectResults<Object> results = executeQuery(FIND_BY_PRINCIPAL_NAME_QUERY, principalName);

    Map<String, Session> sessions = new HashMap<>(results.size());

    for (Object session : results.asList()) {
      sessions.put(getSessionId(session), toSession(session));
    }

    return sessions;
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
      this.lastAccessedTime = this.creationTime;
    }

    private Object assertValidId(Object id) {
      if (isInvalidId(id)) {
        throw new IllegalArgumentException(String.format("id (%1$s) is not valid", id));
      }

      return id;
    }

    @SuppressWarnings("unchecked")
    public static Session from(PdxInstance session) {
      Session sessionCopy = new Session(String.valueOf(session.getField("id")));

      sessionCopy.creationTime = Long.valueOf(String.valueOf(session.getField("creationTime")));
      sessionCopy.setLastAccessedTime(System.currentTimeMillis());
      sessionCopy.setMaxInactiveIntervalInSeconds(Integer.valueOf(String.valueOf(
        session.getField("maxInactiveIntervalInSeconds"))));
      sessionCopy.setPrincipalName(String.valueOf(session.getField("principalName")));

      Set<String> attributeNames = (Set<String>) session.getField("attributeNames");

      for (String attributeName : attributeNames) {
        sessionCopy.setAttribute(attributeName, session.getField(attributeName));
      }

      return sessionCopy;
    }

    public static Session from(Session session) {
      Session sessionCopy = new Session(session.getId());

      sessionCopy.creationTime = session.getCreationTime();
      sessionCopy.setLastAccessedTime(System.currentTimeMillis());
      sessionCopy.setMaxInactiveIntervalInSeconds(session.getMaxInactiveIntervalInSeconds());
      sessionCopy.setPrincipalName(session.getPrincipalName());

      for (String attributeName : session.getAttributeNames()) {
        sessionCopy.setAttribute(attributeName, session.getAttribute(attributeName));
      }

      return sessionCopy;
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
      writer.markIdentityField("id");

      // write Session attributes
      writer.writeObject("attributes", attributes);

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
