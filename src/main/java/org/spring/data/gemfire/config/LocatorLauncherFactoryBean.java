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

package org.spring.data.gemfire.config;

import java.util.Properties;

import com.gemstone.gemfire.distributed.LocatorLauncher;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The LocatorLauncherFactoryBean class is a Spring FactoryBean for constructing, configuring and initializing a
 * GemFire Locator.
 *
 * @author John Blum
 * @see com.gemstone.gemfire.distributed.LocatorLauncher
 * @see org.springframework.beans.factory.FactoryBean
 * @see org.springframework.beans.factory.InitializingBean
 * @since 7.0.1
 */
@SuppressWarnings("unused")
public class LocatorLauncherFactoryBean implements FactoryBean<LocatorLauncher>, InitializingBean {

  private int port;

  private LocatorLauncher locator;

  private Properties gemfireProperties;

  private String bindAddress;
  private String hostnameForClients;
  private String memberName;

  @Override
  public LocatorLauncher getObject() throws Exception {
    Assert.state(locator != null, "The GemFire Locator was not properly initialized!");
    return locator;
  }

  @Override
  public Class<?> getObjectType() {
    return LocatorLauncher.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public final void setBindAddress(final String bindAddress) {
    this.bindAddress = bindAddress;
  }

  protected String getBindAddress() {
    return bindAddress;
  }

  public void setGemfireProperties(final Properties gemfireProperties) {
    this.gemfireProperties = gemfireProperties;
  }

  protected Properties getGemfireProperties() {
    return (gemfireProperties != null ? gemfireProperties : new Properties());
  }

  public final void setMemberName(final String memberName) {
    Assert.isTrue(StringUtils.hasText(memberName), "The GemFire Locator member name must be specified!");
    this.memberName = memberName;
  }

  protected String getMemberName() {
    return memberName;
  }

  public final void setPort(final int port) {
    this.port = port;
  }

  protected int getPort() {
    return port;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    setGemFireSystemProperties();

    final LocatorLauncher locator = new LocatorLauncher.Builder()
      .setBindAddress(getBindAddress())
      .setMemberName(getMemberName())
      .setPort(getPort())
      .build();

    this.locator = locator;
  }

  private void setGemFireSystemProperties() {
    for (String propertyName : getGemfireProperties().stringPropertyNames()) {
      String propertyValue = getGemfireProperties().getProperty(propertyName);
      System.out.printf("%1$s = %2$s%n", propertyName, propertyValue);
      System.setProperty(DistributionConfig.GEMFIRE_PREFIX + propertyName, propertyValue);
    }
  }

}
