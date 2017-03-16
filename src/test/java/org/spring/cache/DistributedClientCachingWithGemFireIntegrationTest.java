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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The DistributedClientCachingWithGemFireIntegrationTest class is a test suite of test cases testing the contract
 * and functionality of Spring's Cache Abstraction with GemFire as the caching provider in the context
 * of a ClientCache, client Region PROXY that accesses a GemFire Server PARTITION Region.
 *
 * This test simulates a Cache/Region 'put' from one client (1) to one server (A) and a Cache/Region 'get' from
 * another client (2) from another server (B).
 *
 * Execute Gfsh shell script:
 *
 * start locator --name=LocatorX --port=11235 --log-level=config
 * start server --name=ServerA --server-port=12480 --log-level=config
 * start server --name=ServerB --server-port=13579 --log-level=config
 * list members
 * create region --name=Example --type=PARTITION
 * list regions
 * describe region --name=/Example
 * put --region=/Example --key=ExampleKey--value=null
 * get --region=/Example --key=ExampleKey
 * stop server --name=ServerB
 * stop server --name=ServerA
 * stop locator --name=LocatorX
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.spring.cache.AbstractSpringCacheAbstractionIntegrationTest
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration
@SuppressWarnings("unused")
public class DistributedClientCachingWithGemFireIntegrationTest {

  protected static final Object KEY = "ExampleKey";
  protected static final Object VALUE = 0xCAFEBABEl;

  @Autowired
  private CachingProxyService cachingProxyService;

  @Resource(name = "Example")
  private Region<Object, Object> example;

  @Test
  public void aCachePut() {
    assumeThat(example.containsKeyOnServer(KEY), is(equalTo(false)));
    cachingProxyService.put(KEY, VALUE);
  }

  @Test
  public void bCacheGet() {
    assertThat(example.get(KEY), is(equalTo(VALUE)));
    assertThat(cachingProxyService.get(KEY), is(equalTo(VALUE)));
  }

  @Service("cachingProxyService")
  public static class CachingProxyService {

    @Cacheable("Example")
    public Object get(Object key) {
      return null;
    }

    @CachePut(value = "Example", key = "#key")
    public Object put(Object key, Object value) {
      return value;
    }
  }

}
