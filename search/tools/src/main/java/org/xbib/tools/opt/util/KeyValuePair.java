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
package org.xbib.tools.opt.util;

import static org.xbib.util.Strings.EMPTY;

/**
 * A simple string key/string value pair.
 * <p/>
 * <p>This is useful as an argument type for options whose values take on the form
 * <kbd>key=value</kbd>, such as JVM command line system properties.</p>
 *
 */
public final class KeyValuePair {
    public final String key;
    public final String value;

    private KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Parses a string assumed to be of the form <kbd>key=value</kbd> into its parts.
     *
     * @param asString key-value string
     * @return a key-value pair
     * @throws NullPointerException if {@code stringRepresentation} is {@code null}
     */
    public static KeyValuePair valueOf(String asString) {
        int equalsIndex = asString.indexOf('=');
        if (equalsIndex == -1) {
            return new KeyValuePair(asString, EMPTY);
        }

        String aKey = asString.substring(0, equalsIndex);
        String aValue = equalsIndex == asString.length() - 1 ? EMPTY : asString.substring(equalsIndex + 1);

        return new KeyValuePair(aKey, aValue);
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof KeyValuePair)) {
            return false;
        }

        KeyValuePair other = (KeyValuePair) that;
        return key.equals(other.key) && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return key.hashCode() ^ value.hashCode();
    }

    @Override
    public String toString() {
        return key + '=' + value;
    }
}
