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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class StringPacket implements Packet<String> {

    private String name;
    private long number;
    private String link;
    private String string;

    public StringPacket() {
    }

    public StringPacket(String name, long number, String link) {
        name(name);
        number(number);
        link(link);
    }

    public StringPacket(String name, long number, String link, String string) {
        this(name, number, link);
        packet(string);
    }

    public StringPacket name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public StringPacket number(long number) {
        this.number = number;
        return this;
    }

    public long number() {
        return number;
    }

    public StringPacket link(String link) {
        this.link = link;
        return this;
    }

    public String link() {
        return link;
    }

    public StringPacket packet(String string) {
        this.string = string;
        return this;
    }
    
    public String packet() {
        return string;
    }

    public StringPacket slurpAll(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(reader);
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        this.string = sb.toString();
        return this;
    }

    @Override
    public String toString() {
        return string;
    }
    
    
}
