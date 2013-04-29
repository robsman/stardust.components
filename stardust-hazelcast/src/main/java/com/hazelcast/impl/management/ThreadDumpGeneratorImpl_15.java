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

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * ThreadDump Java 1.5 implementation
 */

class ThreadDumpGeneratorImpl_15 extends ThreadDumpGenerator {

    public ThreadDumpGeneratorImpl_15(ThreadMXBean bean) {
        super(bean);
    }

    /**
     * modified due to <a href="https://dev.eclipse.org/ipzilla/show_bug.cgi?id=6911">CQ #6911</a>
     */
    protected void appendThreadInfo(ThreadInfo info, StringBuilder sb) {
       sb.append(info.toString());
    }
}
