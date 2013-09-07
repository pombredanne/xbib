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

import java.util.HashMap;
import java.util.Map;

public final class Classes {
    private static final Map<Class<?>, Class<?>> WRAPPERS = new HashMap<Class<?>, Class<?>>(13);

    static {
        WRAPPERS.put(boolean.class, Boolean.class);
        WRAPPERS.put(byte.class, Byte.class);
        WRAPPERS.put(char.class, Character.class);
        WRAPPERS.put(double.class, Double.class);
        WRAPPERS.put(float.class, Float.class);
        WRAPPERS.put(int.class, Integer.class);
        WRAPPERS.put(long.class, Long.class);
        WRAPPERS.put(short.class, Short.class);
        WRAPPERS.put(void.class, Void.class);
    }

    private Classes() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gives the "short version" of the given class name.  Somewhat naive to inner classes.
     *
     * @param className class name to chew on
     * @return the short name of the class
     */
    public static String shortNameOf(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /**
     * Gives the primitive wrapper class for the given class. If the given class is not
     * {@linkplain Class#isPrimitive() primitive}, returns the class itself.
     *
     * @param <T>   generic class type
     * @param clazz the class to check
     * @return primitive wrapper type if {@code clazz} is primitive, otherwise {@code clazz}
     */
    public static <T> Class<T> wrapperOf(Class<T> clazz) {
        return clazz.isPrimitive() ? (Class<T>) WRAPPERS.get(clazz) : clazz;
    }
}
