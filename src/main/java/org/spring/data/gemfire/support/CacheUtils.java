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

package org.spring.data.gemfire.support;

import java.util.ArrayList;
import java.util.List;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Region;

/**
 * The CacheUtils class is a utility class for operating on GemFire Cache instances.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class CacheUtils {

  public static Region[] getRegions(final Cache gemfireCache) {
    List<Region> regions = new ArrayList<>();

    for (Region rootRegion : gemfireCache.rootRegions()) {
      regions.add(rootRegion);

      for (Object subRegion : rootRegion.subregions(true)) {
        regions.add((Region) subRegion);
      }
    }

    return regions.toArray(new Region[regions.size()]);
  }

  public static boolean close(final Cache cache) {
    if (cache != null && !cache.isClosed()) {
      try {
        cache.close();
      }
      catch (Exception ignore) {
        ignore.printStackTrace(System.err);
      }
    }

    return (cache == null || cache.isClosed());
  }

}
