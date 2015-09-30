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

package org.lab.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The NumberDowncastingCompressorTest class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class NumberDowncastingCompressorTest {

  private static final boolean LOGGING_ENABLED = false;

  private static final int ITERATIONS = 1024000;

  protected static String binaryAndToString(final long value, final long mask) {
    return String.format("%1$s(%2$d) & %3$s = %4$s", Long.toBinaryString(value), value, Long.toBinaryString(mask),
      value & mask);
  }

  @BeforeClass
  public static void setup() {
    if (LOGGING_ENABLED) {
      System.out.printf("1l & 0x7F is %1$d%n", 1l & 0x7F);
      System.out.printf("99l & 0x7F is %1$d%n", 99l & 0x7F);
      System.out.printf("126l & 0x7F is %1$d%n", 126l & 0x7F);
      System.out.printf("127l & 0x7F is %1$d%n", 127l & 0x7F);
      System.out.printf("128l & 0x7F is %1$d%n", 128l & 0x7F);
      System.out.printf("1l & 0xFF is %1$d%n", 1l & 0xFF);
      System.out.printf("99l & 0xFF is %1$d%n", 99l & 0xFF);
      System.out.printf("126l & 0xFF is %1$d%n", 126l & 0xFF);
      System.out.printf("127l & 0xFF is %1$d%n", 127l & 0xFF);
      System.out.printf("128l & 0xFF is %1$d%n", 128l & 0xFF);
      System.out.printf("256l & 0xFF is %1$d%n", 256l & 0xFF);
      System.out.printf("334l & 0xFF is %1$d%n", 334l & 0xFF);
      System.out.printf("-1l & 0xFF is %1$s%n", binaryAndToString(-1l, 0xFF));
      System.out.printf("-1l & 0x7F is %1$s%n", binaryAndToString(-1l, 0x7F));
      System.out.printf("128l | 0x7F is %1$d%n", 128l | 0x7F);
      System.out.printf("128l | 0xFF is %1$d%n", 128l | 0xFF);
    }
  }

  protected void logNumericTypeAndValue(Number value) {
    if (LOGGING_ENABLED) {
      System.out.printf("%1$s(%2$s)%n", value.getClass(), value);
    }
  }

  protected void assertByte(byte expectedValued, Number actualValue) {
    logNumericTypeAndValue(actualValue);
    assertEquals(Byte.class, actualValue.getClass());
    assertEquals(expectedValued, actualValue.byteValue());
  }

  @Test
  public void isByte() {
    assertTrue(NumberDowncastingCompressor.isByte(0));
    assertTrue(NumberDowncastingCompressor.isByte(1l));
    assertTrue(NumberDowncastingCompressor.isByte(69l));
    assertTrue(NumberDowncastingCompressor.isByte(99l));
    assertTrue(NumberDowncastingCompressor.isByte(126l));
    assertTrue(NumberDowncastingCompressor.isByte(127l));
    assertTrue(NumberDowncastingCompressor.isByte(-1l));
    assertTrue(NumberDowncastingCompressor.isByte(-42l));
    assertTrue(NumberDowncastingCompressor.isByte(-127l));
    assertTrue(NumberDowncastingCompressor.isByte(-128l));
  }

  @Test
  public void isNotByte() {
    assertFalse(NumberDowncastingCompressor.isByte(128l));
    assertFalse(NumberDowncastingCompressor.isByte(255l));
    assertFalse(NumberDowncastingCompressor.isByte(334l));
    assertFalse(NumberDowncastingCompressor.isByte(511l));
    assertFalse(NumberDowncastingCompressor.isByte(-129l));
    assertFalse(NumberDowncastingCompressor.isByte(-256l));
    assertFalse(NumberDowncastingCompressor.isByte(-397l));
    assertFalse(NumberDowncastingCompressor.isByte(-512l));
    assertFalse(NumberDowncastingCompressor.isByte(-768l));
  }

  @Test
  public void isShort() {
    assertTrue(NumberDowncastingCompressor.isShort(0l));
    assertTrue(NumberDowncastingCompressor.isShort(1l));
    assertTrue(NumberDowncastingCompressor.isShort(128l));
    assertTrue(NumberDowncastingCompressor.isShort(1024l));
    assertTrue(NumberDowncastingCompressor.isShort(32766l));
    assertTrue(NumberDowncastingCompressor.isShort(Short.MAX_VALUE + 0l));
    assertTrue(NumberDowncastingCompressor.isShort(-1l));
    assertTrue(NumberDowncastingCompressor.isShort(-129l));
    assertTrue(NumberDowncastingCompressor.isShort(-1024l));
    assertTrue(NumberDowncastingCompressor.isShort(-32767l));
    assertTrue(NumberDowncastingCompressor.isShort(Short.MIN_VALUE + 0l));
  }

  @Test
  public void isNotShort() {
    assertFalse(NumberDowncastingCompressor.isShort(32768l));
    assertFalse(NumberDowncastingCompressor.isShort(65535l));
    assertFalse(NumberDowncastingCompressor.isShort(65536l));
    assertFalse(NumberDowncastingCompressor.isShort(128000l));
    assertFalse(NumberDowncastingCompressor.isShort(-32769l));
    assertFalse(NumberDowncastingCompressor.isShort(-128000l));
    assertFalse(NumberDowncastingCompressor.isShort(-1024768l));
  }

  @Test
  public void isInteger() {
    assertTrue(NumberDowncastingCompressor.isInteger(0l));
    assertTrue(NumberDowncastingCompressor.isInteger(1l));
    assertTrue(NumberDowncastingCompressor.isInteger(128l));
    assertTrue(NumberDowncastingCompressor.isInteger(32768l));
    assertTrue(NumberDowncastingCompressor.isInteger(2147483646l));
    assertTrue(NumberDowncastingCompressor.isInteger(Integer.MAX_VALUE + 0l));
    assertTrue(NumberDowncastingCompressor.isInteger(-1l));
    assertTrue(NumberDowncastingCompressor.isInteger(-128l));
    assertTrue(NumberDowncastingCompressor.isInteger(-32769l));
    assertTrue(NumberDowncastingCompressor.isInteger(-2147483647l));
    assertTrue(NumberDowncastingCompressor.isInteger(Integer.MIN_VALUE + 0l));
  }

  @Test
  public void isNotInteger() {
    assertFalse(NumberDowncastingCompressor.isInteger(Integer.MAX_VALUE + 1l));
    assertFalse(NumberDowncastingCompressor.isInteger(Long.MAX_VALUE));
    assertFalse(NumberDowncastingCompressor.isInteger(Integer.MIN_VALUE - 1l));
    assertFalse(NumberDowncastingCompressor.isInteger(Long.MIN_VALUE));
  }

  @Test
  public void isLong() {
    assertTrue(NumberDowncastingCompressor.isLong(Integer.MAX_VALUE + 1l));
    assertTrue(NumberDowncastingCompressor.isLong(109876543210l));
    assertTrue(NumberDowncastingCompressor.isLong(Long.MAX_VALUE));
  }

  @Test
  public void isNotLong() {
    assertFalse(NumberDowncastingCompressor.isLong(0l));
    assertFalse(NumberDowncastingCompressor.isLong(64l));
    assertFalse(NumberDowncastingCompressor.isLong(Byte.MAX_VALUE + 0l));
    assertFalse(NumberDowncastingCompressor.isLong(256l));
    assertFalse(NumberDowncastingCompressor.isLong(1024l));
    assertFalse(NumberDowncastingCompressor.isLong(8192l));
    assertFalse(NumberDowncastingCompressor.isLong(Short.MAX_VALUE + 0l));
    assertFalse(NumberDowncastingCompressor.isLong(66123l));
    assertFalse(NumberDowncastingCompressor.isLong(128128l));
    assertFalse(NumberDowncastingCompressor.isLong(Integer.MAX_VALUE + 0l));
  }

  @Test
  public void longIsNotByte() {
    assertNotEquals(Byte.class, NumberDowncastingCompressor.downcast(256).getClass());
    assertNotEquals(256, NumberDowncastingCompressor.downcast(256).byteValue());
    assertEquals(Short.class, NumberDowncastingCompressor.downcast(256).getClass());
  }

  @Test
  public void longToByte() {
    assertByte((byte) 16, NumberDowncastingCompressor.downcast(16l));
    assertByte(Byte.MAX_VALUE, NumberDowncastingCompressor.downcast(Long.valueOf(Byte.MAX_VALUE)));
    assertByte(Byte.MIN_VALUE, NumberDowncastingCompressor.downcast(Long.valueOf(Byte.MIN_VALUE)));
  }

  @Test
  public void performanceOfIsByte() {
    final long seed = System.currentTimeMillis();
    Random random = new Random(seed);

    boolean negativeMatch = false;
    boolean positiveMatch = false;

    long t0 = System.currentTimeMillis();

    for (int index = 0; index < ITERATIONS; index++) {
      boolean result = NumberDowncastingCompressor.isByte(random.nextInt(256));
      positiveMatch |= result;
      negativeMatch |= !result;
    }

    assertTrue(negativeMatch);
    assertTrue(positiveMatch);

    final long isByteRuntime = (System.currentTimeMillis() - t0);

    System.out.printf("isByte() runtime is %1$dms%n", isByteRuntime);

    negativeMatch = false;
    positiveMatch = false;
    random = new Random(seed);
    t0 = System.currentTimeMillis();

    for (int index = 0; index < ITERATIONS; index++) {
      boolean result = NumberDowncastingCompressor.isByteByCasting(random.nextInt(256));
      positiveMatch |= result;
      negativeMatch |= !result;
    }

    assertTrue(negativeMatch);
    assertTrue(positiveMatch);

    final long isByteByCastingRuntime = (System.currentTimeMillis() - t0);

    System.out.printf("isByteByCasting() runtime is %1$dms%n", isByteByCastingRuntime);

    //assertTrue(String.format("The runtime (%1$d) isByte() is slower than the runtime (%2$d) of isByteByCasting()!",
    //  isByteRuntime, isByteByCastingRuntime), isByteRuntime <= isByteByCastingRuntime);

    negativeMatch = false;
    positiveMatch = false;
    random = new Random(seed);
    t0 = System.currentTimeMillis();

    for (int index = 0; index < ITERATIONS; index++) {
      boolean result = NumberDowncastingCompressor.isByteByComparison(random.nextInt(256));
      positiveMatch |= result;
      negativeMatch |= !result;
    }

    assertTrue(negativeMatch);
    assertTrue(positiveMatch);

    final long isByteByComparisonRuntime = (System.currentTimeMillis() - t0);

    System.out.printf("isByteByComparison() runtime is %1$dms%n", isByteByComparisonRuntime);

    //assertTrue(String.format("The runtime (%1$d) isByte() is slower than the runtime (%2$d) of isByteByComparison()!",
    //  isByteRuntime, isByteByComparisonRuntime), isByteRuntime <= isByteByComparisonRuntime);
  }

  @Test
  public void toBytes() {
    final long value = 0x00001000CAFEBABEl;

    byte[] array = NumberDowncastingCompressor.toBytes(value);

    assertEquals(6, array.length);
    assertEquals((byte) 0x10, array[0]);
    assertEquals((byte) 0x00, array[1]);
    assertEquals((byte) 0xCA, array[2]);
    assertEquals((byte) 0xFE, array[3]);
    assertEquals((byte) 0xBA, array[4]);
    assertEquals((byte) 0xBE, array[5]);
  }

  @SuppressWarnings("unused")
  protected static class NumberDowncastingCompressor {

    protected static boolean isByte(Number number) {
      long longValue = number.longValue();
      return ((longValue << 56 & 0xFF00000000000000l) >> 56 == longValue);
    }

    protected static boolean isByteByCasting(Number number) {
      return (number.byteValue() == number.longValue());
    }

    protected static boolean isByteByComparison(Number number) {
      long longValue = number.longValue();
      return (longValue >= Byte.MIN_VALUE && longValue <= Byte.MAX_VALUE);
    }

    protected static boolean isShort(Number number) {
      long longValue = number.longValue();
      return ((longValue << 48 & 0xFFFF000000000000l) >> 48 == longValue);
    }

    protected static boolean isShortByCasting(Number number) {
      return (number.shortValue() == number.longValue());
    }

    protected static boolean isShortByComparison(Number number) {
      long longValue = number.longValue();
      return (longValue >= Short.MIN_VALUE && longValue <= Short.MAX_VALUE);
    }

    protected static boolean isInteger(Number number) {
      long longValue = number.longValue();
      return ((longValue << 32 & 0xFFFFFFFF00000000l) >> 32 == longValue);
    }

    protected static boolean isIntegerByCasting(Number number) {
      return (number.intValue() == number.longValue());
    }

    protected static boolean isIntegerByComparison(Number number) {
      long longValue = number.longValue();
      return (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE);
    }

    protected static boolean isLong(Number number) {
      return !(isByte(number) || isShort(number) || isInteger(number));
    }

    protected static boolean isFloat(Number number) {
      return (number.floatValue() == number.doubleValue());
    }

    protected static boolean isFloatByComparison(Number number) {
      double doubleValue = number.doubleValue();
      return (doubleValue >= Float.MIN_VALUE && doubleValue <= Float.MAX_VALUE);
    }

    protected static boolean isWhole(Number number) {
      double doubleValue = number.doubleValue();
      return (Math.floor(doubleValue) == doubleValue);
    }

    public static byte[] toBytes(Number number) {
      byte[] bytes = ByteBuffer.allocate(8).putLong(0, number.longValue()).array();

      boolean record = false;
      ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream(bytes.length);

      for (byte bite : bytes) {
        if (record || bite != 0) {
          byteBuffer.write(bite);
          record = true;
        }
      }

      return byteBuffer.toByteArray();
    }

    public static Number downcast(Number number) {
      Number result = number;

      // Number is whole
      if (isWhole(number)) {
        if (isByteByComparison(number)) {
          result = number.byteValue();
        }
        else if (isShortByComparison(number)) {
          result = number.shortValue();
        }
        else if (isIntegerByComparison(number)) {
          result = number.intValue();
        }
      }
      // Number is floating
      else {
        if (isFloat(number)) {
          result = number.floatValue();
        }
      }

      return result;
    }
  }

}
