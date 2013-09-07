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

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Converts values to {@link Date}s using a {@link DateFormat} object.
 *
 */
public class DateConverter implements ValueConverter<Date> {
    private final DateFormat formatter;

    /**
     * Creates a converter that uses the given date formatter/parser.
     *
     * @param formatter the formatter/parser to use
     * @throws NullPointerException if {@code formatter} is {@code null}
     */
    public DateConverter(DateFormat formatter) {
        if (formatter == null) {
            throw new NullPointerException("illegal null formatter");
        }

        this.formatter = formatter;
    }

    /**
     * Creates a converter that uses a {@link SimpleDateFormat} with the given date/time pattern.  The date formatter
     * created is not {@link SimpleDateFormat#setLenient(boolean) lenient}.
     *
     * @param pattern expected date/time pattern
     * @return the new converter
     * @throws NullPointerException     if {@code pattern} is {@code null}
     * @throws IllegalArgumentException if {@code pattern} is invalid
     */
    public static DateConverter datePattern(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setLenient(false);

        return new DateConverter(formatter);
    }

    /**
     * {@inheritDoc}
     */
    public Date convert(String value) {
        ParsePosition position = new ParsePosition(0);

        Date date = formatter.parse(value, position);
        if (position.getIndex() != value.length()) {
            throw new ValueConversionException(message(value));
        }

        return date;
    }

    /**
     * {@inheritDoc}
     */
    public Class<Date> valueType() {
        return Date.class;
    }

    /**
     * {@inheritDoc}
     */
    public String valuePattern() {
        return formatter instanceof SimpleDateFormat
                ? ((SimpleDateFormat) formatter).toPattern()
                : "";
    }

    private String message(String value) {
        String message = "Value [" + value + "] does not match date/time pattern";
        if (formatter instanceof SimpleDateFormat) {
            message += " [" + ((SimpleDateFormat) formatter).toPattern() + ']';
        }

        return message;
    }
}
