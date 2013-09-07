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
package org.xbib.util;

import java.util.Iterator;
import java.util.List;

import static java.lang.System.getProperty;
import static java.util.Arrays.asList;

public final class Strings {
    public static final String EMPTY = "";
    public static final String SINGLE_QUOTE = "'";
    public static final String LINE_SEPARATOR = getProperty("line.separator");

    private Strings() {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>Gives a string consisting of the given character repeated the given number of
     * times.</p>
     *
     * @param ch    the character to repeat
     * @param count how many times to repeat the character
     * @return the resultant string
     */
    public static String repeat(char ch, int count) {
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < count; ++i) {
            buffer.append(ch);
        }

        return buffer.toString();
    }

    /**
     * <p>Tells whether the given string is either {@code} or consists solely of
     * whitespace characters.</p>
     *
     * @param target string to check
     * @return {@code true} if the target string is null or empty
     */
    public static boolean isNullOrEmpty(String target) {
        return target == null || EMPTY.equals(target);
    }


    /**
     * <p>Gives a string consisting of a given string prepended and appended with
     * surrounding characters.</p>
     *
     * @param target a string
     * @param begin  character to prepend
     * @param end    character to append
     * @return the surrounded string
     */
    public static String surround(String target, char begin, char end) {
        return begin + target + end;
    }

    /**
     * Gives a string consisting of the elements of a given array of strings, each
     * separated by a given separator string.
     *
     * @param pieces    the strings to join
     * @param separator the separator
     * @return the joined string
     */
    public static String join(String[] pieces, String separator) {
        return join(asList(pieces), separator);
    }

    /**
     * Gives a string consisting of the string representations of the elements of a
     * given array of objects, each separated by a given separator string.
     *
     * @param pieces    the elements whose string representations are to be joined
     * @param separator the separator
     * @return the joined string
     */
    public static String join(List<String> pieces, String separator) {
        StringBuilder buffer = new StringBuilder();

        for (Iterator<String> iter = pieces.iterator(); iter.hasNext(); ) {
            buffer.append(iter.next());

            if (iter.hasNext()) {
                buffer.append(separator);
            }
        }

        return buffer.toString();
    }
}
