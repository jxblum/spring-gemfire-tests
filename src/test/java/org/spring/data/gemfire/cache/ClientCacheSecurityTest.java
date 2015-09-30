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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The ClientCacheSecurityTest class is a test suite of test cases testing the interaction between a GemFire Cache
 * client and server using SSL to secure GemFire data transport over the network.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see com.gemstone.gemfire.cache.Region
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("all")
public class ClientCacheSecurityTest {

  protected static final String KEY = "TestKey";
  protected static final String EXPECTED_VALUE = "TestValue";

  @Resource(name = "Example")
  private Region<Object, Object> example;

  @Test
  public void exampleRegionTestKeyValue() {
    assertThat(String.valueOf(example.get(KEY)), is(equalTo(EXPECTED_VALUE)));
  }

}
