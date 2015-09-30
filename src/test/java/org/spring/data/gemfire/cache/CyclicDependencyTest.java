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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEvent;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * The CyclicDependencyTest class is a test suite of test cases testing the cycle of dependencies between Application
 * beans and components (Service and DAO) and GemFire beans and components (Regions, Queues, Listeners, etc).
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 1.3.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class CyclicDependencyTest {

  /*
  @Resource(name = "Colocated")
  private Region<?, ?> colocated;
  */

  @Resource(name = "otherAppDao")
  private ApplicationDao otherAppDao;

  @Test
  public void testCycle() {
    //assertNotNull(colocated);
    assertNotNull(otherAppDao);
  }

  protected static final class ApplicationService {

    @Autowired
    private ApplicationDao dao;

    protected ApplicationDao getDao() {
      Assert.state(dao != null, "A reference to the Application DAO was not properly configured!");
      return dao;
    }

    public void setDao(final ApplicationDao dao) {
      this.dao = dao;
    }

    @PostConstruct
    public void init() {
      getDao();
      System.out.printf("%1$s initialized!%n", getClass().getName());
    }

    @Override
    public String toString() {
      return getClass().getName();
    }
  }

  protected static final class ApplicationDao {

    @Resource(name = "AppData")
    private Region<?, ?> appData;

    protected Region<?, ?> getAppData() {
      Assert.state(appData != null, "A reference to the GemFire 'AppData' Region was not properly configured!");
      return appData;
    }

    public void setAppData(final Region<?, ?> appData) {
      this.appData = appData;
    }

    @PostConstruct
    public void init() {
      getAppData();
      System.out.printf("%1$s initialized!%n", this);
    }

    @Override
    public String toString() {
      return getClass().getName();
    }
  }

  protected static final class ApplicationAsyncEventListener implements AsyncEventListener {

    @Autowired
    private ApplicationService service;

    protected ApplicationService getService() {
      Assert.state(service != null, "A reference to the Application Service was not properly configured!");
      return service;
    }

    public void setService(final ApplicationService service) {
      this.service = service;
    }

    @Override
    public boolean processEvents(final List<AsyncEvent> events) {
      // use ApplicationService to process events...
      return false;
    }

    @PostConstruct
    public void init() {
      getService();
      System.out.printf("%1$s initialized!%n", this);
    }

    @PreDestroy
    @Override
    public void close() {
      service = null;
      System.out.printf("%1$s closed!%n", this);
    }

    @Override
    public String toString() {
      return getClass().getName();
    }
  }

}
