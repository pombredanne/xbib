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
package org.xbib.servlet.filter.common;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Common Class to hold the public static constant so that to share across the project
 *
 */
public interface Constants {

    String TYPE_JS = "js";

    String TYPE_CSS = "css";

    String DEFAULT_CHARSET = "UTF-8";

    String EXT_JS = ".js";

    String EXT_JSON = ".json";

    String EXT_CSS = ".css";

    String MIME_OCTET_STREAM = "application/octet-stream";

    String MIME_JS = "text/javascript";

    String MIME_JSON = "application/json";

    String MIME_CSS = "text/css";

    String HEADER_EXPIRES = "Expires";

    String HEADER_LAST_MODIFIED = "Last-Modified";

    String PARAM_EXPIRE_CACHE = "_expirecache_";

    String PARAM_RESET_CACHE = "_resetcache_";

    String PARAM_SKIP_CACHE = "_skipcache_";

    String PARAM_DEBUG = "_dbg_";

    long DEFAULT_EXPIRES_MINUTES = 7 * 24 * 60; //7 days

    String DEFAULT_CACHE_CONTROL = "public";//

    int DEFAULT_COMPRESSION_SIZE_THRESHOLD = 128 * 1024; //128KB

    String HTTP_VARY_HEADER = "Vary";

    String HTTP_ACCEPT_ENCODING_HEADER = "Accept-Encoding";

    String HTTP_CONTENT_ENCODING_HEADER = "Content-Encoding";

    String HTTP_CACHE_CONTROL_HEADER = "Cache-Control";

    String HTTP_CONTENT_LENGTH_HEADER = "Content-Length";

    String HTTP_CONTENT_TYPE_HEADER = "Content-Type";

    String HTTP_ETAG_HEADER = "ETag";

    String HTTP_IF_NONE_MATCH_HEADER = "If-None-Match";

    String HTTP_IF_MODIFIED_SINCE = "If-Modified-Since";

    String CONTENT_ENCODING_GZIP = "gzip";

    String CONTENT_ENCODING_COMPRESS = "compress";

    String CONTENT_ENCODING_DEFLATE = "deflate";

    String CONTENT_ENCODING_IDENTITY = "identity";

    String HTTP_USER_AGENT_HEADER = "User-Agent";

    //HTTP dates are in one of these format
    //@see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html

    String DATE_PATTERN_RFC_1123 = "EEE, dd MMM yyyy HH:mm:ss z";

    String DATE_PATTERN_RFC_1036 = "EEEEEEEEE, dd-MMM-yy HH:mm:ss z";

    String DATE_PATTERN_ANSI_C = "EEE MMM d HH:mm:ss yyyy";

    String DATE_PATTERN_HTTP_HEADER ="EEE, dd MMM yyyy HH:mm:ss zzz";

    String HEADER_X_OPTIMIZED_BY = "X-Optimized-By";

    String X_OPTIMIZED_BY_VALUE = "http://webutilities.googlecode.com";

    //HTTP locale - US
    Locale DEFAULT_LOCALE_US = Locale.US;

    //HTTP timeZone - GMT
    TimeZone DEFAULT_ZONE_GMT = TimeZone.getTimeZone("GMT");

    Pattern CSS_IMG_URL_PATTERN = Pattern.compile("[uU][rR][lL]\\s*\\(\\s*['\"]?([^('|\")]*)['\"]?\\s*\\)");

    //Map that holds Image path -> CSS files path that refers it
    Map<String, List<String>> CSS_IMG_REFERENCES = new HashMap();

}
