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

package com.hazelcast.impl.ascii.rest;

import com.hazelcast.impl.ascii.AbstractTextCommandProcessor;
import com.hazelcast.impl.ascii.TextCommandService;

public abstract class HttpCommandProcessor<T> extends AbstractTextCommandProcessor<T> {
    public static final String URI_MAPS = "/hazelcast/rest/maps/";
    public static final String URI_QUEUES = "/hazelcast/rest/queues/";
    public static final String URI_CLUSTER = "/hazelcast/rest/cluster";
    public static final String URI_STATE_DUMP = "/hazelcast/rest/dump";
    public static final String URI_MANCENTER_CHANGE_URL = "/hazelcast/rest/mancenter/changeurl";

    protected HttpCommandProcessor(TextCommandService textCommandService) {
        super(textCommandService);
    }
}
