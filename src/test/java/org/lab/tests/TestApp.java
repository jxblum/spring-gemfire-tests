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

package org.lab.tests;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.pivotal.gemfire.cache.client.ClientCacheFunctionExecutionWithPdxTest;

/**
 * The TestApp class is a Java class with a main method for testing simple expressions and/or statements in Java.
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class TestApp {

  public static void main(final String... args) {
    printClassNameAsPath();
  }

  protected static void printClassNameAsPath() {
    System.out.printf("%1$s", ClientCacheFunctionExecutionWithPdxTest.class.getName().replaceAll("\\.",
      File.separator).concat("-context.xml"));
  }
  protected static void printRegexPatternMatches() {
    Pattern regex = Pattern.compile("org\\.pivotal\\.gemfire\\.cache\\.client\\.ClientCacheFunctionExecutionWithPdxTest\\$Test.+");

    System.out.printf("%1$s %2$s%n", ClientCacheFunctionExecutionWithPdxTest.TestDomainClass.class.getName(),
      regex.matcher(ClientCacheFunctionExecutionWithPdxTest.TestDomainClass.class.getName()).matches() ? "matches"
        : "does not match");
    System.out.printf("%1$s %2$s%n", ClientCacheFunctionExecutionWithPdxTest.TestEnum.class.getName(),
      regex.matcher(ClientCacheFunctionExecutionWithPdxTest.TestEnum.class.getName()).matches() ? "matches"
        : "does not match");
  }

  protected static void printStringReplacements() {
    System.out.printf("SELECT * FROM /Programmers x WHERE x.programmingLanguage = $1%n"
      .replaceAll("(?<=\\/)\\w+", "/Users/Programmers"));
    System.out.printf("/%1$s.class%n", TestApp.class.getName().replace('.', File.separatorChar));
    System.out.println(String.format("SELECT * FROM %s WHERE birthdate >= DATE '%s' AND c.birthdate =< DATE '%s' ",
      "/Customer", "1958-01-01", "1966-01-01"));
  }

  protected static void printStringSplit() {
    System.out.printf("%1$s%n", Arrays.toString("0123456789".split("\\d")));
  }

}
