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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * The CachingWithGeodeIntegrationTest class is a test suite of test cases testing the contract and functionality
 * of the Spring Framework's Cache Abstraction using Apache Geode as the caching provider applied
 * using Spring Data GemFire.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.stereotype.Service
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration
@SuppressWarnings("unused")
public class CachingWithGeodeIntegrationTest {

  @Autowired
  private MathService mathService;

  @Test
  public void aCacheMisses() {
    assertThat(mathService.factorial(BigInteger.valueOf(0)), is(equalTo(BigInteger.ONE)));
    assertThat(mathService.wasCacheMiss(), is(true));
    assertThat(mathService.factorial(BigInteger.valueOf(1)), is(equalTo(BigInteger.ONE)));
    assertThat(mathService.wasCacheMiss(), is(true));
    assertThat(mathService.factorial(BigInteger.valueOf(2)), is(equalTo(MathService.TWO)));
    assertThat(mathService.wasCacheMiss(), is(true));
    assertThat(mathService.factorial(BigInteger.valueOf(4)), is(equalTo(BigInteger.valueOf(24))));
    assertThat(mathService.wasCacheMiss(), is(true));
    assertThat(mathService.factorial(BigInteger.valueOf(8)), is(equalTo(BigInteger.valueOf(40320))));
    assertThat(mathService.wasCacheMiss(), is(true));
  }

  @Test
  public void bCacheHits() {
    assertThat(mathService.factorial(BigInteger.valueOf(0)), is(equalTo(BigInteger.ONE)));
    assertThat(mathService.wasCacheMiss(), is(false));
    assertThat(mathService.factorial(BigInteger.valueOf(1)), is(equalTo(BigInteger.ONE)));
    assertThat(mathService.wasCacheMiss(), is(false));
    assertThat(mathService.factorial(BigInteger.valueOf(2)), is(equalTo(MathService.TWO)));
    assertThat(mathService.wasCacheMiss(), is(false));
    assertThat(mathService.factorial(BigInteger.valueOf(4)), is(equalTo(BigInteger.valueOf(24))));
    assertThat(mathService.wasCacheMiss(), is(false));
    assertThat(mathService.factorial(BigInteger.valueOf(8)), is(equalTo(BigInteger.valueOf(40320))));
    assertThat(mathService.wasCacheMiss(), is(false));
  }

  @Test
  public void cCacheMissesAgain() {
    assertThat(mathService.factorial(BigInteger.valueOf(3)), is(equalTo(BigInteger.valueOf(6))));
    assertThat(mathService.wasCacheMiss(), is(true));
    assertThat(mathService.factorial(BigInteger.valueOf(5)), is(equalTo(BigInteger.valueOf(120))));
    assertThat(mathService.wasCacheMiss(), is(true));
    assertThat(mathService.factorial(BigInteger.valueOf(6)), is(equalTo(BigInteger.valueOf(720))));
    assertThat(mathService.wasCacheMiss(), is(true));
    assertThat(mathService.factorial(BigInteger.valueOf(7)), is(equalTo(BigInteger.valueOf(5040))));
    assertThat(mathService.wasCacheMiss(), is(true));
    assertThat(mathService.factorial(BigInteger.valueOf(9)), is(equalTo(BigInteger.valueOf(362880))));
    assertThat(mathService.wasCacheMiss(), is(true));
  }

  @Service("mathService")
  public static class MathService {

    protected static final BigInteger NEGATIVE_ONE = BigInteger.ONE.negate();
    protected static final BigInteger TWO = BigInteger.valueOf(2);

    protected static final String NUMBER_LESS_THAN_ZERO_ERROR_MESSAGE = "number (%1$d) must be greater than equal to 0";

    private volatile boolean cacheMiss = false;

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

    private void setCacheMiss() {
      this.cacheMiss = true;
    }

    public boolean wasCacheMiss() {
      boolean localCacheMiss = this.cacheMiss;
      this.cacheMiss = false;
      return localCacheMiss;
    }
  }

}
