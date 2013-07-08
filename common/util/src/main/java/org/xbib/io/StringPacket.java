/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
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
