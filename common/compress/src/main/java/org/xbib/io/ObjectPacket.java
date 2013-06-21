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
package org.xbib.io;

public class ObjectPacket implements Packet<Object> {

    private String name;

    private long number;

    private String link;

    private Object object;

    public ObjectPacket() {
    }

    public ObjectPacket(String name, long number, String link) {
        name(name);
        number(number);
        link(link);
    }

    public ObjectPacket(String name, long number, String link, Object object) {
        this(name, number, link);
        packet(object);
    }

    public ObjectPacket name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public ObjectPacket number(long number) {
        this.number = number;
        return this;
    }

    public long number() {
        return number;
    }

    public ObjectPacket link(String link) {
        this.link = link;
        return this;
    }

    public String link() {
        return link;
    }

    public ObjectPacket packet(Object object) {
        this.object = object;
        return this;
    }

    public Object packet() {
        return object;
    }

    @Override
    public String toString() {
        return object.toString();
    }

}
