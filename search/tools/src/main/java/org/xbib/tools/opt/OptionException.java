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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.unmodifiableCollection;
import static org.xbib.util.Strings.SINGLE_QUOTE;

/**
 * Thrown when a problem occurs during option parsing.
 *
 */
public abstract class OptionException extends RuntimeException {


    private final List<String> options = new ArrayList<String>();

    protected OptionException(Collection<String> options) {
        this.options.addAll(options);
    }

    protected OptionException(Collection<String> options, Throwable cause) {
        super(cause);

        this.options.addAll(options);
    }

    /**
     * Gives the option being considered when the exception was created.
     *
     * @return the option being considered when the exception was created
     */
    public Collection<String> options() {
        return unmodifiableCollection(options);
    }

    protected final String singleOptionMessage() {
        return singleOptionMessage(options.get(0));
    }

    protected final String singleOptionMessage(String option) {
        return SINGLE_QUOTE + option + SINGLE_QUOTE;
    }

    protected final String multipleOptionMessage() {
        StringBuilder buffer = new StringBuilder("[");

        for (Iterator<String> iter = options.iterator(); iter.hasNext(); ) {
            buffer.append(singleOptionMessage(iter.next()));
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }

        buffer.append(']');

        return buffer.toString();
    }

    static OptionException unrecognizedOption(String option) {
        return new UnrecognizedOptionException(option);
    }
}
