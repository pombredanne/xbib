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
package org.xbib.io.iso23950.util;

import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class ZRequestDumper {

    public String toString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("characterEncoding=").append(
                request.getCharacterEncoding()).append(" contentLength=").append(request.getContentLength()).append(" contentType=").append(request.getContentType()).append(" locale=").append(request.getLocale()).append(" locales=");
        final Enumeration<Locale> locales = request.getLocales();
        for (int i = 0; locales.hasMoreElements(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(locales.nextElement());
        }
        sb.append("; ");
        final Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            final String name = paramNames.nextElement();
            sb.append(" parameter=").append(name).append("=");
            final String[] values = request.getParameterValues(name);
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(values[i]);
            }
            sb.append("; ");
        }
        sb.append(" protocol=").append(request.getProtocol()).append(" remoteAddr=").append(request.getRemoteAddr()).append(" remoteHost=").append(request.getRemoteHost()).append(" scheme=").append(request.getScheme()).append(" serverName=").append(request.getServerName()).append(" serverPort=").append(request.getServerPort()).append(" isSecure=").append(request.isSecure());
        sb.append(" contextPath=").append(request.getContextPath());
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            cookies = new Cookie[0];
        }
        for (int i = 0; i < cookies.length; i++) {
            sb.append(" cookie=").append(cookies[i].getName()).append("=").append(cookies[i].getValue());
        }
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            final String value = request.getHeader(name);
            sb.append(" header=").append(name).append("=").append(value);
        }
        sb.append(" method=").append(request.getMethod()).append(" pathInfo=").append(request.getPathInfo()).append(" queryString=").append(request.getQueryString()).append(" remoteUser=").append(request.getRemoteUser()).append(" requestedSessionId=").append(request.getRequestedSessionId()).append(" requestURI=").append(request.getRequestURI()).append(" servletPath=").append(request.getServletPath());
        return sb.toString();
    }
}
