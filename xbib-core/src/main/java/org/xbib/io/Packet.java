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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A packet is data with a given name and some package information
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class Packet implements Data {

    private static final long serialVersionUID = 1L;
    private String name;
    private String number;
    private String link;
    private Object object;
    private final MessageDigest md;

    public Packet() {
        this(null);
    }

    public Packet(Object object) {
        setObject(object);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            digest = null;
        }
        this.md = digest;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public final void setObject(Object object) {
        this.object = object;
    }

    public MessageDigest createDigest() throws UnsupportedEncodingException {
        return createDigest(null);
    }

    public MessageDigest createDigest(MessageDigest old) throws UnsupportedEncodingException {
        md.reset();
        if (old != null) {
            md.update(old.digest());
        }
        if (name != null) {
            md.update(name.getBytes("UTF-8"));
        }
        if (number != null) {
            md.update(number.getBytes("UTF-8"));
        }
        if (object != null) {
            md.update(object.toString().getBytes("UTF-8"));
        }
        return md;
    }

    public MessageDigest getDigest() {
        return md;
    }

    public String getDigestString() {
        return bytesToHex(md.digest());
    }

    @Override
    public String toString() {
        return object.toString();
    }
    private char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private String bytesToHex(byte[] b) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            buf.append(hexDigit[(b[i] >> 4) & 0x0f]).append(hexDigit[b[i] & 0x0f]);
        }
        return buf.toString();
    }
}
