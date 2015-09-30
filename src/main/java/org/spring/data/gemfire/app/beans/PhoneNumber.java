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

package org.spring.data.gemfire.app.beans;

import org.codeprimate.util.ComparatorAccumulator;
import org.springframework.util.ObjectUtils;

/**
 * The PhoneNumber class...
 *
 * @author John Blum
 * @see
 * @since 7.x
 */
@SuppressWarnings("unused")
public class PhoneNumber implements Comparable<PhoneNumber> {

  private Long id;

  private String areaCode;
  private String prefix;
  private String suffix;
  private String extension;

  public PhoneNumber() {
  }

  public PhoneNumber(final Long id) {
    this.id = id;
  }

  public PhoneNumber(final String areaCode, final String prefix, final String suffix) {
    this.areaCode = areaCode;
    this.prefix = prefix;
    this.suffix = suffix;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getAreaCode() {
    return areaCode;
  }

  public void setAreaCode(final String areaCode) {
    this.areaCode = areaCode;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(final String suffix) {
    this.suffix = suffix;
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension(final String extension) {
    this.extension = extension;
  }

  @Override
  public int compareTo(final PhoneNumber that) {
    return new ComparatorAccumulator()
      .doCompare(this.getAreaCode(), that.getAreaCode())
      .doCompare(this.getPrefix(), that.getPrefix())
      .doCompare(this.getSuffix(), that.getSuffix())
      .doCompare(this.getExtension(), that.getExtension())
      .getResult();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof PhoneNumber)) {
      return false;
    }

    PhoneNumber that = (PhoneNumber) obj;

    return ObjectUtils.nullSafeEquals(this.getAreaCode(), that.getAreaCode())
      && ObjectUtils.nullSafeEquals(this.getPrefix(), that.getPrefix())
      && ObjectUtils.nullSafeEquals(this.getSuffix(), that.getSuffix())
      && ObjectUtils.nullSafeEquals(this.getExtension(), that.getExtension());
  }

  @Override
  public int hashCode() {
    int hashValue = 17;
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getAreaCode());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getPrefix());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getSuffix());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getExtension());
    return hashValue;
  }

  @Override
  public String toString() {
    return String.format("(%1$s) %2$s-%3$s x%4$s", getAreaCode(), getPrefix(), getSuffix(), getExtension());
  }

}
