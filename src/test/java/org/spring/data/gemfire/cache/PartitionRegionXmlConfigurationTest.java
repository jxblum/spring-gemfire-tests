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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;

import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.LoaderHelper;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.util.CacheListenerAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.dao.support.DaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * The PartitionRegionXmlConfigurationTest class is a test suite of test cases testing the injection
 * of a GemFire Cache, Partitioned Region pre-initialized with data into a Spring bean managed component.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.AbstractGemFireIntegrationTest
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see org.apache.geode.cache.Region
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class PartitionRegionXmlConfigurationTest extends AbstractGemFireIntegrationTest {

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private ApplicationDao appDao;

  @Test
  @SuppressWarnings("unchecked")
  public void testPartitionRegionReference() {
    assertTrue(applicationContext.containsBean("Example"));

    Region<String, Integer> exampleReference = applicationContext.getBean("Example", Region.class);

    assertSame(exampleReference, appDao.getExample());
    assertEquals(1, appDao.get("one"));
    assertEquals(2, appDao.get("two"));
    assertEquals(3, appDao.get("three"));
  }

  @Repository("appDao")
  public static class ApplicationDao<K, V> extends DaoSupport {

    @Resource(name = "Example")
    private Region<K, V> example;

    public final void setExample(final Map<K, V>  example) {
      Assert.isInstanceOf(Region.class, example, "The reference to the '/Example' Partitioned Region must not be null!");
      this.example = (Region<K, V>) example;
    }

    /*
    public final void setExample(final Region<K, V>  example) {
      Assert.notNull(example, "The reference to the 'Example' Partitioned Region must not null!");
      this.example = example;
    }
    */

    protected Region<K, V> getExample() {
      Assert.state(example != null, "The '/Example' Partitioned Region was not properly initialized");
      return example;
    }

    @Override
    protected void checkDaoConfig() throws IllegalArgumentException {
      getExample();
    }

    public V get(final K key) {
      return getExample().get(key);
    }

    public int size() {
      return getExample().size();
    }
  }

  public static class ExampleCacheListener<K, V> extends CacheListenerAdapter<K, V> {

    @Override
    public void afterCreate(final EntryEvent<K, V> event) {
      System.out.printf("Created Region (%1$s) Key (%2$s) / Value (%3$s)", event.getRegion().getFullPath(),
        event.getKey(), event.getNewValue());
    }

    @Override
    public void afterUpdate(final EntryEvent<K, V> event) {
      System.out.printf("Updated Region (%1$s) Key (%2$s) / Value (%3$s)", event.getRegion().getFullPath(),
        event.getKey(), event.getNewValue());
    }
  }

  public static class NameNumberCacheLoader implements CacheLoader<String, Integer> {

    private static final Map<String, Integer> NUMBERS = new HashMap<>(3);

    static {
      NUMBERS.put("one", 1);
      NUMBERS.put("two", 2);
      NUMBERS.put("three", 3);
    }

    @Override
    public Integer load(final LoaderHelper<String, Integer> helper) throws CacheLoaderException {
      return NUMBERS.get(helper.getKey());
    }

    @Override
    public void close() {
    }
  }

  public static final class RegionToRegionConverter implements ConditionalGenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
      Set<ConvertiblePair> convertibleTypes = new HashSet<>(3, 1.0f);
      convertibleTypes.add(new ConvertiblePair(Region.class, Region.class));
      convertibleTypes.add(new ConvertiblePair(Region.class, Map.class));
      return Collections.unmodifiableSet(convertibleTypes);
    }

    @Override
    public boolean matches(final TypeDescriptor sourceType, final TypeDescriptor targetType) {
      return targetType.getType().isAssignableFrom(sourceType.getType());
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
      return source;
    }
  }

}
