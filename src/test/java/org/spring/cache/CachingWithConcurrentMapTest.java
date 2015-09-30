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

package org.spring.cache;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ObjectUtils;

/**
 * The CachingWithConcurrentMapTest class is a test suite of test cases testing the contract and functionality
 * of @Cacheable inheritance.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class CachingWithConcurrentMapTest {

  @Autowired
  private FactorialComputeService computeService;

  @Test
  public void testCachedObject() {
    ValueHolder<Long> twoSquared = computeService.squared(2l);
    ValueHolder<Long> twoSquaredAgain = computeService.squared(2l);

    assertEquals(twoSquared, twoSquaredAgain);
    assertSame(twoSquared, twoSquaredAgain);

    ValueHolder<Long> fourFactorial = computeService.factorial(4l);
    ValueHolder<Long> fourFactorialAgain = computeService.factorial(4l);

    assertEquals(fourFactorial, fourFactorialAgain);
    assertSame(fourFactorial, fourFactorialAgain);
    assertNotSame(twoSquared, fourFactorial);

    ValueHolder<Long> eightSquared = computeService.squared(8l);
    ValueHolder<Long> eightSquaredAgain = computeService.squared(8l);

    assertEquals(eightSquared, eightSquaredAgain);
    assertSame(eightSquared, eightSquaredAgain);
    assertNotSame(twoSquared, eightSquared);
    assertNotSame(fourFactorial, eightSquared);
  }

  @Service
  public static class SquaredComputeService {

    @Cacheable("Computations")
    public ValueHolder<Long> squared(Long value) {
      return new ValueHolder<>(value * value);
    }
  }

  @Service
  public static class FactorialComputeService extends SquaredComputeService {

    @Cacheable("Computations")
    public ValueHolder<Long> factorial(Long value) {
      return new ValueHolder<>(computeFactorial(value));
    }

    protected long computeFactorial(long value) {
      long result = value;
      while (--value > 0) {
        result *= value;
      }
      return result;
    }
  }

  public static class ValueHolder<T> {

    private T value;

    public ValueHolder() {
      this(null);
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

      return ObjectUtils.nullSafeEquals(this.getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getValue());
      return hashValue;
    }

    @Override
    public String toString() {
      return String.format("{ @type = %1$s, value = %2$s }", getClass().getName(), getValue());
    }
  }

}
