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

package org.spring.data.gemfire.app.main.redis;

import java.util.Scanner;

import org.apache.geode.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.gemfire.config.annotation.EnableRedisServer;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;

@SpringBootApplication
@PeerCacheApplication
@EnableRedisServer(bindAddress = "localhost")
@SuppressWarnings("unused")
public class SpringGemFireRedisServer implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(SpringGemFireRedisServer.class, args);
  }

  @Autowired
  private Cache gemfireCache;

  @Override
  public void run(String... args) throws Exception {
    System.out.println("Press ENTER to exit");
    Scanner in = new Scanner(System.in);
    in.nextLine();
  }
}
