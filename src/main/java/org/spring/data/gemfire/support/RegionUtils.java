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

import org.apache.geode.cache.Region;

/**
 * The RegionUtils class is a utility class for working with GemFire Cache Regions.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @since 1.4.0
 */
@SuppressWarnings("unused")
public abstract class RegionUtils extends CacheUtils {

  public static String getFullPath(final Region region) {
    return (region != null ? region.getFullPath() : null);
  }

  public static String getName(final Region region) {
    return (region != null ? region.getName() : null);
  }

  public static String[] getNames(final Region... regions) {
    List<String> regionNames = new ArrayList<>(regions.length);

    for (Region region : regions) {
      regionNames.add(getName(region));
    }

    return regionNames.toArray(new String[regionNames.size()]);
  }

  public static void close(final Region region) {
    if (region != null) {
      region.close();
    }
  }

  public static void destroy(final Region region) {
    if (region != null) {
      region.destroyRegion();
    }
  }

}
