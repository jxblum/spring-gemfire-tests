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
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.spring.cache.CachingWithRedisIntegrationTest.CachingWithRedisConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

/**
 * Integration tests testing Spring's Cache Abstraction using Spring Data Redis auto-configured with Spring Boot.
 *
 * To run this test, first start a Redis Server on localhost listening on the default port, 6379.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see <a href="http://stackoverflow.com/questions/41647382/enabling-redis-cache-in-spring-boot">Enabling Redis cache in spring boot</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("auto")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = CachingWithRedisConfiguration.class)
@SuppressWarnings("unused")
public class CachingWithRedisIntegrationTest {

  protected static final int REDIS_PORT = 6379;
  protected static final String REDIS_HOST = "localhost";

  private AtomicBoolean setup = new AtomicBoolean(false);

  @Autowired
  private MathService mathService;

  @Autowired(required = false)
  private RedisTemplate<Object, Object> redisTemplate;

  @Before
  public void setup() {
    if (redisTemplate != null && !setup.getAndSet(true)) {
      redisTemplate.delete(Arrays.asList(0L, 1L, 2L, 4L, 8L));
    }
  }

  @Test
  public void firstCacheMisses() {
    assertThat(mathService.factorial(0L)).isEqualTo(1L);
    assertThat(mathService.wasCacheMiss()).isTrue();
    assertThat(mathService.factorial(1L)).isEqualTo(1L);
    assertThat(mathService.wasCacheMiss()).isTrue();
    assertThat(mathService.factorial(2L)).isEqualTo(2L);
    assertThat(mathService.wasCacheMiss()).isTrue();
    assertThat(mathService.factorial(4L)).isEqualTo(24L);
    assertThat(mathService.wasCacheMiss()).isTrue();
    assertThat(mathService.factorial(8L)).isEqualTo(40320L);
    assertThat(mathService.wasCacheMiss()).isTrue();
  }

  @Test
  public void thenCacheHits() {
    assertThat(mathService.factorial(0L)).isEqualTo(1L);
    assertThat(mathService.wasCacheMiss()).isFalse();
    assertThat(mathService.factorial(1L)).isEqualTo(1L);
    assertThat(mathService.wasCacheMiss()).isFalse();
    assertThat(mathService.factorial(2L)).isEqualTo(2L);
    assertThat(mathService.wasCacheMiss()).isFalse();
    assertThat(mathService.factorial(4L)).isEqualTo(24L);
    assertThat(mathService.wasCacheMiss()).isFalse();
    assertThat(mathService.factorial(8L)).isEqualTo(40320L);
    assertThat(mathService.wasCacheMiss()).isFalse();
  }

  interface MathService {
    boolean wasCacheMiss();
    long factorial(long number);
  }

  @EnableCaching
  @SpringBootConfiguration
  @Import({ AutoRedisConfiguration.class, CustomRedisConfiguration.class })
  static class CachingWithRedisConfiguration {

    @Bean
    MathService mathService() {
      return new MathService() {
        private final AtomicBoolean cacheMiss = new AtomicBoolean(false);

        @Override
        public boolean wasCacheMiss() {
          return cacheMiss.getAndSet(false);
        }

        @Override
        @Cacheable(cacheNames = "Factorials")
        public long factorial(long number) {
          cacheMiss.set(true);

          Assert.isTrue(number >= 0L, String.format("Number [%d] must be greater than equal to 0", number));

          if (number <= 2L) {
            return (number < 2L ? 1L : 2L);
          }

          long result = number;

          while (--number > 1) {
            result *= number;
          }

          return result;
        }
      };
    }

    @Bean
    @Profile("none")
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager();
    }
  }

  @Profile("auto")
  @EnableAutoConfiguration
  @SpringBootConfiguration
  static class AutoRedisConfiguration {

    @PostConstruct
    public void afterPropertiesSet() {
      System.out.println("AUTO");
    }

    @Bean
    static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
      PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer =
        new PropertySourcesPlaceholderConfigurer();
      propertySourcesPlaceholderConfigurer.setProperties(redisProperties());
      return propertySourcesPlaceholderConfigurer;
    }

    static Properties redisProperties() {
      Properties redisProperties = new Properties();

      redisProperties.setProperty("spring.cache.type", "redis");
      redisProperties.setProperty("spring.redis.host", REDIS_HOST);
      redisProperties.setProperty("spring.redis.port", String.valueOf(REDIS_PORT));

      return redisProperties;
    }
  }

  @Profile("CUSTOM")
  @SpringBootConfiguration
  static class CustomRedisConfiguration {

    @PostConstruct
    public void afterPropertiesSet() {
      System.out.println("CUSTOM");
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
      JedisConnectionFactory factory = new JedisConnectionFactory();
      factory.setHostName(REDIS_HOST);
      factory.setPort(REDIS_PORT);
      factory.setUsePool(true);
      return factory;
    }

    @Bean
    RedisTemplate<Object, Object> redisTemplate() {
      RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
      redisTemplate.setConnectionFactory(jedisConnectionFactory());
      return redisTemplate;
    }

    @Bean
    CacheManager cacheManager() {
      /*
      RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate());
      cacheManager.setUsePrefix(true); // THIS IS NEEDED!
      return cacheManager;
      */
      return null;
    }
  }
}
