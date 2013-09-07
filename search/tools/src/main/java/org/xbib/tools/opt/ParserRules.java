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

import java.util.Collection;

import static java.lang.Character.isLetterOrDigit;

/**
 * <p>Can tell whether or not options are well-formed.</p>
 *
 */
final class ParserRules {
    static final char HYPHEN_CHAR = '-';
    static final String HYPHEN = String.valueOf(HYPHEN_CHAR);
    static final String DOUBLE_HYPHEN = "--";
    static final String OPTION_TERMINATOR = DOUBLE_HYPHEN;
    static final String RESERVED_FOR_EXTENSIONS = "W";

    private ParserRules() {
        throw new UnsupportedOperationException();
    }

    static boolean isShortOptionToken(String argument) {
        return argument.startsWith(HYPHEN)
                && !HYPHEN.equals(argument)
                && !isLongOptionToken(argument);
    }

    static boolean isLongOptionToken(String argument) {
        return argument.startsWith(DOUBLE_HYPHEN) && !isOptionTerminator(argument);
    }

    static boolean isOptionTerminator(String argument) {
        return OPTION_TERMINATOR.equals(argument);
    }

    static void ensureLegalOption(String option) {
        if (option.startsWith(HYPHEN)) {
            throw new IllegalOptionSpecificationException(String.valueOf(option));
        }

        for (int i = 0; i < option.length(); ++i) {
            ensureLegalOptionCharacter(option.charAt(i));
        }
    }

    static void ensureLegalOptions(Collection<String> options) {
        for (String each : options) {
            ensureLegalOption(each);
        }
    }

    private static void ensureLegalOptionCharacter(char option) {
        if (!(isLetterOrDigit(option) || isAllowedPunctuation(option))) {
            throw new IllegalOptionSpecificationException(String.valueOf(option));
        }
    }

    private static boolean isAllowedPunctuation(char option) {
        String allowedPunctuation = "?." + HYPHEN_CHAR;
        return allowedPunctuation.indexOf(option) != -1;
    }
}
