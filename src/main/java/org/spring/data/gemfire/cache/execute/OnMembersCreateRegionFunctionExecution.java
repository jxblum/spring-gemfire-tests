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

import org.apache.geode.cache.DataPolicy;
import org.springframework.data.gemfire.function.annotation.OnMembers;

/**
 * The OnMembersCreateRegionFunctionExecution class is a Spring Data GemFire Function Execution
 * used to create GemFire Cache Regions.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.function.annotation.OnMembers
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.DataPolicy
 * @since 1.0.0
 */
@OnMembers
@SuppressWarnings("unused")
public interface OnMembersCreateRegionFunctionExecution {

  boolean createRegion(String regionName, DataPolicy dataPolicy);

}
