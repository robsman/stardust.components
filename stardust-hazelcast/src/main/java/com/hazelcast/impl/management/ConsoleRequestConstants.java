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

package com.hazelcast.impl.management;

public final class ConsoleRequestConstants {

    public static final byte STATE_OUT_OF_MEMORY = 0;
    public static final byte STATE_ACTIVE = 1;

    public static final int REQUEST_TYPE_LOGIN = 0;
    public static final int REQUEST_TYPE_CLUSTER_STATE = 1;
    public static final int REQUEST_TYPE_GET_THREAD_DUMP = 2;
    public static final int REQUEST_TYPE_EXECUTE_SCRIPT = 3;
    public static final int REQUEST_TYPE_EVICT_LOCAL_MAP = 4;
    public static final int REQUEST_TYPE_CONSOLE_COMMAND = 5;
    public static final int REQUEST_TYPE_MAP_CONFIG = 6;
    public static final int REQUEST_TYPE_DETECT_DEADLOCK = 7;

    private ConsoleRequestConstants() {
    }
}
