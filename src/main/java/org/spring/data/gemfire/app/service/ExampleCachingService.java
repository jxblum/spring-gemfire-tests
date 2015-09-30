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

package org.spring.data.gemfire.app.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * The ExampleCachingService class is an example Spring @Service component contain Cacheable service operations.
 *
 * @author John Blum
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.stereotype.Service
 * @since 1.0.0
 */
@Service
@Cacheable("People")
@SuppressWarnings("unused")
public class ExampleCachingService {

  @Cacheable("Customers")
  public Object exampleCacheableServiceMethodOne(Long id) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Cacheable("Accounts")
  public Object exampleCacheableServiceMethodTwo(Long id) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Cacheable({ "Products", "Orders", "Items" })
  public Object exampleCacheableServiceMethodThree(Long id) {
    throw new UnsupportedOperationException("not implemented");
  }

}
