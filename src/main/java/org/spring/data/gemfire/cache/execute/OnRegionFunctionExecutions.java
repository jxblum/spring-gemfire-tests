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

import org.springframework.data.gemfire.function.annotation.OnRegion;

/**
 * The OnRegionFunctionExecutions class...
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.function.annotation.OnRegion
 * @since 1.3.3
 */
// NOTE the annotation region attribute limits us to the specified Region
@OnRegion(region = "Example")
@SuppressWarnings("unused")
public interface OnRegionFunctionExecutions {

  String echo(String echo);

  // NOTE the return type; see comment(s) and test case code in the
  // FunctionCreationExecutionTest.testRegionSizeWithSpringAnnotatedFunction method for further details...
  Object regionSize(String regionNamePath);

}
