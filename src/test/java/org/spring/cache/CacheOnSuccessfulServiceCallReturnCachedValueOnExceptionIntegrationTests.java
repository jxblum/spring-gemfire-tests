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

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration test testing the use of Spring' Cache Abstraction in the context of
 * a {@link Cacheable @Cacheable} service method failure.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.cache.Cache
 * @see org.springframework.cache.CacheManager
 * @see org.springframework.cache.annotation.CachePut
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see <a href="https://stackoverflow.com/questions/47480629/caching-in-spring-when-service-is-unavailable">caching in spring when service is unavailable</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CacheOnSuccessfulServiceCallReturnCachedValueOnExceptionIntegrationTests {

  @Resource(name = "StockQuotes")
  private Cache cachedStockQuotes;

  @Autowired
  private StockQuoteClient stockQuoteClient;

  @Before
  public void setup() {
    assertThat(this.cachedStockQuotes).isNotNull();
    assertThat(this.stockQuoteClient).isNotNull();
  }

  @Test
  public void stockQuoteLookups() {

    Double previousStockQuote = null;

    for (int callCount = 1; callCount <= 100; callCount++) {

      Double newStockQuote = this.stockQuoteClient.lookupQuote("VMW");

      if (callCount % 2 == 1) {
        assertThat(newStockQuote).isNotNaN();
        previousStockQuote = newStockQuote;
      }
      else {
        assertThat(newStockQuote).isEqualTo(previousStockQuote);
      }
    }
  }

  @Configuration
  @EnableCaching
  @SuppressWarnings("unused")
  static class TestConfiguration {

    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager();
    }

    @Bean("StockQuotes")
    Cache stockQuotes() {
      return cacheManager().getCache("StockQuotes");
    }

    @Bean
    StockQuoteService stockQuoteService() {
      return new StockQuoteService();
    }

    @Bean
    StockQuoteClient stockQuoteClient() {
      return new StockQuoteClient(stockQuoteService(), stockQuotes());
    }
  }

  @Component
  static class StockQuoteClient {

    private final Cache cachedStockQuotes;

    private final StockQuoteService stockQuoteService;

    StockQuoteClient(StockQuoteService stockQuoteService, @Qualifier("StockQuotes") Cache stockQuotes) {

      this.stockQuoteService = Optional.ofNullable(stockQuoteService)
        .orElseThrow(() -> new IllegalArgumentException("StockQuoteService is required"));

      this.cachedStockQuotes = Optional.ofNullable(stockQuotes)
        .orElseThrow(() -> new IllegalArgumentException("CachedStockQuotes are required"));
    }

    public Double lookupQuote(String tickerSymbol) {

      try {
        return this.stockQuoteService.lookupQuote(tickerSymbol);
      }
      catch (Exception ignore) {
        return Optional.ofNullable(this.cachedStockQuotes.get(tickerSymbol, Double.class))
          .orElse(Double.NaN);
      }
    }
  }

  @Service
  static class StockQuoteService {

    private final AtomicInteger callCount = new AtomicInteger(0);

    private final Random stockQuoteGenerator = new Random(System.currentTimeMillis());

    @CachePut("StockQuotes")
    @SuppressWarnings("unused")
    public Double lookupQuote(String tickerSymbol) {

      if (callCount.incrementAndGet() % 2 == 0) {
        throw new IllegalStateException("StockQuoteService is down");
      }

      return (double) stockQuoteGenerator.nextInt(100);
    }
  }
}
