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

package org.pivotal.gemfire.app.main;

import java.io.File;

import org.apache.geode.distributed.LocatorLauncher;
import org.apache.geode.distributed.LocatorLauncher.Builder;

/**
 * The LocatorApp class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class NativeLocatorApp {

  protected static final String USER_HOME = System.getProperty("user.home");

  public static void main(final String[] args) {
    String workingDirectoryPathname = (args.length > 0 ? args[0] : USER_HOME.concat(File.separator).concat("tmp")
      .concat(File.separator).concat("locator"));

    File workingDirectory = new File(workingDirectoryPathname);

    assert (workingDirectory.isDirectory() || workingDirectory.mkdirs())
      : String.format("Working directory (%1$s) does not exist or could not be created!", workingDirectory);

    LocatorLauncher locator = new Builder()
      .setMemberName("MyLocator")
      .setPort(11235)
      .setWorkingDirectory(workingDirectory.getAbsolutePath())
      .set("jmx-manager", "true")
      .set("jmx-manager-start", "true")
      .set("jmx-manager-port", "1199")
      .set("log-level", "config")
      .build();

    locator.start();

    System.out.printf("Locator (%1$s) is running...%n", locator.getMemberName());

    locator.waitOnLocator();
  }

}
