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

package org.spring.data.gemfire;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.gemstone.gemfire.distributed.ServerLauncher;
import com.gemstone.gemfire.distributed.ServerLauncher.ServerState;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;

import org.codeprimate.io.FileSystemUtils;
import org.codeprimate.lang.StringUtils;
import org.codeprimate.process.support.ProcessUtils;
import org.spring.data.gemfire.app.main.SpringGemFireCacheServerLauncher;
import org.spring.data.gemfire.support.DistributedSystemConfigurationUtils;
import org.springframework.util.Assert;

/**
 * The AbstractGemFireIntegrationTest class is an abstract base class encapsulating functionality common to writing
 * integration tests with Spring and GemFire as well as between client and servers.
 *
 * @author John Blum
 * @see org.spring.data.gemfire.AbstractGemFireTest
 * @see org.spring.data.gemfire.app.main.SpringGemFireCacheServerLauncher
 * @see com.gemstone.gemfire.distributed.ServerLauncher
 * @see com.gemstone.gemfire.distributed.internal.DistributionConfig
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractGemFireIntegrationTest extends AbstractGemFireTest {

  private static final int DEFAULT_LOCATOR_PORT = 11235;
  private static final int DEFAULT_SERVER_PORT = 12480;

  private static final long DEFAULT_WAIT_TIMEOUT_MILLISECONDS = TimeUnit.SECONDS.toMillis(60);

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SS");

  protected static ServerLauncher startGemFireServer(final String cacheXmlPathname, final Properties gemfireProperties)
    throws IOException
  {
    return startGemFireServer(DEFAULT_WAIT_TIMEOUT_MILLISECONDS, cacheXmlPathname, gemfireProperties);
  }

  protected static ServerLauncher startGemFireServer(final long waitTimeout,
                                                     final String cacheXmlPathname,
                                                     final Properties gemfireProperties)
    throws IOException
  {
    String gemfireMemberName = gemfireProperties.getProperty(DistributionConfig.NAME_NAME);
    String serverId = DATE_FORMAT.format(Calendar.getInstance().getTime());

    gemfireMemberName = String.format("%1$s-%2$s", (StringUtils.hasText(gemfireMemberName)
      ? gemfireMemberName : ""), serverId);

    File serverWorkingDirectory = FileSystemUtils.createFile(gemfireMemberName.toLowerCase());

    Assert.isTrue(FileSystemUtils.createDirectory(serverWorkingDirectory), String.format(
      "Failed to create working directory (%1$s) in which the GemFire Server will run!", serverWorkingDirectory));

    ServerLauncher serverLauncher = buildServerLauncher(cacheXmlPathname, gemfireProperties, serverId,
      DEFAULT_SERVER_PORT, serverWorkingDirectory);

    List<String> serverCommandLine = buildServerCommandLine(serverLauncher);

    System.out.printf("Starting GemFire Server in (%1$s)...%n", serverWorkingDirectory);

    Process serverProcess = ProcessUtils.startProcess(serverCommandLine.toArray(new String[serverCommandLine.size()]),
      serverWorkingDirectory);

    readProcessStream(serverId, "ERROR", serverProcess.getErrorStream());
    readProcessStream(serverId, "OUT", serverProcess.getInputStream());
    waitOnServer(waitTimeout, serverProcess, serverWorkingDirectory);

    return serverLauncher;
  }

  private static ServerLauncher buildServerLauncher(final String cacheXmlPathname,
                                                    final Properties gemfireProperties,
                                                    final String serverId,
                                                    final int serverPort,
                                                    final File serverWorkingDirectory)
  {
    ServerLauncher.Builder serverLauncherBuilder = new ServerLauncher.Builder()
      //.setCommand(ServerLauncher.Command.START)
      .setDebug(Boolean.FALSE)
      .setForce(Boolean.FALSE)
      .setRedirectOutput(Boolean.TRUE)
      .setServerPort(serverPort)
      .setWorkingDirectory(serverWorkingDirectory.getAbsolutePath())
      .set(DistributionConfig.CACHE_XML_FILE_NAME, cacheXmlPathname);

    if (!gemfireProperties.contains(DistributionConfig.NAME_NAME)) {
      serverLauncherBuilder.setMemberName(serverId);
    }

    for (String property : gemfireProperties.stringPropertyNames()) {
      serverLauncherBuilder.set(property, gemfireProperties.getProperty(property));
    }

    return serverLauncherBuilder.build();
  }

  private static List<String> buildServerCommandLine(final ServerLauncher serverLauncher) {
    List<String> serverCommandLine = new ArrayList<>();

    serverCommandLine.add(FileSystemUtils.JAVA_EXE.getAbsolutePath());
    serverCommandLine.add("-server");
    serverCommandLine.add("-classpath");
    serverCommandLine.add(System.getProperty("java.class.path"));
    serverCommandLine.add("-Djava.awt.headless=true");
    serverCommandLine.add("-Dsun.rmi.dgc.server.gcInterval=".concat(Long.toString(Long.MAX_VALUE - 1)));

    for (String property : serverLauncher.getProperties().stringPropertyNames()) {
      serverCommandLine.add(DistributedSystemConfigurationUtils.configureDistributedSystemPropertyAsSystemProperty(
        property, serverLauncher.getProperties().getProperty(property)));
    }

    serverCommandLine.add(ServerLauncher.class.getName());
    serverCommandLine.add(ServerLauncher.Command.START.toString());
    serverCommandLine.add("--server-port=".concat(String.valueOf(serverLauncher.getServerPort())));

    return serverCommandLine;
  }

  protected static ServerState stopGemFireServer(final ServerLauncher serverLauncher) {
    return (serverLauncher != null ? serverLauncher.stop() : null);
  }

  protected static OutputStream startSpringGemFireServer(final String... springConfigLocations) throws IOException {
    return startSpringGemFireServer(DEFAULT_WAIT_TIMEOUT_MILLISECONDS, springConfigLocations);
  }

  protected static OutputStream startSpringGemFireServer(final long waitTimeout, final String... springConfigLocations)
    throws IOException
  {
    String serverId = DATE_FORMAT.format(Calendar.getInstance().getTime());

    File serverWorkingDirectory = FileSystemUtils.createFile("server-".concat(serverId));

    Assert.isTrue(FileSystemUtils.createDirectory(serverWorkingDirectory), String.format(
      "Failed to create working directory (%1$s) in which the Spring-based GemFire Server will run!",
        serverWorkingDirectory));

    String[] serverCommandLine = buildServerCommandLine(springConfigLocations);

    System.out.printf("Starting Spring GemFire Server in (%1$s)...%n", serverWorkingDirectory);

    Process serverProcess = ProcessUtils.startProcess(serverCommandLine, serverWorkingDirectory);

    readProcessStream(serverId, "ERROR", serverProcess.getErrorStream());
    readProcessStream(serverId, "OUT", serverProcess.getInputStream());
    ProcessUtils.registerProcessShutdownHook(serverProcess, "Spring GemFire Server", serverWorkingDirectory);
    waitOnServer(waitTimeout, serverProcess, serverWorkingDirectory);

    return serverProcess.getOutputStream();
  }

  private static String[] buildServerCommandLine(final String[] springConfigLocations) {
    List<String> commandLine = new ArrayList<>();

    commandLine.add(FileSystemUtils.JAVA_EXE.getAbsolutePath());
    commandLine.add("-server");
    commandLine.add("-classpath");
    commandLine.add(System.getProperty("java.class.path"));
    commandLine.add("-Djava.awt.headless=true");
    commandLine.add("-Dsun.rmi.dgc.server.gcInterval=".concat(Long.toString(Long.MAX_VALUE - 1)));
    commandLine.add(SpringGemFireCacheServerLauncher.class.getName());
    commandLine.addAll(Arrays.asList(springConfigLocations));

    return commandLine.toArray(new String[commandLine.size()]);
  }

  private static void readProcessStream(final String serverId, final String streamType, final InputStream in) {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

    new Thread(new Runnable() {
      @Override public void run() {
        try {
          for (String line = "Reading..."; line != null; line = reader.readLine()) {
            System.out.printf("[%1$s - %2$s]: %3$s%n", serverId, streamType, line);
            System.out.flush();
          }
        }
        catch (IOException ignore) {
        }
        finally {
          FileSystemUtils.close(reader);
        }
      }
    }).start();
  }

  private static void waitOnServer(final long waitTimeout,
                                   final Process serverProcess,
                                   final File serverWorkingDirectory)
  {
    File serverPidFile = new File(serverWorkingDirectory, SpringGemFireCacheServerLauncher.SERVER_PID_FILE_NAME);

    final long endTime = (System.currentTimeMillis() + waitTimeout);

    while (System.currentTimeMillis() < endTime && !serverPidFile.isFile()) {
      try {
        synchronized (serverProcess) {
          TimeUnit.MILLISECONDS.timedWait(serverProcess, 500l);
        }
      }
      catch (InterruptedException ignore) {
      }
    }
  }

  protected static void pause(final long milliseconds) {
    final long timeout = (System.currentTimeMillis() + milliseconds);

    while (System.currentTimeMillis() < timeout) {
      try {
        Thread.sleep(Math.min(milliseconds, TimeUnit.MILLISECONDS.toMillis(500)));
      }
      catch (InterruptedException ignore) {
      }
    }
  }

}
