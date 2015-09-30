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

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ObjectUtils;

/**
 * The RegionInitializationTest class is a test suite of test cases testing the initialization of data (key/values)
 * in a GemFire Cache Region.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see com.gemstone.gemfire.cache.Region
 * @since 1.3.3 (Spring Data GemFire)
 * @since 7.0.1 (GemFire)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class RegionInitializationTest {

  @Resource(name="RegionOne")
  private Region regionOne;

  @Resource(name="RegionTwo")
  private Region regionTwo;

  @Test
  public void testRegionInitialization() {
    assertNotNull(regionOne);
    assertNotNull(regionTwo);

    Object valueOne = regionOne.get("keyOne");

    assertTrue(valueOne instanceof ValueHolder);
    assertEquals("test", ((ValueHolder) valueOne).getValue());
    assertFalse(regionOne.containsKey("keyTwo"));
    assertFalse(regionOne.containsKey("keyThree"));

    Object valueTwo = regionTwo.get("keyTwo");
    Object valueThree = regionTwo.get("keyThree");

    assertTrue(valueTwo instanceof ValueHolder);
    assertEquals("123456789", ((ValueHolder) valueTwo).getValue());
    assertTrue(valueThree instanceof ValueHolder);
    assertEquals("01/10/2014", ((ValueHolder) valueThree).getValue());
    assertFalse(regionTwo.containsKey("keyOne"));
  }

  public static final class ValueHolder<T> {

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

      ValueHolder that = (ValueHolder) obj;

      return (ObjectUtils.nullSafeEquals(this.getValue(), that.getValue()));
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getValue());
      return hashValue;
    }

    @Override
    public String toString() {
      return String.valueOf(getValue());
    }
  }

}
