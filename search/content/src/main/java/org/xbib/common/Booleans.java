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
package org.xbib.common;

public class Booleans {

    public static boolean parseBoolean(char[] text, int offset, int length, boolean defaultValue) {
        if (text == null || length == 0) {
            return defaultValue;
        }
        if (length == 1) {
            return text[offset] != '0';
        }
        if (length == 2) {
            return !(text[offset] == 'n' && text[offset + 1] == 'o');
        }
        if (length == 3) {
            return !(text[offset] == 'o' && text[offset + 1] == 'f' && text[offset + 2] == 'f');
        }
        if (length == 5) {
            return !(text[offset] == 'f' && text[offset + 1] == 'a' && text[offset + 2] == 'l' && text[offset + 3] == 's' && text[offset + 4] == 'e');
        }
        return true;
    }

    public static boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return !(value.equals("false") || value.equals("0") || value.equals("off") || value.equals("no"));
    }

    public static Boolean parseBoolean(String value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return !(value.equals("false") || value.equals("0") || value.equals("off") || value.equals("no"));
    }

    public static boolean isExplicitFalse(String value) {
        return (value.equals("false") || value.equals("0") || value.equals("off") || value.equals("no"));
    }

    public static boolean isExplicitTrue(String value) {
        return (value.equals("true") || value.equals("1") || value.equals("on") || value.equals("yes"));
    }
}
