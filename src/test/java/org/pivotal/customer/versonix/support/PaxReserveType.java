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

package org.pivotal.customer.versonix.support;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.gemstone.gemfire.DataSerializable;

import org.springframework.util.ObjectUtils;

@SuppressWarnings("unused")
public class PaxReserveType implements DataSerializable {

  private boolean linked; // NOTE: should be == true at loadAll

  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isLinked() {
    return linked;
  }

  public void setLinked(boolean linked) {
    this.linked = linked;
  }

  @Override
  public void fromData(DataInput in) throws IOException {
    id = in.readUTF();
    linked = in.readBoolean();
  }

  @Override
  public void toData(DataOutput out) throws IOException {
    out.writeUTF(id);
    out.writeBoolean(linked);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof PaxReserveType)) {
      return false;
    }

    PaxReserveType that = (PaxReserveType) obj;

    return ObjectUtils.nullSafeEquals(this.id, that.id);
  }

  public boolean deepEquals(PaxReserveType other) {
    return (equals(other) && (linked == other.linked));
  }

  @Override
  public int hashCode() {
    int hashValue = 17;
    hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(this.id);
    return hashValue;
  }

  @Override
  public String toString() {
    return String.format("{@type = %1$s, id = %2$s, linked = %3$s}", getClass().getName(), id, linked);
  }

}
