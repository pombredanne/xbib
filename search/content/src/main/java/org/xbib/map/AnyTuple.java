/*
 *  Copyright 2011 BigData Mx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.xbib.map;

import java.util.Arrays;

public class AnyTuple {

    private final String key;

    private final Object o;

    public AnyTuple(String key, Object o) {
        this.key = key;
        this.o = o;
    }

    public String getKey() {
        return key;
    }

    public Object getObject() {
        return o;
    }

    @Override
    public int hashCode() {
        return hashCode(key, o);
    }

    private int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnyTuple)) {
            return false;
        }
        AnyTuple at = (AnyTuple) o;
        return this.key.equals(at.key) && this.o.equals(at.o);
    }
}