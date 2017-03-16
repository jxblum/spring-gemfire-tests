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

package org.spring.data.gemfire.cache.manager;

import java.util.concurrent.Callable;

import org.apache.geode.cache.Region;
import org.springframework.cache.Cache;
import org.springframework.data.gemfire.cache.GemfireCacheManager;
import org.springframework.util.Assert;

/**
 * The CustomerGemFireCacheManager class...
 *
 * @author jb
 * @see org.springframework.cache.Cache
 * @see org.springframework.data.gemfire.support.GemfireCacheManager
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class CustomGemFireCacheManager extends GemfireCacheManager {

  @Override
  protected Cache decorateCache(Cache cache) {
    return new CustomDelegatingGemFireCache(cache);
  }

  // TODO replace this Token enum with the custom PdxSerializer class, containing the Token enum, where
  // the custom PdxSerializer is for the application domain class who's version changed between
  // GemFire client applications
  public static enum Token { NULL }

  protected static final class CustomDelegatingGemFireCache implements Cache {

    private final Cache cache;

    private CustomDelegatingGemFireCache(final Cache cache) {
      Assert.notNull(cache, "The Cache to delegate to must not be null!");
      this.cache = cache;
    }

    @Override
    public String getName() {
      return cache.getName();
    }

    @Override
    public Object getNativeCache() {
      return cache.getNativeCache();
    }

    // NOTE this method is key...
    @Override
    public ValueWrapper get(final Object key) {
      ValueWrapper valueWrapper = cache.get(key);

      return (Token.NULL.equals(valueWrapper.get()) ? null : valueWrapper);
    }

    @Override
    public <T> T get(final Object key, final Class<T> type) {
      return type.cast(get(key));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
      return (T) get(key, Object.class);
    }

    @Override
    public void put(final Object key, final Object value) {
      cache.put(key, value);
    }

    //@Override
    @SuppressWarnings("unchecked")
    public ValueWrapper putIfAbsent(final Object key, final Object value) {
      //return cache.putIfAbsent(key, value);
      final Object previousValue = ((Region<Object, Object>) getNativeCache()).putIfAbsent(key, value);
      return new ValueWrapper() {
        @Override public Object get() {
          return previousValue;
        }
      };
    }

    @Override
    public void evict(final Object key) {
      cache.evict(key);
    }

    @Override
    public void clear() {
      cache.clear();
    }
  }
}
