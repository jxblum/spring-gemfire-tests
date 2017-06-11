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

package org.spring.data.gemfire.cache.management;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geode.cache.Cache;
import org.apache.geode.management.ManagementService;
import org.apache.geode.management.membership.MembershipEvent;
import org.apache.geode.management.membership.MembershipListener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * GemFire {@link MembershipListener} used to refresh the Spring {@link ApplicationContext} when the member
 * departs, or gets disconnected from the cluster, such as during a network failure,
 * and then subsequently reconnects to the cluster when auto-reconnect is configured.
 *
 * This listener is really only applicable when Spring (Data GemFire) is used to configure, bootstrap
 * and initialize a GemFire Server with auto-reconnect enabled.
 *
 * NOTE: this listener is only applicable if the GemFire cache is a peer {@link Cache} in the cluster.
 * In other words, this class is not applicable to GemFire cache clients
 * (i.e. {@link org.apache.geode.cache.client.ClientCache}.
 *
 * @author John Blum
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.management.ManagementService
 * @see org.apache.geode.management.membership.MembershipListener
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class AutoReconnectMembershipListener implements ApplicationContextAware, MembershipListener {

  private ApplicationContext applicationContext;

  private final AtomicBoolean memberLeft = new AtomicBoolean(false);

  public AutoReconnectMembershipListener(Cache gemfireCache) {
    Optional.ofNullable(ManagementService.getExistingManagementService(gemfireCache))
      .orElseGet(() -> ManagementService.getManagementService(gemfireCache)).addMembershipListener(this);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public void memberJoined(MembershipEvent event) {
    if (this.memberLeft.compareAndSet(true, false)) {
      if (this.applicationContext instanceof ConfigurableApplicationContext) {
        ((ConfigurableApplicationContext) this.applicationContext).refresh();
      }
    }
  }

  @Override
  public void memberLeft(MembershipEvent event) {
    this.memberLeft.set(true);
  }

  @Override
  public void memberCrashed(MembershipEvent event) {
    this.memberLeft.set(true);
  }
}
