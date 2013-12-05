/*
 * Copyright (c) 2008-2012, Hazel Bilisim Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.config;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 */
@RunWith(com.hazelcast.util.RandomBlockJUnit4ClassRunner.class)
public class QueueConfigTest {

    /**
     * Test method for {@link com.hazelcast.config.QueueConfig#getName()}.
     */
    @Test
    public void testGetName() {
        QueueConfig queueConfig = new QueueConfig();
        assertNull(null, queueConfig.getName());
    }

    /**
     * Test method for {@link com.hazelcast.config.QueueConfig#setName(java.lang.String)}.
     */
    @Test
    public void testSetName() {
        String name = "a test name";
        QueueConfig queueConfig = new QueueConfig().setName(name);
        assertEquals(name, queueConfig.getName());
        assertEquals("q:" + name, queueConfig.getBackingMapRef());
        queueConfig.setBackingMapRef("backingMap");
        assertEquals("backingMap", queueConfig.getBackingMapRef());
    }
}
