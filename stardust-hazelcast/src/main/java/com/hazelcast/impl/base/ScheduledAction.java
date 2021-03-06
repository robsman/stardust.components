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

package com.hazelcast.impl.base;

import com.hazelcast.util.Clock;
import com.hazelcast.impl.Request;

import java.util.concurrent.atomic.AtomicLong;

public abstract class ScheduledAction {

    private final static AtomicLong idGen = new AtomicLong();

    protected long timeToExpire;

    protected long timeout;

    protected boolean valid = true;

    protected final Request request;

    protected final long id;

    public ScheduledAction(final Request request) {
        this.request = request;
        setTimeout(request.timeout);
        id = idGen.incrementAndGet();
    }

    public abstract boolean consume();

    public boolean expired() {
        return !valid || (timeout != -1 && Clock.currentTimeMillis() >= getExpireTime());
    }

    public long getExpireTime() {
        return timeToExpire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduledAction that = (ScheduledAction) o;
        if (id != that.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public boolean isValid() {
        return valid;
    }

    public boolean neverExpires() {
        return (timeout == -1);
    }

    public void onExpire() {
    }

    public void onMigrate() {
    }

    public void setTimeout(long newTimeout) {
        if (newTimeout > -1) {
            this.timeout = newTimeout;
            timeToExpire = Clock.currentTimeMillis() + newTimeout;
            if (timeToExpire < 0) {
                this.timeout = -1;
                this.timeToExpire = Long.MAX_VALUE;
            }
        } else {
            this.timeout = -1;
        }
    }

    public void setValid(final boolean valid) {
        this.valid = valid;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + id + "]{ neverExpires=" + neverExpires() + ", timeout= " + timeout + "}";
    }

    public Request getRequest() {
        return request;
    }
}
