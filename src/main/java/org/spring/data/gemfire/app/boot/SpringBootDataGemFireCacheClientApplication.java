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

package org.spring.data.gemfire.app.boot;

import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * The SpringBootDataGemFireCacheClientApplication class is a Spring Boot application that configures and bootstraps
 * a GemFire {@link org.apache.geode.cache.client.ClientCache} instance.
 *
 * @author John Blum
 * @see org.springframework.boot.CommandLineRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @since 1.0.0
 */
@SpringBootApplication
@ImportResource("file:///Users/jblum/pivdev/tmp/filinv-gemfire-client-config.xml")
public class SpringBootDataGemFireCacheClientApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(SpringBootDataGemFireCacheClientApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    Scanner scanner = new Scanner(System.in);
    scanner.next();
  }
}
