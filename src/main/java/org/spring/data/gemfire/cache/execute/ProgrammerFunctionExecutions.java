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

import java.util.List;
import java.util.Set;

import org.spring.data.gemfire.app.beans.Programmer;
import org.springframework.data.gemfire.function.annotation.Filter;
import org.springframework.data.gemfire.function.annotation.OnRegion;

/**
 * The ProgrammerFunctionExecutions class...
 * @author John Blum
 * @see org.spring.data.gemfire.app.beans.Programmer
 * @see org.springframework.data.gemfire.function.annotation.OnRegion
 * @since 1.0.0
 */
@OnRegion(region = "Programmers")
@SuppressWarnings("unused")
public interface ProgrammerFunctionExecutions {

  List<Programmer> updateReputation(@Filter Set<Long> keys, String programmingLanguage, int reputationDelta);

}
