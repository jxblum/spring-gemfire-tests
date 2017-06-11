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
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;
import org.codeprimate.lang.ClassUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.app.beans.Gemstone;
import org.spring.data.gemfire.app.beans.GemstoneType;
import org.spring.data.gemfire.app.dao.repo.GemstoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The RepositoryUsingPdxSerializationTest class is a test suite of test cases testing the use of Spring Data GemFire's
 * Repository abstraction and extension on a GemFire Cache Region storing PDX serialized data.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class RepositoryUsingPdxSerializationTest {

  private static final AtomicLong ID_SEQUENCE = new AtomicLong(0l);

  @Autowired
  private Cache gemfireCache;

  @Autowired
  private GemstoneRepository gemstoneRepo;

  @Resource(name = "Gemstones")
  private Region<Long, Object> gemstones;

  protected Gemstone createGemstone(final String name) {
    return createGemstone(ID_SEQUENCE.incrementAndGet(), name);
  }

  protected Gemstone createGemstone(final Long id, final String name) {
    return new Gemstone(id, name);
  }

  private PdxInstance serialize(final Gemstone gem) {
    return gemfireCache.createPdxInstanceFactory(Gemstone.class.getName())
      .writeObject("id", gem.getId())
      .writeString("name", gem.getName())
      .writeObject("type", gem.getType())
      .markIdentityField("id")
      .create();
  }

  @Test
  public void testSaveAndReadGemstone() {
    Gemstone expectedRuby = createGemstone("Ruby");

    expectedRuby.setType(GemstoneType.GEM);

    //Object serializedRuby = serialize(expectedRuby);
    //gemstones.put(expectedRuby.getId(), serializedRuby);

    gemstones.put(expectedRuby.getId(), expectedRuby);

    /*
    Object actualRuby = gemstones.get(expectedRuby.getId());

    System.out.printf("Type (%1$s) after Region.get(..)%n", ClassUtils.getClassName(actualRuby));

    if (actualRuby instanceof PdxInstance) {
      actualRuby = ((PdxInstance) actualRuby).getObject();
    }

    assertTrue(actualRuby instanceof Gemstone);
    assertEquals(expectedRuby, actualRuby);
    */

    Object actualRuby = gemstoneRepo.findById(expectedRuby.getId()).orElse(null);
    //Gemstone actualRuby = gemstoneRepo.findOne(expectedRuby.getId());

    System.out.printf("Type (%1$s) after Repo.findOne(..)%n", ClassUtils.getClassName(actualRuby));

    if (actualRuby instanceof PdxInstance) {
      actualRuby = ((PdxInstance) actualRuby).getObject();
    }

    assertTrue(actualRuby instanceof Gemstone);
    assertEquals(expectedRuby, actualRuby);
  }
}
