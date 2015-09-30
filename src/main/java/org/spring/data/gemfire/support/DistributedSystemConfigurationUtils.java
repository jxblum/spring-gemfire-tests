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

import com.gemstone.gemfire.distributed.internal.DistributionConfig;

/**
 * The DistributedSystemConfigurationUtils class is a utility class for configuring GemFire' Distributed System
 * (cluster) properties.
 *
 * @author John Blum
 * @see com.gemstone.gemfire.distributed.internal.DistributionConfig
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class DistributedSystemConfigurationUtils {

  public static String getGemFirePrefix() {
    return DistributionConfig.GEMFIRE_PREFIX;
  }

  public static String configureDistributedSystemProperty(final String property, final String value) {
    return getGemFirePrefix().concat(property).concat("=").concat(value);
  }

  public static String configureDistributedSystemPropertyAsSystemProperty(final String property, final String value) {
    return "-D".concat(configureDistributedSystemProperty(property, value));
  }

}
