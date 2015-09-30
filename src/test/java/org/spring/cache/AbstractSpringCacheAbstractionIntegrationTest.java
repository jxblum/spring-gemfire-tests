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

import java.math.BigInteger;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * AbstractSpringCacheAbstractionIntegrationTest is a base class for implementing Spring Cache Abstraction test class
 * using different caching providers and configurations.
 *
 * @author John Blum
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.stereotype.Service
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractSpringCacheAbstractionIntegrationTest {

  public static abstract class CachingSupport {

    private volatile boolean cacheMiss = false;

    protected void setCacheMiss() {
      this.cacheMiss = true;
    }

    protected void unsetCacheMiss() {
      this.cacheMiss = false;
    }

    public boolean wasCacheMiss() {
      boolean localCacheMiss = this.cacheMiss;
      unsetCacheMiss();
      return localCacheMiss;
    }
  }

  @Service("mathService")
  public static class MathService extends CachingSupport {

    protected static final BigInteger NEGATIVE_ONE = BigInteger.ONE.negate();
    protected static final BigInteger TWO = BigInteger.valueOf(2);

    protected static final String NUMBER_LESS_THAN_ZERO_ERROR_MESSAGE = "number (%1$d) must be greater than equal to 0";

    @Cacheable("Numbers")
    public BigInteger factorial(BigInteger number) {
      setCacheMiss();

      Assert.notNull(number, "number must not be null");

      Assert.isTrue(number.compareTo(BigInteger.ZERO) >= 0, String.format(NUMBER_LESS_THAN_ZERO_ERROR_MESSAGE, number));

      if (number.compareTo(TWO) <= 0) {
        return (number.equals(TWO) ? TWO : BigInteger.ONE);
      }

      BigInteger result = number;

      for (number = result.add(NEGATIVE_ONE) ; number.compareTo(BigInteger.ONE) > 0; number = number.add(NEGATIVE_ONE)) {
        result = result.multiply(number);
      }

      return result;
    }
  }

}
