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

package org.spring.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aopalliance.aop.Advice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.cache.ProgrammaticCachingWithSpringIntegrationTests.TestConfigurationTwo;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.cache.interceptor.CacheProxyFactoryBean;
import org.springframework.cache.interceptor.CacheableOperation;
import org.springframework.cache.interceptor.NameMatchCacheOperationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

/**
 * The {@link ProgrammaticCachingWithSpringIntegrationTests} class is a test suite of test cases testing
 * the programmatic creation of caching proxies with Spring's Cache Abstraction.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.cache.interceptor.CacheInterceptor
 * @see org.springframework.cache.interceptor.CacheProxyFactoryBean
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see <a href="https://stackoverflow.com/questions/47665485/how-can-i-manually-add-a-spring-cache-interceptor-using-java-config/47665877?noredirect=1#comment82334024_47665877">How can I manually add a Spring CacheInterceptor using Java Config?</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfigurationTwo.class)
@SuppressWarnings("unused")
public class ProgrammaticCachingWithSpringIntegrationTests {

  @Autowired
  private Greeter greeter;

  @Test
  public void cacheOperationsAreSuccessful() {

    assertThat(this.greeter.isCacheMiss()).isFalse();
    assertThat(this.greeter.greet("John")).isEqualTo("Hello John!");
    assertThat(this.greeter.isCacheMiss()).isTrue();
    assertThat(this.greeter.greet("Jon")).isEqualTo("Hello Jon!");
    assertThat(this.greeter.isCacheMiss()).isTrue();
    assertThat(this.greeter.greet("John")).isEqualTo("Hello John!");
    assertThat(this.greeter.isCacheMiss()).isFalse();
  }

  @Configuration
  @EnableCaching
  static abstract class BaseTestConfiguration {

    @Bean
    ConcurrentMapCacheManager cacheManager() {
      return new ConcurrentMapCacheManager("Greetings");
    }

    CacheOperationSource newCacheOperationSource(CacheOperation... cacheOperations) {

      NameMatchCacheOperationSource cacheOperationSource = new NameMatchCacheOperationSource();

      cacheOperationSource.addCacheMethod("greet", Arrays.asList(cacheOperations));

      return cacheOperationSource;
    }

    CacheableOperation newCacheableOperation() {

      CacheableOperation.Builder builder = new CacheableOperation.Builder();

      builder.setCacheManager("cacheManager");
      builder.setCacheName("Greetings");

      return builder.build();
    }
  }

  static class TestConfigurationOne extends BaseTestConfiguration {

    @Bean
    CacheProxyFactoryBean greeter() {

      CacheProxyFactoryBean cacheProxyFactory = new SmartCacheProxyFactoryBean();

      cacheProxyFactory.setCacheOperationSources(newCacheOperationSource(newCacheableOperation()));
      cacheProxyFactory.setTarget(new NameGreeter());

      return cacheProxyFactory;
    }
  }

  static class SmartCacheProxyFactoryBean extends CacheProxyFactoryBean
      implements BeanFactoryAware, SmartInitializingSingleton {

    private BeanFactory beanFactory;

    private SmartInitializingSingleton mainInterceptor;

    @Override
    @SuppressWarnings("all")
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
      this.beanFactory = beanFactory;
    }

    protected BeanFactory getBeanFactory() {
      Assert.state(this.beanFactory != null, "BeanFactory was not properly configured");
      return this.beanFactory;
    }

    protected Optional<SmartInitializingSingleton> getMainInterceptor() {
      return Optional.ofNullable(this.mainInterceptor);
    }

    @Override
    public void afterSingletonsInstantiated() {
      getMainInterceptor().ifPresent(SmartInitializingSingleton::afterSingletonsInstantiated);
    }

    @Override
    @SuppressWarnings("all")
    protected Object createMainInterceptor() {

      Object mainInterceptor = super.createMainInterceptor();

      if (mainInterceptor instanceof Advisor) {

        Advice advice = ((Advisor) mainInterceptor).getAdvice();

        if (advice instanceof SmartInitializingSingleton) {

          this.mainInterceptor = (SmartInitializingSingleton) advice;

          if (this.mainInterceptor instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.mainInterceptor).setBeanFactory(getBeanFactory());
          }
        }
      }

      return mainInterceptor;
    }
  }

  static class TestConfigurationTwo extends BaseTestConfiguration {

    @Bean
    CacheInterceptor cacheInterceptor(CacheManager cacheManager) {

      CacheInterceptor cacheInterceptor = new CacheInterceptor();

      cacheInterceptor.setCacheManager(cacheManager);
      cacheInterceptor.setCacheOperationSources(newCacheOperationSource(newCacheableOperation()));

      return cacheInterceptor;
    }

    @Bean
    Object greeter(ConfigurableListableBeanFactory beanFactory, CacheInterceptor cacheInterceptor) {

      ProxyFactory proxyFactory = new ProxyFactory();

      proxyFactory.addAdvisor(new DefaultPointcutAdvisor(cacheInterceptor));
      //proxyFactory.addAdvisor(new DefaultPointcutAdvisor(cacheInterceptor()));
      proxyFactory.setInterfaces(Greeter.class);
      proxyFactory.setTarget(new NameGreeter());

      return proxyFactory.getProxy(beanFactory.getBeanClassLoader());
    }
  }

  interface CacheableService {

    boolean isCacheHit();

    boolean isCacheMiss();

  }

  static abstract class AbstractCacheableService implements CacheableService {

    private final AtomicBoolean cacheMiss = new AtomicBoolean(false);

    public boolean isCacheHit() {
      return !isCacheMiss();
    }

    public boolean isCacheMiss() {
      return this.cacheMiss.getAndSet(false);
    }

    protected void setCacheMiss() {
      this.cacheMiss.set(true);
    }
  }

  interface Greeter extends CacheableService {

    default String greet(String name) {
      ((AbstractCacheableService) this).setCacheMiss();
      return String.format("Hello %s!", name);
    }
  }

  interface HelloWorldGreeter extends Greeter {

    default String greet() {
      return greet("World");
    }
  }

  static class NameGreeter extends AbstractCacheableService implements HelloWorldGreeter {
  }
}
