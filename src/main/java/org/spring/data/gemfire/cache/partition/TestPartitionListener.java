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

package org.spring.data.gemfire.cache.partition;

import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.partition.PartitionListenerAdapter;
import org.spring.data.gemfire.support.RegionUtils;

/**
 * The TestPartitionListener class is an implementation of the PartitionListenerAdapter.
 *
 * @author John Blum
 * @see org.apache.geode.cache.partition.PartitionListenerAdapter
 * @since 1.3.3
 */
@SuppressWarnings("unused")
public class TestPartitionListener extends PartitionListenerAdapter {

  @Resource(name = "AppData")
  private Region appData;

  // NOTE using constructor injection causes a circular dependency problem here... a BeanCurrentlyInCreationException.
  // When the Region is being created, it too references this PartitionListener during the creation, configuration
  // and initialization of the Region, thereby creating the circular reference.
  /*
  @Autowired
  public TestPartitionListener(final Region<String, Object> appData) {
    this.appData = appData;
  }
  */

  public Region getRegion() {
    return this.appData;
  }

  //@Resource(name = "AppData")
  public void setRegion(final Region appData) {
    this.appData = appData;
  }

  @Override
  public String toString() {
    return String.format("PartitionListener (%1$s) for Region (%2$s)", getClass().getName(),
      RegionUtils.getName(getRegion()));
  }

}
