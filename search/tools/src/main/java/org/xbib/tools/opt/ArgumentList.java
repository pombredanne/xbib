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

import static org.xbib.tools.opt.ParserRules.DOUBLE_HYPHEN;
import static org.xbib.tools.opt.ParserRules.HYPHEN_CHAR;

/**
 * <p>Wrapper for an array of command line arguments.</p>
 *
 */
class ArgumentList {
    private final String[] arguments;
    private int currentIndex;

    ArgumentList(String... arguments) {
        this.arguments = arguments.clone();
    }

    boolean hasMore() {
        return currentIndex < arguments.length;
    }

    String next() {
        return arguments[currentIndex++];
    }

    String peek() {
        return arguments[currentIndex];
    }

    void treatNextAsLongOption() {
        if (HYPHEN_CHAR != arguments[currentIndex].charAt(0)) {
            arguments[currentIndex] = DOUBLE_HYPHEN + arguments[currentIndex];
        }
    }
}
