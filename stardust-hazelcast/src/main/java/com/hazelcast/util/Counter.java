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

package com.hazelcast.util;

/**
 * non-thread-safe counter
 *
 * @mdogan 7/19/12
 */
public class Counter {

    private int count ;

    public Counter() {
    }

    public Counter(final int count) {
        this.count = count;
    }

    public void increment() {
        ++count;
    }

    public void decrement() {
        --count;
    }

    public int get() {
        return count;
    }

    public void reset() {
        count = 0;
    }
}
