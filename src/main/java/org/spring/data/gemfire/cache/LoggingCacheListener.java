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

import java.util.Properties;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;

/**
 * The LoggingCacheListener class is a GemFire CacheListener logging Cache Region Entry Events, such as creates, updates
 * and deletes.
 *
 * @author John Blum
 * @see com.gemstone.gemfire.cache.Declarable
 * @see com.gemstone.gemfire.cache.util.CacheListenerAdapter
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class LoggingCacheListener<K, V> extends CacheListenerAdapter<K, V> {

  @Override
  public void afterCreate(final EntryEvent<K, V> event) {
    System.out.printf("Created Region (%1$s) Entry with Key (%2$s) and Value (%3$s)", event.getRegion().getFullPath(),
      event.getKey(), event.getNewValue());
  }

  @Override
  public void afterUpdate(final EntryEvent<K, V> event) {
    System.out.printf("Updated Region (%1$s) Entry with Key (%2$s) from (%3$s) to (%4$s)",
      event.getRegion().getFullPath(), event.getKey(), event.getOldValue(), event.getNewValue());
  }

  @Override
  public void afterDestroy(final EntryEvent<K, V> event) {
    System.out.printf("Removed Region ($1$s) Entry Value for Key (%2$s)", event.getRegion().getFullPath(),
      event.getKey());
  }

}
