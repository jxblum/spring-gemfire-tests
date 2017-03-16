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

import java.util.Properties;

import org.apache.geode.cache.Declarable;

/**
 * The DeclarableSupport class is a abstract base implementation of GemFire's Declarable interface.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Declarable
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class DeclarableSupport implements Declarable {

  /**
   * Initializes the user-defined POJO using the provided Properties defined as parameters in declarative cache XML
   * configuration meta-data.
   *
   * @param parameters the Properties object containing the parameters used to initialize this POJO.
   * @see java.util.Properties
   * @see org.apache.geode.cache.Declarable#init(java.util.Properties)
   */
  @Override
  public void init(final Properties parameters) {
    // do nothing
  }

}
