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
package org.xbib.io;

import java.util.HashMap;
import java.util.Map;

public enum OutputFormat {

    JSON("application/json", "json"),
    XML("application/xml", "xml"),
    XHTML("application/xhtml+xml", "xhtml"),
    MODS("application/mods+xml", "mods"), // http://tools.ietf.org/html/rfc6207
    SRU("application/sru+xml", "sru");

    private String mime;
    private String suffix;
    private static Map<String, OutputFormat> map;

    private static void map(String mime, OutputFormat format) {
        if (map == null) {
            map = new HashMap();
        }
        map.put(mime, format);
    }

    OutputFormat(String mime, String suffix) {
        this.mime = mime;
        this.suffix = suffix;
        map(mime, this);
    }

    public String mimeType() {
        return mime;
    }

    public String suffix() {
        return suffix;
    }

    public static OutputFormat fromMediaType(String mime) {
        return map.get(mime);
    }
}
