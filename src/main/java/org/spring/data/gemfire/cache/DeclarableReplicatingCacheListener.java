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

import javax.annotation.Resource;

import org.apache.geode.cache.CacheListener;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionEvent;
import org.springframework.data.gemfire.support.LazyWiringDeclarableSupport;

/**
 * The DeclarableLoggingCacheListener class...
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.LazyWiringDeclarableSupport
 * @see org.apache.geode.cache.CacheListener
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class DeclarableReplicatingCacheListener<K, V> extends LazyWiringDeclarableSupport implements CacheListener<K, V> {

  private Region<K, V> backingRegion;

  @Resource(name = "BackingRegion")
  public final void setBackingRegion(Region<K, V> backingRegion) {
    this.backingRegion = backingRegion;
  }

  protected void logEvent(EntryEvent<K, V> event, String operation) {
    System.out.printf("{ op = %1$s, event = { region = %2$s, key = %3$s, oldValue = %4$s newValue = %5$s } }%n",
      operation, event.getRegion().getFullPath(), event.getKey(), event.getOldValue(), event.getNewValue());
  }

  @Override
  public void afterCreate(EntryEvent<K, V> event) {
    logEvent(event, "CREATE");
    if (backingRegion != null) {
      backingRegion.create(event.getKey(), event.getNewValue());
    }
    else {
      System.out.println("/BackingRegion is null!");
    }
  }

  @Override
  public void afterDestroy(EntryEvent<K, V> event) {
    logEvent(event, "DESTROY");
    if (backingRegion != null) {
      backingRegion.destroy(event.getKey());
    }
    else {
      System.out.println("/BackingRegion is null!");
    }
  }

  @Override
  public void afterInvalidate(EntryEvent<K, V> event) {
    logEvent(event, "INVALIDATE");
    if (backingRegion != null) {
      backingRegion.invalidate(event.getKey());
    }
    else {
      System.out.println("/BackingRegion is null!");
    }
  }

  @Override
  public void afterUpdate(EntryEvent<K, V> event) {
    logEvent(event, "UPDATE");
    if (backingRegion != null) {
      backingRegion.replace(event.getKey(), event.getNewValue());
    }
    else {
      System.out.println("/BackingRegion is null!");
    }
  }

  @Override
  public void afterRegionClear(RegionEvent<K, V> event) {
    if (backingRegion != null) {
      backingRegion.clear();
    }
  }

  @Override
  public void afterRegionCreate(RegionEvent<K, V> event) {
  }

  @Override
  public void afterRegionDestroy(RegionEvent<K, V> event) {
  }

  @Override
  public void afterRegionInvalidate(RegionEvent<K, V> event) {
  }

  @Override
  public void afterRegionLive(RegionEvent<K, V> event) {
  }

  @Override
  public void close() {
  }
}
