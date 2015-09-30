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

package org.spring.data.gemfire.cache;

import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;

/**
 * The TestCacheListener class...
 *
 * @author John Blum
 * @see com.gemstone.gemfire.cache.CacheListener
 * @since 1.3.3
 */
@SuppressWarnings("unused")
public class DescribableEntryLoggingCacheListener<K, V> extends CacheListenerAdapter<K, V> {

  private String description;

  public DescribableEntryLoggingCacheListener() {
  }

  public DescribableEntryLoggingCacheListener(final String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  @Override
  public void afterCreate(final EntryEvent<K, V> event) {
    super.afterCreate(event);
    System.out.printf("Created entry with key (%1$s) and value (%2$s) in Region (%3$s)...%n", event.getKey(),
      event.getNewValue(), event.getRegion().getName());
  }

  @Override
  public void afterDestroy(final EntryEvent<K, V> event) {
    super.afterCreate(event);
    System.out.printf("Destroying entry with key (%1$s) in Region (%2$s)...%n", event.getKey(),
      event.getRegion().getName());
  }

  @Override
  public void afterInvalidate(final EntryEvent<K, V> event) {
    super.afterCreate(event);
    System.out.printf("Invalidating entry with key (%1$s) in Region (%2$s)...%n", event.getKey(),
      event.getRegion().getName());
  }

  @Override
  public void afterUpdate(final EntryEvent<K, V> event) {
    super.afterCreate(event);
    System.out.printf("Updated entry with new value (%2$s) for key (%2$s) on Region (%3$s)...%n", event.getNewValue(),
      event.getKey(), event.getRegion().getName());
  }

  @Override
  public String toString() {
    return getDescription();
  }

}
