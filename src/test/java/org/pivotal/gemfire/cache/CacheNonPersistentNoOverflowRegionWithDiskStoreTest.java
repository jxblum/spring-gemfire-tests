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

package org.pivotal.gemfire.cache;

import static org.junit.Assert.assertNotNull;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * The CacheNonPersistentNoEvictionRegionWithDiskStoreTest class...
 *
 *
 * @author John Blum
 * @see
 * @since 7.x
 */
public class CacheNonPersistentNoOverflowRegionWithDiskStoreTest {

	private Cache cache;

	@Before
	public void setup() {
		cache = new CacheFactory()
			.set("cache-xml-file", "region-diskstore-cache.xml")
			.set("name", getClass().getSimpleName())
			.set("mcast-port", "0")
			.set("log-level", "config")
			.create();
	}

	@Test
	public void testSetup() {
		assertNotNull("The GemFire Cache was not properly configured and initialized!", cache);
		assertNotNull("Expected Region AppData!", cache.getRegion("AppData"));
	}

}
