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

import java.util.Map;
import javax.annotation.Resource;

import com.gemstone.gemfire.cache.CacheLoader;
import com.gemstone.gemfire.cache.CacheLoaderException;
import com.gemstone.gemfire.cache.LoaderHelper;

import org.springframework.data.gemfire.LazyWiringDeclarableSupport;
import org.springframework.util.Assert;

/**
 * The NameNumberCacheLoader class is a GemFire CacheLoader loading number by name from an external data source into
 * a Region configured with this CacheLoader instance.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.LazyWiringDeclarableSupport
 * @see com.gemstone.gemfire.cache.CacheLoader
 * @see com.gemstone.gemfire.cache.LoaderHelper
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class NamedNumbersCacheLoader extends LazyWiringDeclarableSupport implements CacheLoader<String, Integer> {

  private Map<String, Integer> namedNumbers;

  @Resource(name = "NamedNumbers")
  public final void setNamedNumbers(final Map<String, Integer> namedNumbers) {
    Assert.notNull(namedNumbers, "The reference to the 'NamedNumbers' Map must not be null!");
    this.namedNumbers = namedNumbers;
  }

  protected Map<String, Integer> getNamedNumbers() {
    Assert.state(namedNumbers != null, "The reference to the 'NamedNumbers' Map was not properly configured and initialized!");
    return namedNumbers;
  }

  @Override
  public Integer load(final LoaderHelper<String, Integer> helper) throws CacheLoaderException {
    return getNamedNumbers().get(helper.getKey());
  }

  @Override
  public void close() {
  }

}
