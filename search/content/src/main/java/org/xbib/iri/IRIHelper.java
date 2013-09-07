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
package org.xbib.iri;

import java.util.regex.Pattern;

/**
 * Utility methods for working with URI's / IRI's
 */
public class IRIHelper {

    private static final Pattern javascript =
        Pattern.compile("\\s*j\\s*a\\s*v\\s*a\\s*s\\s*c\\s*r\\s*i\\s*p\\s*t\\s*:.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern mailto =
        Pattern.compile("\\s*m\\s*a\\s*i\\s*l\\s*t\\s*o\\s*:.*", Pattern.CASE_INSENSITIVE);

    public static boolean isJavascriptUri(IRI uri) {
        if (uri == null)
            return false;
        return javascript.matcher(uri.toString()).matches();
    }

    public static boolean isMailtoUri(IRI uri) {
        if (uri == null)
            return false;
        return mailto.matcher(uri.toString()).matches();
    }

}
