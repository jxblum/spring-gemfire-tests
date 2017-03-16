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

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.util.ObjectUtils;

/**
 * The Gemstone class is an abstract data type for modeling a gemstone, such as a diamond or a ruby.
 *
 * @author John Blum
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.annotation.PersistenceConstructor
 * @see org.springframework.data.gemfire.mapping.Region
 * @since 1.0.0
 */
@Region("Gemstones")
@SuppressWarnings("unused")
public class Gemstone {

  private GemstoneType gemstoneType;

  @Id
  private Long id;

  private String name;

  @PersistenceConstructor
  public Gemstone() {
  }

  public Gemstone(final Long id) {
    this.id = id;
  }

  public Gemstone(final Long id, final String name) {
    this.id = id;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public GemstoneType getType() {
    return gemstoneType;
  }

  public void setType(final GemstoneType gemstoneType) {
    this.gemstoneType = gemstoneType;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof Gemstone)) {
      return false;
    }

    Gemstone that = (Gemstone) obj;

    return ObjectUtils.nullSafeEquals(this.getId(), that.getId())
      && ObjectUtils.nullSafeEquals(this.getName(), that.getName())
      && ObjectUtils.nullSafeEquals(this.getType(), that.getType());
  }

  @Override
  public int hashCode() {
    int hashValue = 17;
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getId());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getName());
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getType());
    return hashValue;
  }

  @Override
  public String toString() {
    return String.format("{ @type = %1$s, id = %2$d, name = %3$s, type = %4$s }", getClass().getName(), getId(),
      getName(), getType());
  }
}
