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

package org.spring.data.gemfire.app.beans;

import org.codeprimate.lang.ObjectUtils;
import org.springframework.data.gemfire.mapping.Region;
import org.springframework.util.StringUtils;

/**
 * The Programmer class is a Person representing (modeling) a software engineer/developer.
 *
 * @author John J. Blum
 * @see org.spring.data.gemfire.app.beans.User
 * @see org.springframework.data.gemfire.mapping.Region
 * @since 1.0.0
 */
@Region("Programmers")
//@Region("/People/Programmers")
@SuppressWarnings("unused")
public class Programmer extends Person {

  protected static final Integer DEFAULT_REPUTATION = 1;

  protected static final String DEFAULT_PROGRAMMING_LANGUAGE = "?";

  private Integer reputation;

  private String programmingLanguage;

  public Programmer() {
  }

  public Programmer(final Long id) {
    super(id);
  }

  public Programmer(final String firstName, final String lastName) {
    super(firstName, lastName);
  }

  public String getProgrammingLanguage() {
    return (StringUtils.hasText(programmingLanguage) ? programmingLanguage : DEFAULT_PROGRAMMING_LANGUAGE);
  }

  public void setProgrammingLanguage(final String programmingLanguage) {
    this.programmingLanguage = programmingLanguage;
  }

  public Integer getReputation() {
    return ObjectUtils.defaultIfNull(reputation, DEFAULT_REPUTATION);
  }

  public void setReputation(final Integer reputation) {
    this.reputation = reputation;
  }

  @Override
  public String toString() {
    return String.format("%1$s programs in '%2$s.", getName(), getProgrammingLanguage());
  }

}
