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

package org.spring.data.gemfire.app.dao.vendor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;

import org.spring.data.gemfire.app.beans.Gemstone;
import org.spring.data.gemfire.app.dao.GemstoneDao;
import org.spring.data.gemfire.app.dao.support.DaoSupportAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * The GemFireGemstoneDao class is a Data Access Object (DAO) implementing the GemsDao interface for accessing
 * and persisting data about gemstones to GemFire (Cache Region).
 *
 * NOTE not a Thread-safe class!
 *
 * @author John Blum
 * @see org.spring.data.gemfire.app.beans.Gemstone
 * @see org.spring.data.gemfire.app.dao.GemstoneDao
 * @see org.spring.data.gemfire.app.dao.support.DaoSupportAdapter
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.stereotype.Repository
 * @see com.gemstone.gemfire.cache.Region
 * @since 1.0.0
 */
@Repository("gemfireGemstoneDao")
@SuppressWarnings("unused")
public class GemFireGemstoneDao extends DaoSupportAdapter<Gemstone, Long> implements GemstoneDao {

  @Autowired(required = false)
  private GemfireTemplate gemstonesTemplate;

  @Resource(name = "Gemstones")
  private Region<Long, String> gemstones;

  public GemFireGemstoneDao() {
  }

  public GemFireGemstoneDao(final Region<Long, String> gemstones) {
    Assert.notNull(gemstones, "The 'Gemstones' Region reference must not be null!");
    this.gemstones = gemstones;
  }

  protected Region<Long, String> getGemstonesRegion() {
    Assert.state(gemstones != null, "A reference to the 'Gemstones' Region was not properly configured!");
    return gemstones;
  }

  protected GemfireTemplate getGemstonesTemplate() {
    if (gemstonesTemplate == null) {
      gemstonesTemplate = new GemfireTemplate(getGemstonesRegion());
    }

    return gemstonesTemplate;
  }

  protected Iterable<Gemstone> mapEntries(final Map<Long, String> entries) {
    List<Gemstone> gemstones = new ArrayList<Gemstone>(entries.size());

    for (Map.Entry<Long, String> entry : entries.entrySet()) {
      gemstones.add(new Gemstone(entry.getKey(), entry.getValue()));
    }

    return gemstones;
  }

  @PostConstruct
  public void init() {
    getGemstonesRegion();
    System.out.printf("%1$s initialized!%n", getClass().getSimpleName());
  }

  // NOTE GemFire does not allow Region.size() within a Transactional context even when the Transaction is read-only.
  @Override
  //@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public int count() {
    return getGemstonesRegion().size();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public Gemstone findBy(final Long key) {
    return (getGemstonesRegion().containsKey(key) ? new Gemstone(key, getGemstonesRegion().get(key)) : null);
  }

  // NOTE GemFire does not allow Region.getAll(Region.keySet()) within a Transactional context even when
  // the Transaction is read-only.
  @Override
  //@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public Iterable<Gemstone> findAll() {
    return mapEntries(getGemstonesTemplate().<Long, String>getAll(getGemstonesRegion().keySet()));
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public Gemstone save(final Gemstone gemstone) {
    getGemstonesTemplate().put(gemstone.getId(), gemstone.getName());
    return gemstone;
  }

}
