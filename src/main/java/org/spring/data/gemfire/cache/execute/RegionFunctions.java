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

package org.spring.data.gemfire.cache.execute;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;

import org.spring.data.gemfire.config.CacheableAnnotationDynamicRegionCreationBeanPostProcessor;
import org.spring.data.gemfire.support.RegionNotFoundException;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.util.StringUtils;

/**
 * The RegionFunctions class is a POJO containing GemFire Server-defined Functions for Cache Region data.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.function.annotation.GemfireFunction
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.cache.execute.FunctionContext
 * @see com.gemstone.gemfire.cache.execute.RegionFunctionContext
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class RegionFunctions extends CommonFunctions {

  @GemfireFunction(id = CacheableAnnotationDynamicRegionCreationBeanPostProcessor.CREATE_REGION_FUNCTION_ID)
  public synchronized boolean createRegion(String regionName, DataPolicy dataPolicy) {
    Cache gemfireCache = CacheFactory.getAnyInstance();

    boolean result = false;

    if (gemfireCache.getRegion(regionName) == null) {
      RegionFactory regionFactory = gemfireCache.createRegionFactory();

      regionFactory.setDataPolicy(dataPolicy);
      result = (regionFactory.create(regionName) != null);
    }

    return result;
  }

  @GemfireFunction
  public Integer regionSize(final FunctionContext context, final String regionNamePath) {
    Region region = getRegion(context, regionNamePath);

    if (region != null) {
      return region.size();
    }

    throw new RegionNotFoundException("The Region on which the size will be determined was not found!");
  }

  protected Region getRegion(final FunctionContext context, final String regionNamePath) {
    Region region = (context instanceof RegionFunctionContext ? ((RegionFunctionContext) context).getDataSet() : null);

    if (region == null && !StringUtils.isEmpty(regionNamePath)) {
      region = CacheFactory.getAnyInstance().getRegion(regionNamePath);
    }

    if (region == null && context.getArguments() instanceof String) {
      region = CacheFactory.getAnyInstance().getRegion(context.getArguments().toString());
    }

    return region;
  }

}
