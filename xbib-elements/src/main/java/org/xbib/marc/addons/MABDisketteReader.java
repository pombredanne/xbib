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
package org.xbib.marc.addons;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import org.xbib.io.sequential.Separable;

public class MABDisketteReader extends FilterReader {

    private LinkedList<Integer> pushback;
    private boolean started;
    private boolean ended;
    private boolean leader;

    public MABDisketteReader(Reader in) {
        super(in);
        pushback = new LinkedList();
        started = true;
        ended = false;
        leader = false;
    }

    @Override
    public int read() throws IOException {
        int ch;
        ch = !pushback.isEmpty() ? pushback.pop() : in.read();
        // skip carriage return
        while (ch == '\r') {
            ch = in.read();
        }
        // check for ###
        if (ch == '#') {
            int ch2 = in.read();
            if (ch2 == '#') {
                int ch3 = in.read();
                if (ch3 == '#') {
                    leader = true;
                    in.read(); // skip blank after ###
                    if (!started) {
                        return Separable.GS;
                    } else {
                        started = false;
                        return in.read();
                    }
                } else {
                    return ch3;
                }
            } else {
                return ch2;
            }
        }
        if (ch == '\n') {
            // slurp all subsequent line terminators
            ch = in.read();
            while (ch == '\n' || ch == '\r') {
                ch = in.read();
            }
            if (leader) {
                leader = false;
                return ch;
            }
            pushback.addLast(ch);
            ch = Separable.RS;
            // no UNIT sep!
        }
        // insert a last GS if end of stream reached
        if (ch == -1 && !ended) {
            pushback.addLast(ch);
            ended = true;
            return Separable.GS;
        }
        return ch;
    }

    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            int ch = read();
            if (ch == -1) {
                return (i == 0) ? -1 : i;
            } else {
                cbuf[i + off] = (char) ch;
            }
        }
        return len;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public boolean ready() throws IOException {
        if (!pushback.isEmpty()) {
            return true;
        } else {
            return in.ready();
        }
    }
}
