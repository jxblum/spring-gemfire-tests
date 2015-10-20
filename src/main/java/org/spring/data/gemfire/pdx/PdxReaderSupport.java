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

import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxUnreadFields;

/**
 * The PdxReaderSupport class is an Adapter for the GemFire PdxReader interface for simplifying implementations.
 *
 * @author John Blum
 * @see com.gemstone.gemfire.pdx.PdxReader
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class PdxReaderSupport implements PdxReader {

  private static final String NOT_IMPLEMENTED = "not implemented";

  @Override
  public char readChar(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public boolean readBoolean(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public byte readByte(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public short readShort(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public int readInt(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public long readLong(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public float readFloat(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public double readDouble(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public String readString(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public Object readObject(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public char[] readCharArray(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public boolean[] readBooleanArray(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public byte[] readByteArray(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public short[] readShortArray(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public int[] readIntArray(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public long[] readLongArray(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public float[] readFloatArray(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public double[] readDoubleArray(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public String[] readStringArray(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public Object[] readObjectArray(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public byte[][] readArrayOfByteArrays(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public Date readDate(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public boolean hasField(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public boolean isIdentityField(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public Object readField(final String fieldName) {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public PdxUnreadFields readUnreadFields() {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

}
