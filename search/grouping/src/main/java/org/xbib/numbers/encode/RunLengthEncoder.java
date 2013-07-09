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
package org.xbib.numbers.encode;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

public class RunLengthEncoder {

    private final List members = new LinkedList();

    private final NumberFormat format = NumberFormat.getInstance();

    public RunLengthEncoder() {
    }

    public RunLengthEncoder member(Number number) {
        this.members.add(number);
        return this;
    }

    public RunLengthEncoder member(String string) {
        this.members.add(string);
        return this;
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        for (Object o : members) {
            if (o instanceof Number) {
                Number number = (Number)o;
                String s = format.format(number);
                if (s.length() <= 9) {
                    sb.append(Integer.toString(s.length())).append(s);
                } else {
                    throw new IllegalArgumentException("number too long");
                }
            } else {
                sb.append(o.toString());
            }
        }
        return sb.toString();
    }

}
