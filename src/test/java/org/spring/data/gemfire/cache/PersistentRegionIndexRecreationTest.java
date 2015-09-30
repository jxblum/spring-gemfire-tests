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

import static org.junit.Assert.*;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.CacheLoader;
import com.gemstone.gemfire.cache.CacheLoaderException;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.LoaderHelper;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.query.QueryService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.AbstractGemFireTest;
import org.spring.data.gemfire.app.beans.Gemstone;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The PersistentRegionIndexRecreationTest class is a test suite of test cases testing the default behavior
 * of Spring Data GemFire Index management, which is to "override", or "remove" and then "re-create"
 * a named Index, on a persistent, PARTITION Region.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.IndexFactoryBean
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class PersistentRegionIndexRecreationTest extends AbstractGemFireTest {

  @Resource(name = "Gemstones")
  private Region<Long, Gemstone> gemstones;

  protected Gemstone create(final Long id, final String name) {
    return new Gemstone(id, name);
  }

  @Test
  @Repeat(2)
  @DirtiesContext
  public void testRegionConfigurationAndData() {
    assertRegionExists("Gemstones", gemstones);
    assertNotNull(gemstones.getAttributes());
    assertEquals(DataPolicy.PERSISTENT_PARTITION, gemstones.getAttributes().getDataPolicy());
    assertEquals(create(2l, "DIAMOND"), gemstones.get(2l));
    assertEquals(create(6l, "SAPPHIRE"), gemstones.get(6l));

    QueryService queryService = gemstones.getRegionService().getQueryService();

    assertNotNull(queryService);
    assertNotNull(queryService.getIndex(gemstones, "GemstoneIdx"));
  }

  public static final class GemstoneCacheLoader implements CacheLoader<Long, Gemstone> {

    private static final String[] GEMSTONE_NAMES = {
      "ALEXANDRITE", "AQUAMARINE", "DIAMOND", "OPAL", "PEARL", "RUBY", "SAPPHIRE", "SPINEL", "TOPAZ"
    };

    @Override
    public Gemstone load(final LoaderHelper<Long, Gemstone> helper) throws CacheLoaderException {
      Long key = helper.getKey();
      int index = (int) (Math.abs(key) % GEMSTONE_NAMES.length);
      return new Gemstone(key, GEMSTONE_NAMES[index]);
    }

    @Override
    public void close() {
    }
  }

}
