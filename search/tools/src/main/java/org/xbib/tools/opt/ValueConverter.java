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
package org.xbib.tools.opt;

/**
 * Instances of this interface are used to convert arguments of options into specific Java types.
 *
 * @param <V> constraint on the type of values being converted to
 */
public interface ValueConverter<V> {
    /**
     * Converts the given string value into a Java type.
     *
     * @param value the string to convert
     * @return the converted value
     * @throws ValueConversionException if a problem occurs while converting the value
     */
    V convert(String value);

    /**
     * Gives the class of the type of values this converter converts to.
     *
     * @return the target class for conversion
     */
    Class<V> valueType();

    /**
     * Gives a string that describes the pattern of the values this converter expects, if any.  For example, a date
     * converter can respond with a {@link java.text.SimpleDateFormat date format string}.
     *
     * @return a value pattern, or {@code null} if there's nothing interesting here
     */
    String valuePattern();
}
