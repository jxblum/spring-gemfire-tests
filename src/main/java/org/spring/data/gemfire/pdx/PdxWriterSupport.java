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

package org.spring.data.gemfire.pdx;

import java.util.Date;

import org.apache.geode.pdx.PdxUnreadFields;
import org.apache.geode.pdx.PdxWriter;

/**
 * The PdxWriterSupport class is an Adapter for the GemFire PdxWriter interface for simplifying implementations
 *
 * @author John Blum
 * @see org.apache.geode.pdx.PdxWriter
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class PdxWriterSupport implements PdxWriter {

  private static final String NOT_IMPLEMENTED = "not implemented";

  @Override
  public PdxWriter writeChar(final String fieldName, final char value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeBoolean(final String fieldName, final boolean value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeByte(final String fieldName, final byte value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeShort(final String fieldName, final short value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeInt(final String fieldName, final int value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeLong(final String fieldName, final long value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeFloat(final String fieldName, final float value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeDouble(final String fieldName, final double value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeDate(final String fieldName, final Date value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeString(final String fieldName, final String value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeObject(final String fieldName, final Object value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeObject(final String fieldName, final Object value, final boolean checkPortability) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeBooleanArray(final String fieldName, final boolean[] value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeCharArray(final String fieldName, final char[] value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeByteArray(final String fieldName, final byte[] value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeShortArray(final String fieldName, final short[] value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeIntArray(final String fieldName, final int[] value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeLongArray(final String fieldName, final long[] value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeFloatArray(final String fieldName, final float[] value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeDoubleArray(final String fieldName, final double[] value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeStringArray(final String fieldName, final String[] value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeObjectArray(final String fieldName, final Object[] value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeObjectArray(final String fieldName, final Object[] value, final boolean checkPortability) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeArrayOfByteArrays(final String fieldName, final byte[][] value) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public <CT, VT extends CT> PdxWriter writeField(final String fieldName,
                                                  final VT fieldValue,
                                                  final Class<CT> fieldType)
  {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public <CT, VT extends CT> PdxWriter writeField(final String fieldName,
                                                  final VT fieldValue,
                                                  final Class<CT> fieldType,
                                                  final boolean checkPortability)
  {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter writeUnreadFields(final PdxUnreadFields unread) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxWriter markIdentityField(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

}
