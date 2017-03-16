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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.query.CqEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.app.beans.Address;
import org.spring.data.gemfire.app.beans.State;
import org.springframework.data.gemfire.listener.ContinuousQueryListener;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The ClientCacheContinuousQueriesTest class is a test suite of test cases testing the functionality of GemFire
 * Continuous Queries in the context of a GemFire ClientCache.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.data.gemfire.listener.ContinuousQueryListener
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.Region
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class ClientCacheContinuousQueriesTest {

  private static final long EXPECTED_COUNT = 6;

  private static final AtomicLong COUNTER = new AtomicLong(0l);

  @Resource(name = "Example")
  private Region<String, Address> example;

  protected Address createAddress(final String street, final String city, final State state, final String zipCode) {
    return new Address(street, city, state, zipCode);
  }

  @Test(timeout = 120000)
  public void testCqEvents() {
    example.put("jonDoe", createAddress("100 Main St.", "Portland", State.OREGON, "97205"));
    example.put("jackHandy", createAddress("6900 Beaver St.", "Missoula", State.MONTANA, "12345"));
    example.put("janeDoe", createAddress("100 Main St.", "Portland", State.OREGON, "97205"));
    example.put("cookieDoe", createAddress("100 Main St.", "Portland", State.OREGON, "97205"));
    example.put("jackBlack", createAddress("101 Turner Rd.", "Dubuque", State.IOWA, "53001"));
    example.put("sandyHandy", createAddress("6900 Beaver St.", "Missoula", State.MONTANA, "12345"));
    example.put("pieDoe", createAddress("100 Main St.", "Portland", State.OREGON, "97205"));
    example.put("randyHandy", createAddress("2424 Angle St.", "Las Angeles", State.CALIFORNIA, "54321"));
    example.put("sourDoe", createAddress("100 Main St.", "Portland", State.OREGON, "97205"));
    example.put("joeBlow", createAddress("6969 Downunder St.", "Dallas", State.TEXAS, "98765"));
    example.put("jonDoe", createAddress("4040 Times Ave.", "Portland", State.OREGON, "97205"));
    example.put("imaPigg", createAddress("1234 Farmington Ave.", "Denver", State.COLORADO, "345678"));

    while (COUNTER.get() < EXPECTED_COUNT) {
      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
      }
      catch (InterruptedException ignore) {
      }
    }

    assertEquals(EXPECTED_COUNT, COUNTER.get());
  }

  @Component
  public static final class TestContinuousQueryListener implements ContinuousQueryListener {

    @Resource(name = "Example")
    private Region<String, Address> example;

    @Override
    public void onEvent(final CqEvent event) {
      COUNTER.incrementAndGet();
      System.out.printf("Region (%1$s): op(%2$s), query-op(%3$s), size(%4$d) - %5$s = %6$s%n",
        example.getName(), event.getBaseOperation(), event.getQueryOperation(), example.size(),
          event.getKey(), event.getNewValue());
    }
  }

}
