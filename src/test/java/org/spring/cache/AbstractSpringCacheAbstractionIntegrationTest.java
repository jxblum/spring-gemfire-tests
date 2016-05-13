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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * AbstractSpringCacheAbstractionIntegrationTest is a base class for implementing Spring Cache Abstraction test classes
 * using different caching providers and configurations.
 *
 * @author John Blum
 * @see java.util.function.Function
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.stereotype.Service
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractSpringCacheAbstractionIntegrationTest {

  @SafeVarargs
  static <T> T[] asArray(T... array) {
    return array;
  }

  protected static Function<Long, Long> add(long addend) {
    return new Addition(addend);
  }

  protected static Function<Long, Long> divideBy(long divisor) {
    return new Division(divisor);
  }

  protected static Function<Long, Long> factorial() {
    return new Factorial();
  }

  protected static Function<Long, Long> identity() {
    return new Identity();
  }

  protected static Function<Long, Long> modulus(long divisor) {
    return new Modulus(divisor);
  }

  protected static Function<Long, Long> multiplyBy(long multiplier) {
    return new Multiplication(multiplier);
  }

  protected static Function<Long, Long> powerOf(long exponent) {
    return new PowerOf(exponent);
  }

  protected static Function<Long, Long> squareRoot() {
    return new SquareRoot();
  }

  protected static Function<Long, Long> subtract(long subtrahend) {
    return new Subtraction(subtrahend);
  }

  protected static abstract class CachingSupport {

    private volatile boolean cacheMiss = false;

    protected void setCacheMiss() {
      this.cacheMiss = true;
    }

    protected void unsetCacheMiss() {
      this.cacheMiss = false;
    }

    public boolean wasCacheHit() {
      return !wasCacheMiss();
    }

    public boolean wasCacheMiss() {
      boolean localCacheMiss = this.cacheMiss;
      unsetCacheMiss();
      return localCacheMiss;
    }
  }

  @Service
  public static class MathService extends CachingSupport {

    protected static final BigInteger NEGATIVE_ONE = BigInteger.ONE.negate();
    protected static final BigInteger TWO = BigInteger.valueOf(2);

    protected static final String NUMBER_LESS_THAN_ZERO_ERROR_MESSAGE = "number [%1$d] must be greater than equal to 0";

    @Cacheable("Calculations")
    @SuppressWarnings("unchecked")
    public long calculate(long number) {
      return calculate(number, Function.<Long>identity());
    }

    @SuppressWarnings("unchecked")
    @Cacheable(cacheNames = "Calculations", key="#number")
    public long calculate(long number, Function<Long, Long>... calculations) {
      setCacheMiss();

      long result = number;

      for (Function<Long, Long> calculation : calculations) {
        result = calculation.apply(result);
      }

      return result;
    }

    @Cacheable("Factorials")
    public long factorial(long number) {
      return factorial(new BigInteger(Long.toString(number))).longValue();
    }

    @Cacheable("Factorials")
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

  public enum NumberClassification {
    EVEN,
    FLOATING,
    NEGATIVE,
    ODD,
    POSITIVE,
    WHOLE
  }

  public interface NumberClassificationEvaluator {
    NumberClassification classify(double number);
  }

  @Service
  public static class NumberClassificationService extends CachingSupport {

    protected final Set<NumberClassificationEvaluator> numberClassificationEvaluators;

    public NumberClassificationService() {
      Set<NumberClassificationEvaluator> numberClassificationEvaluators = new HashSet<>(3);

      Collections.addAll(numberClassificationEvaluators,
        number -> isEven(number) ? NumberClassification.EVEN : NumberClassification.ODD,
        number -> isPositive(number) ? NumberClassification.POSITIVE : NumberClassification.NEGATIVE,
        number -> isWhole(number) ? NumberClassification.WHOLE : NumberClassification.FLOATING
      );

      this.numberClassificationEvaluators = Collections.unmodifiableSet(numberClassificationEvaluators);
    }

    @Cacheable("Categories")
    public List<NumberClassification> classify(double number) {
      setCacheMiss();

      return numberClassificationEvaluators.stream().map(
        numberClassificationEvaluator -> numberClassificationEvaluator.classify(number)).collect(Collectors.toList());
    }

    protected boolean isEven(double number) {
      return (isWhole(number) && Math.abs(number) % 2 == 0);
    }

    protected boolean isFloating(double number) {
      return !isWhole(number);
    }

    protected boolean isNegative(double number) {
      return (number < 0);
    }

    protected boolean isOdd(double number) {
      return !isEven(number);
    }

    protected boolean isPositive(double number) {
      return (number > 0);
    }

    protected boolean isWhole(double number) {
      return (number == Math.floor(number));
    }
  }

  protected static class Addition implements Function<Long, Long> {

    private final long addend;

    protected Addition(long addend) {
      this.addend = addend;
    }

    @Override
    public Long apply(Long number) {
      return (number + addend);
    }
  }

  protected static class Division implements Function<Long, Long> {

    private final long divisor;

    protected Division(long divisor) {
      this.divisor = divisor;
    }

    @Override
    public Long apply(Long dividend) {
      return (dividend / divisor);
    }
  }

  protected static class Factorial implements Function<Long, Long> {

    @Override
    public Long apply(Long number) {
      Assert.notNull(number, "number must not be null");
      Assert.isTrue(number >= 0, String.format("number [%1$d] must be greater than equal to 0", number));

      if (number <= 2l) {
        return (number < 2l ? 1l : 2l);
      }

      long result = number;

      while (--number > 1l) {
        result *= number;
      }

      return result;
    }
  }

  protected static class Identity implements Function<Long, Long> {

    @Override
    public Long apply(Long number) {
      return number;
    }
  }

  protected static class Modulus implements Function<Long, Long> {

    private final long divisor;

    protected Modulus(long divisor) {
      this.divisor = divisor;
    }

    @Override
    public Long apply(Long dividend) {
      return (dividend % divisor);
    }
  }

  protected static class Multiplication implements Function<Long, Long> {

    private final long multiplier;

    protected Multiplication(long multiplier) {
      this.multiplier = multiplier;
    }

    @Override
    public Long apply(Long multiplicand) {
      return (multiplicand * multiplier);
    }
  }

  protected static class PowerOf implements Function<Long, Long> {

    private final long exponent;

    protected PowerOf(long exponent) {
      this.exponent = exponent;
    }

    @Override
    public Long apply(Long base) {
      return Double.valueOf(Math.pow(base, exponent)).longValue();
    }
  }

  protected static class SquareRoot implements Function<Long, Long> {

    @Override
    public Long apply(Long number) {
      return Double.valueOf(Math.sqrt(number)).longValue();
    }
  }

  protected static class Subtraction implements Function<Long, Long> {

    private final long subtrahend;

    protected Subtraction(long subtrahend) {
      this.subtrahend = subtrahend;
    }

    @Override
    public Long apply(Long minuend) {
      return (minuend - subtrahend);
    }
  }
}
