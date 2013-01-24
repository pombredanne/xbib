/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.xbib.common;

/**
 *
 */
public class Pair<P1, P2> {

    public static <P1, P2> Pair<P1, P2> tuple(P1 p1, P2 p2) {
        return new Pair(p1, p2);
    }
    private final P1 p1;
    private final P2 p2;

    public Pair(P1 p1, P2 p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public P1 p1() {
        return p1;
    }

    public P2 p2() {
        return p2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Pair p = (Pair) o;

        if (p1 != null ? !p1.equals(p.p1) : p.p1 != null) {
            return false;
        }
        if (p2 != null ? !p2.equals(p.p2) : p.p2 != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = p1 != null ? p1.hashCode() : 0;
        result = 31 * result + (p2 != null ? p2.hashCode() : 0);
        return result;
    }
}
