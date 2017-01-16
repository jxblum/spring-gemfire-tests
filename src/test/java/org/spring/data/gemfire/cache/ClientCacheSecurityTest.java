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

package org.spring.data.gemfire.cache;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.security.Principal;
import java.util.Properties;
import javax.annotation.Resource;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheLoader;
import com.gemstone.gemfire.cache.CacheLoaderException;
import com.gemstone.gemfire.cache.LoaderHelper;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AuthInitialize;
import com.gemstone.gemfire.security.AuthenticationFailedException;
import com.gemstone.gemfire.security.Authenticator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.data.gemfire.ReplicatedRegionFactoryBean;
import org.springframework.data.gemfire.server.CacheServerFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The ClientCacheSecurityTest class is a test suite of test cases testing the interaction between a GemFire Cache
 * client and server using SSL to secure GemFire data transport over the network.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.data.gemfire.ReplicatedRegionFactoryBean
 * @see org.springframework.data.gemfire.server.CacheServerFactoryBean
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.distributed.DistributedMember
 * @see com.gemstone.gemfire.distributed.DistributedSystem
 * @see com.gemstone.gemfire.security.AuthInitialize
 * @see com.gemstone.gemfire.security.Authenticator
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("all")
public class ClientCacheSecurityTest {

  @Resource(name = "Example")
  private Region<Long, Long> example;

  @Test
  public void exampleRegionTestKeyValueIsExpected() {
    assertThat(example.get(0l), is(equalTo(1l)));
    assertThat(example.get(1l), is(equalTo(1l)));
    assertThat(example.get(2l), is(equalTo(2l)));
    assertThat(example.get(3l), is(equalTo(6l)));
    assertThat(example.get(4l), is(equalTo(24l)));
    assertThat(example.get(5l), is(equalTo(120l)));
    assertThat(example.get(6l), is(equalTo(720l)));
    assertThat(example.get(7l), is(equalTo(5040l)));
    assertThat(example.get(8l), is(equalTo(40320l)));
    assertThat(example.get(9l), is(equalTo(362880l)));
    assertThat(example.get(10l), is(equalTo(3628800l)));
  }

  protected static abstract class AuthenticationAuthorizationSecuritySupport {

    protected static final String SECURITY_USERNAME_PROPERTY = "security-username";
    protected static final String SECURITY_PASSWORD_PROPERTY = "security-password";

    protected PropertiesSetter set(String propertyName) {
      return new PropertiesSetterSupport() {
        @Override public PropertiesSetter with(final Properties source) {
          validateAuthenticationConfiguration(source, getPropertyName());
          return super.with(source);
        }
      }.set(propertyName);
    }

    protected Principal validateAuthenticated(String username, String password) {
      if (!username.equals(password)) {
        throw new AuthenticationFailedException(String.format("user [%s] authentication verification failed",
          username));
      }

      return new TestPrincipal(username);
    }

    protected String validateAuthenticationConfiguration(Properties securityConfiguration, String propertyName) {
      String propertyValue = securityConfiguration.getProperty(propertyName);

      if (!StringUtils.hasText(propertyValue)) {
        throw new AuthenticationFailedException(String.format("[%s] must be specified", propertyName));
      }

      return propertyValue;
    }
  }

  public static final class TestAuthInitialize extends AuthenticationAuthorizationSecuritySupport
      implements AuthInitialize {

    public static TestAuthInitialize create() {
      return new TestAuthInitialize();
    }

    public Properties getCredentials(Properties gemfireSecurityProperties, DistributedMember server, boolean isPeer)
        throws AuthenticationFailedException {

      Properties credentials = new Properties();

      set(SECURITY_USERNAME_PROPERTY).of(credentials).with(gemfireSecurityProperties);
      set(SECURITY_PASSWORD_PROPERTY).of(credentials).with(gemfireSecurityProperties);

      return credentials;
    }

    public void init(LogWriter systemLogger, LogWriter securityLogger) throws AuthenticationFailedException {
    }

    public void close() {
    }
  }

  public static final class TestAuthenticator extends AuthenticationAuthorizationSecuritySupport
      implements Authenticator {

    public static TestAuthenticator create() {
      return new TestAuthenticator();
    }

    public void init(Properties gemfireSecurityProperties, LogWriter systemLogger, LogWriter securityLogger)
        throws AuthenticationFailedException {

      securityLogger.info(String.format("GemFire system security configuration was [%s]", gemfireSecurityProperties));
    }

    public Principal authenticate(Properties credentials, DistributedMember member)
        throws AuthenticationFailedException {

      String username = validateAuthenticationConfiguration(credentials, SECURITY_USERNAME_PROPERTY);
      String password = validateAuthenticationConfiguration(credentials, SECURITY_PASSWORD_PROPERTY);

      return validateAuthenticated(username, password);
    }

    public void close() {
    }
  }

  public static final class TestPrincipal implements Principal {

    private final String username;

    public TestPrincipal(String username) {
      Assert.hasText(username, "username must be specified");
      this.username = username;
    }

    public String getName() {
      return username;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof Principal)) {
        return false;
      }

      Principal that = (Principal) obj;

      return this.getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + getName().hashCode();
      return hashValue;
    }

    @Override
    public String toString() {
      return getName();
    }
  }

  static interface PropertiesSetter {

    PropertiesSetter set(String propertyName);

    PropertiesSetter of(Properties target);

    PropertiesSetter to(String propertyValue);

    PropertiesSetter with(Properties source);
  }

  static class PropertiesSetterSupport implements PropertiesSetter {

    private Properties target;
    private String propertyName;

    protected String getPropertyName() {
      Assert.state(StringUtils.hasText(this.propertyName), "propertyName was not initialized");
      return this.propertyName;
    }

    public PropertiesSetter set(String propertyName) {
      Assert.hasText(propertyName, String.format("propertyName [%s] must be specified", propertyName));
      this.propertyName = propertyName;
      return this;
    }

    @Override
    public PropertiesSetter of(Properties target) {
      Assert.notNull(target, String.format(
        "The target Properties in which to set property [%s] cannot be null", propertyName));
      this.target = target;
      return this;
    }

    @Override
    public PropertiesSetter to(String propertyValue) {
      this.target.setProperty(this.propertyName, propertyValue);
      return this;
    }

    @Override
    public PropertiesSetter with(Properties source) {
      Assert.notNull(source, "source Properties cannot be null");
      this.target.setProperty(this.propertyName, source.getProperty(this.propertyName));
      return this;
    }
  }

  @Configuration
  //@SpringBootApplication
  public static class SpringGemFireSecureServerApplication {

    private static final boolean USE_SSL_PROPERTIES = true;
    private static final boolean USE_SERVER_SSL_PROPERTIES = !USE_SSL_PROPERTIES;

    private static final String KEYSTORE_LOCATION =
      "/Users/jblum/pivdev/spring-data-gemfire-tests-workspace/spring-data-gemfire-tests/etc/gemfire/security/trusted.keystore";

    private static final String KEYSTORE_PASSWORD = "s3cr3t";

    private static final String KEYSTORE_TYPE = "JKS";

    static {
      if (USE_SSL_PROPERTIES) {
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);
        System.setProperty("javax.net.ssl.keyStoreType", KEYSTORE_TYPE);
        System.setProperty("javax.net.ssl.trustStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.trustStorePassword", KEYSTORE_PASSWORD);
      }
    }
    public static void main(final String[] args) {
      SpringApplication.run(SpringGemFireSecureServerApplication.class, args);
    }

    @Bean
    Properties serverProperties() {
      Properties serverProperties = new Properties();

      serverProperties.setProperty("gemfire.security.ssl.keystore", KEYSTORE_LOCATION);
      serverProperties.setProperty("gemfire.security.ssl.keystore.password", KEYSTORE_PASSWORD);
      serverProperties.setProperty("gemfire.security.ssl.keystore.type", KEYSTORE_TYPE);
      serverProperties.setProperty("gemfire.security.ssl.truststore", KEYSTORE_LOCATION);
      serverProperties.setProperty("gemfire.security.ssl.truststore.password", KEYSTORE_PASSWORD);

      return serverProperties;
    }

    @Bean
    PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer(
        @Qualifier("serverProperties") Properties serverProperties) {

      PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

      propertyPlaceholderConfigurer.setProperties(serverProperties);

      return propertyPlaceholderConfigurer;
    }

    @Bean
    Properties gemfireProperties(@Value("${gemfire.log.level:config}") String logLevel,
        @Value("${gemfire.locator.host-port:localhost[11235]}") String locatorHostPort,
        @Value("${gemfire.manager.port:1199}") String jmxManagerPort,
        @Value("${gemfire.security.ssl.keystore}") String keystoreLocation,
        @Value("${gemfire.security.ssl.keystore.password}") String keystorePassword,
        @Value("${gemfire.security.ssl.keystore.type}") String keystoreType,
        @Value("${gemfire.security.ssl.truststore}") String truststoreLocation,
        @Value("${gemfire.security.ssl.truststore.password}") String truststorePassword) {

      Properties gemfireProperties = new Properties();

      gemfireProperties.setProperty("name", SpringGemFireSecureServerApplication.class.getSimpleName());
      gemfireProperties.setProperty("mcast-port", "0");
      gemfireProperties.setProperty("log-level", logLevel);
      gemfireProperties.setProperty("start-locator", locatorHostPort);
      gemfireProperties.setProperty("jmx-manager", "true");
      gemfireProperties.setProperty("jmx-manager-port", jmxManagerPort);
      gemfireProperties.setProperty("jmx-manager-ssl-enabled", "false");
      gemfireProperties.setProperty("jmx-manager-start", "true");
      gemfireProperties.setProperty("security-client-authenticator",
        TestAuthenticator.class.getName().concat(".create"));

      configureSslProperties(gemfireProperties);

      configureServerSslProperties(gemfireProperties, keystoreLocation, keystorePassword, keystoreType,
        truststoreLocation, truststorePassword);

      return gemfireProperties;
    }

    void configureServerSslProperties(Properties gemfireProperties, String keystoreLocation, String keystorePassword,
                                      String keystoreType, String truststoreLocation, String truststorePassword)
    {
      if (USE_SERVER_SSL_PROPERTIES) {
        gemfireProperties.setProperty("server-ssl-enabled", "true");
        gemfireProperties.setProperty("server-ssl-ciphers", "any");
        gemfireProperties.setProperty("server-ssl-protocols", "any");
        gemfireProperties.setProperty("server-ssl-require-authentication", "true");
        gemfireProperties.setProperty("server-ssl-keystore", keystoreLocation);
        gemfireProperties.setProperty("server-ssl-keystore-password", keystorePassword);
        gemfireProperties.setProperty("server-ssl-keystore-type", keystoreType);
        gemfireProperties.setProperty("server-ssl-truststore", truststoreLocation);
        gemfireProperties.setProperty("server-ssl-truststore-password", truststorePassword);
      }
    }

    void configureSslProperties(Properties gemfireProperties) {
      if (USE_SSL_PROPERTIES) {
        gemfireProperties.setProperty("ssl-enabled", "true");
        gemfireProperties.setProperty("ssl-ciphers", "any");
        gemfireProperties.setProperty("ssl-protocols", "any");
        gemfireProperties.setProperty("ssl-require-authentication", "true");
      }
    }

    @Bean
    CacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties) {
      CacheFactoryBean gemfireCache = new CacheFactoryBean();

      gemfireCache.setClose(true);
      gemfireCache.setProperties(gemfireProperties);

      return gemfireCache;
    }

    @Bean
    CacheServerFactoryBean gemfireCacheServer(Cache gemfireCache,
        @Value("${gemfire.cache.server.bind-address:localhost}") String bindAddress,
        @Value("${gemfire.cache.server.hostname-for-clients:localhost}") String hostnameForClients,
        @Value("${gemfire.cache.server.port:12480}") int port,
        @Value("${gemfire.cache.server.max-connections:10}") int maxConnections) {

      CacheServerFactoryBean gemfireCacheServer = new CacheServerFactoryBean();

      gemfireCacheServer.setAutoStartup(true);
      gemfireCacheServer.setCache(gemfireCache);
      gemfireCacheServer.setBindAddress(bindAddress);
      gemfireCacheServer.setHostNameForClients(hostnameForClients);
      gemfireCacheServer.setPort(port);
      gemfireCacheServer.setMaxConnections(maxConnections);

      return gemfireCacheServer;
    }

    @Bean(name = "Example")
    ReplicatedRegionFactoryBean<Long, Long> exampleRegion(Cache gemfireCache,
        RegionAttributes<Long, Long> exampleRegionAttributes) {

      ReplicatedRegionFactoryBean<Long, Long> exampleRegion = new ReplicatedRegionFactoryBean<>();

      exampleRegion.setAttributes(exampleRegionAttributes);
      exampleRegion.setCache(gemfireCache);
      exampleRegion.setClose(false);
      exampleRegion.setName("Example");
      exampleRegion.setPersistent(false);

      return exampleRegion;
    }

    @Bean
    RegionAttributesFactoryBean exampleRegionAttributes() {
      RegionAttributesFactoryBean exampleRegionAttributes = new RegionAttributesFactoryBean();

      exampleRegionAttributes.setCacheLoader(exampleRegionCacheLoader());
      exampleRegionAttributes.setKeyConstraint(Long.class);
      exampleRegionAttributes.setValueConstraint(Long.class);

      return exampleRegionAttributes;
    }

    @Bean
    CacheLoader exampleRegionCacheLoader() {
      return new CacheLoader<Long, Long>() {
        public Long load(final LoaderHelper<Long, Long> helper) throws CacheLoaderException {
          long number = helper.getKey();

          Assert.isTrue(number >= 0L, String.format("factorial(%d) is invalid", number));

          if (number < 3L) {
            return (number < 2L ? 1L : 2L);
          }

          long result = number;

          while (--number > 1) {
            result *= number;
          }

          return result;
        }

        public void close() {
        }
      };
    }
  }
}
