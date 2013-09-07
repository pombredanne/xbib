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

import org.xbib.tools.opt.ValueConversionException;
import org.xbib.tools.opt.ValueConverter;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Ensures that values entirely match a regular expression.
 *
 */
public class RegexMatcher implements ValueConverter<String> {
    private final Pattern pattern;

    /**
     * Creates a matcher that uses the given regular expression, modified by the given flags.
     *
     * @param pattern the regular expression pattern
     * @param flags   modifying regex flags
     * @throws IllegalArgumentException if bit values other than those corresponding to the defined match flags are
     *                                  set in {@code flags}
     * @throws java.util.regex.PatternSyntaxException
     *                                  if the expression's syntax is invalid
     */
    public RegexMatcher(String pattern, int flags) {
        this.pattern = compile(pattern, flags);
    }

    /**
     * Gives a matcher that uses the given regular expression.
     *
     * @param pattern the regular expression pattern
     * @return the new converter
     * @throws java.util.regex.PatternSyntaxException
     *          if the expression's syntax is invalid
     */
    public static ValueConverter<String> regex(String pattern) {
        return new RegexMatcher(pattern, 0);
    }

    /**
     * {@inheritDoc}
     */
    public String convert(String value) {
        if (!pattern.matcher(value).matches()) {
            throw new ValueConversionException(
                    "Value [" + value + "] did not match regex [" + pattern.pattern() + ']');
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    public Class<String> valueType() {
        return String.class;
    }

    /**
     * {@inheritDoc}
     */
    public String valuePattern() {
        return pattern.pattern();
    }
}
