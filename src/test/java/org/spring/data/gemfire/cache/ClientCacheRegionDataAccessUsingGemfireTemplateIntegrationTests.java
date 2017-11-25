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

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.Scope;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionShortcutWrapper;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests demonstrating the use of Spring Data GemFire's {@link GemfireTemplate} to access
 * a Pivotal GemFire client {@link Region} using various {@link ClientRegionShortcut ClientRegionShortcuts}.
 *
 * Gfsh setup:
 *
 * start locator --name=LocatorOne
 * start server --name=ServerOne --log-level=config
 * list members
 * describe member --name=ServerOne
 * list regions
 * create region --name=Example --type=PARTITION
 * list regions
 * describe region --name=/Example
 * query --query="SELECT * FROM /Example"
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientRegionShortcut
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see <a href="https://stackoverflow.com/questions/47455381/gemfiretemplate-with-clientregionshortcut-caching-proxy-heap-lru-doesnt-cache-l">GemfireTemplate with ClientRegionShortcut.CACHING_PROXY_HEAP_LRU doesn't cache locally</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@ActiveProfiles("SpringDataGemFire")
@SuppressWarnings("unused")
public class ClientCacheRegionDataAccessUsingGemfireTemplateIntegrationTests {

  private static final ClientRegionShortcut CLIENT_REGION_SHORTCUT = ClientRegionShortcut.CACHING_PROXY_HEAP_LRU;

  @Autowired
  private GemfireTemplate exampleTemplate;

  @Resource(name = "Example")
  private Region<Object, Object> example;

  @Before
  public void setup() {

    assertThat(this.example).isNotNull();
    assertThat(this.example.getName()).isEqualTo("Example");
    assertThat(this.example.getFullPath()).isEqualTo(GemfireUtils.toRegionPath(this.example.getName()));
    assertThat(this.example.getAttributes()).isNotNull();
    assertThat(this.example.getAttributes().getDataPolicy())
      .isEqualTo(ClientRegionShortcutWrapper.valueOf(CLIENT_REGION_SHORTCUT).getDataPolicy());
    assertThat(this.example.getAttributes().getScope()).isEqualTo(Scope.LOCAL);
  }

  @After
  public void tearDown() {
    this.example.removeAll(this.example.keySet());
  }

  private boolean isServerRequired() {
    return !CLIENT_REGION_SHORTCUT.name().contains("LOCAL");
  }

  @Test
  public void regionSizeIsCorrect() {

    System.err.printf("Size on client before [%d]%n", this.example.size());
    assertThat(this.example).hasSize(0);

    if (isServerRequired()) {
      System.err.printf("Size on server before [%d]%n", this.example.sizeOnServer());
      assertThat(this.example.sizeOnServer()).isEqualTo(0);
    }

    assertThat(this.exampleTemplate.put("testKey", "testValue")).isNull();

    System.err.printf("Size on client after [%d]%n", this.example.size());
    assertThat(this.example).hasSize(1);

    if (isServerRequired()) {
      System.err.printf("Size on server after [%d]%n", this.example.sizeOnServer());
      assertThat(this.example.sizeOnServer()).isEqualTo(1);
    }
  }

  @Configuration
  static class TestConfiguration {

    @Bean
    GemfireTemplate exampleTemplate(GemFireCache gemfireCache) {
      return new GemfireTemplate(gemfireCache.getRegion("/Example"));
    }
  }

  @ClientCacheApplication
  @Profile("SpringDataGemFire")
  static class SdgAnnotationBasedConfiguration {

    @Bean("Example")
    public ClientRegionFactoryBean<Object, Object> clientRegion(GemFireCache gemfireCache) {

      ClientRegionFactoryBean<Object, Object> clientRegion = new ClientRegionFactoryBean<>();

      clientRegion.setCache(gemfireCache);
      clientRegion.setClose(false);
      clientRegion.setShortcut(CLIENT_REGION_SHORTCUT);

      return clientRegion;
    }
  }

  @Configuration
  @Profile("GemFireApi")
  // TODO: Do not ever use GemFire's API for configuration in a Spring context!
  // While is this may work for this test, it is not 100% guaranteed to work in a Spring context,
  // particularly for more complex configurations!
  static class GemFireApiConfiguration {

    @Bean
    GemFireCache gemfireCache() {

      return new ClientCacheFactory()
        .set("log-level", "config")
        .create();
    }

    @Bean("Example")
    Region<Object, Object> exampleRegion(GemFireCache gemfireCache) {

      ClientRegionFactory<Object, Object> exampleRegionFactory =
        ((ClientCache) gemfireCache).createClientRegionFactory(CLIENT_REGION_SHORTCUT);

      return exampleRegionFactory.create("Example");
    }
  }
}
