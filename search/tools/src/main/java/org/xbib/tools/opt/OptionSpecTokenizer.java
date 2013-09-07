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

import java.util.NoSuchElementException;

import static org.xbib.tools.opt.ParserRules.RESERVED_FOR_EXTENSIONS;
import static org.xbib.tools.opt.ParserRules.ensureLegalOption;

/**
 * <p>Tokenizes a short option specification string.</p>
 *
 */
class OptionSpecTokenizer {
    private static final char POSIXLY_CORRECT_MARKER = '+';

    private String specification;
    private int index;

    OptionSpecTokenizer(String specification) {
        if (specification == null) {
            throw new NullPointerException("null option specification");
        }

        this.specification = specification;
    }

    boolean hasMore() {
        return index < specification.length();
    }

    AbstractOptionSpec<?> next() {
        if (!hasMore()) {
            throw new NoSuchElementException();
        }


        String optionCandidate = String.valueOf(specification.charAt(index));
        index++;

        AbstractOptionSpec<?> spec;
        if (RESERVED_FOR_EXTENSIONS.equals(optionCandidate)) {
            spec = handleReservedForExtensionsToken();

            if (spec != null) {
                return spec;
            }
        }

        ensureLegalOption(optionCandidate);

        if (hasMore()) {
            spec = specification.charAt(index) == ':'
                    ? handleArgumentAcceptingOption(optionCandidate)
                    : new NoArgumentOptionSpec(optionCandidate);
        } else {
            spec = new NoArgumentOptionSpec(optionCandidate);
        }

        return spec;
    }

    void configure(OptionParser parser) {
        adjustForPosixlyCorrect(parser);

        while (hasMore()) {
            parser.recognize(next());
        }
    }

    private void adjustForPosixlyCorrect(OptionParser parser) {
        if (POSIXLY_CORRECT_MARKER == specification.charAt(0)) {
            parser.posixlyCorrect(true);
            specification = specification.substring(1);
        }
    }

    private AbstractOptionSpec<?> handleReservedForExtensionsToken() {
        if (!hasMore()) {
            return new NoArgumentOptionSpec(RESERVED_FOR_EXTENSIONS);
        }

        if (specification.charAt(index) == ';') {
            ++index;
            return new AlternativeLongOptionSpec();
        }

        return null;
    }

    private AbstractOptionSpec<?> handleArgumentAcceptingOption(String candidate) {
        index++;

        if (hasMore() && specification.charAt(index) == ':') {
            index++;
            return new OptionalArgumentOptionSpec<String>(candidate);
        }

        return new RequiredArgumentOptionSpec<String>(candidate);
    }
}
