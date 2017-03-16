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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.codeprimate.io.FileSystemUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * The PdxDiskStoreTest class is a test suite containing tests to reproduce the issue in JIRA SGF-197.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.spring.data.gemfire.AbstractGemFireTest
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @link https://jira.springsource.org/browse/SGF-197
 * @since 1.3.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SuppressWarnings("unused")
public class PdxDiskStoreTest extends AbstractGemFireTest {

  protected static final int NUMBER_OF_REGION_ENTRIES = 1000;

  @Resource(name = "PdxDataRegion")
  private Region<KeyHolder<String>, ValueHolder<Integer>> pdxDataRegion;

  @BeforeClass
  public static void setupBeforeClass() {
    assertTrue(FileSystemUtils.createDirectory(FileSystemUtils.createFile("./gemfire/data-store")));
    assertTrue(FileSystemUtils.createDirectory(FileSystemUtils.createFile("./gemfire/pdx-store")));
  }

  @AfterClass
  public static void tearDownAfterClass() {
    FileSystemUtils.deleteRecursive(FileSystemUtils.createFile("./gemfire"));
  }

  @Before
  public void setup() {
    assertNotNull("The PdxData GemFire Region was not created successfully!", pdxDataRegion);

    if (pdxDataRegion.size() == 0) {
      System.out.printf("Creating entries for Region (%1$s)...%n", pdxDataRegion.getName());
      for (int index = 1; index <= NUMBER_OF_REGION_ENTRIES; index++) {
        pdxDataRegion.put(new KeyHolder<String>("key" + index), new ValueHolder<Integer>(index));
      }
    }
  }

  @Test
  public void testPersistentRegionWithDataCreation() {
    assertRegion(pdxDataRegion, "PdxData", "/PdxData");
    assertEquals(NUMBER_OF_REGION_ENTRIES, pdxDataRegion.size());
  }

  @Test
  public void testPersistentRegionWithDataRecovery() {
    assertRegion(pdxDataRegion, "PdxData", "/PdxData");
    assertEquals(NUMBER_OF_REGION_ENTRIES, pdxDataRegion.size());
  }

  protected static class AbstractHolderSupport {

    protected static boolean equals(final Object obj1, final Object obj2) {
      return (obj1 != null && obj1.equals(obj2));
    }

    protected static int hashCode(final Object obj) {
      return (obj == null ? 0 : obj.hashCode());
    }
  }

  public static class KeyHolder<T extends Serializable> extends AbstractHolderSupport {

    private T key;

    public KeyHolder() {
    }

    public KeyHolder(final T key) {
      Assert.notNull(key, "The key cannot be null!");
      this.key = key;
    }

    public T getKey() {
      return key;
    }

    public void setKey(final T key) {
      this.key = key;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof KeyHolder)) {
        return false;
      }

      final KeyHolder that = (KeyHolder) obj;

      return equals(this.getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + hashCode(this.getKey());
      return hashValue;
    }

    @Override
    public String toString() {
      return String.valueOf(getKey());
    }
  }

  public static class ValueHolder<T extends Serializable> extends AbstractHolderSupport {

    private T value;

    public ValueHolder() {
    }

    public ValueHolder(final T value) {
      this.value = value;
    }

    public T getValue() {
      return value;
    }

    public void setValue(final T value) {
      this.value = value;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof ValueHolder)) {
        return false;
      }

      final ValueHolder that = (ValueHolder) obj;

      return equals(this.getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 17 * hashValue + hashCode(this.getValue());
      return hashValue;
    }

    @Override
    public String toString() {
      return String.valueOf(getValue());
    }
  }

}
