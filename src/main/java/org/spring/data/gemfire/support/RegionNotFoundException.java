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

import org.codeprimate.util.ResourceNotFoundException;

/**
 * The RegionNotFoundException class is a RuntimeException indicating that the specified Region by name or full path
 * cannot be found.
 *
 * @author John Blum
 * @see org.codeprimate.util.ResourceNotFoundException
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class RegionNotFoundException extends ResourceNotFoundException {

  public RegionNotFoundException() {
  }

  public RegionNotFoundException(final String message) {
    super(message);
  }

  public RegionNotFoundException(final Throwable cause) {
    super(cause);
  }

  public RegionNotFoundException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
