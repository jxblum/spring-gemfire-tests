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


import com.gemstone.gemfire.pdx.PdxInstance;

import org.spring.data.gemfire.cache.ClientCacheFunctionExecutionWithPdxTest.TestDomainClass;
import org.spring.data.gemfire.cache.ClientCacheFunctionExecutionWithPdxTest.TestEnum;
import org.springframework.data.gemfire.function.annotation.OnServer;

/**
 * The ArgumentTypeCaptureFunctionExecution class...
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.function.annotation.OnServer
 * @since 1.0.0
 */
@OnServer
@SuppressWarnings("unused")
public interface ArgumentTypeCaptureFunctionExecution {

  Class[] captureArgumentTypes(String stringValue, Integer integerValue, Double doubleValue, TestDomainClass domainObject, TestEnum enumValue);

}
