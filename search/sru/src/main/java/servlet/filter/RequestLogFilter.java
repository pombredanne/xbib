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
package servlet.filter;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet filter that logs HTTP request/response headers
 *
 */
public class RequestLogFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(RequestLogFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (!logger.isDebugEnabled()) {
            filterChain.doFilter(request, response);
        } else {
            XHttpServletRequest xRequest = new XHttpServletRequest((HttpServletRequest) request);
            XHttpServletResponse xResponse = new XHttpServletResponse((HttpServletResponse) response);
            try {
                logger.debug(xRequest.getResquestInfo().toString());
                filterChain.doFilter(xRequest, xResponse);
            } finally {
                logger.debug(xResponse.getResponseInfo().toString());
            }
        }
    }

    @Override
    public void destroy() {
    }

    private static class XHttpServletRequest extends HttpServletRequestWrapper {

        public XHttpServletRequest(HttpServletRequest request) {
            super(request);
        }

        public StringBuilder getResquestInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n").append("> ").append(getMethod()).append(" ").append(getRequestURL());
            if (getQueryString() != null) {
                sb.append("?").append(getQueryString());
            }
            sb.append("\n");
            Enumeration names = getHeaderNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                Enumeration values = getHeaders(name);
                while (values.hasMoreElements()) {
                    String value = (String) values.nextElement();
                    sb.append("> ").append(name).append(": ").append(value).append("\n");
                }
            }
            sb.append(">");
            return sb;
        }
    }

    private static class XHttpServletResponse extends HttpServletResponseWrapper {
        private Map<String, List<String>> headers = new HashMap();
        private int status;
        private String message;

        public XHttpServletResponse(HttpServletResponse response) {
            super(response);
        }

        private List<String> getHeaderValues(String name, boolean reset) {
            List<String> values = headers.get(name);
            if (reset || values == null) {
                values = new ArrayList();
                headers.put(name, values);
            }
            return values;
        }

        @Override
        public void addCookie(Cookie cookie) {
            super.addCookie(cookie);
            List<String> cookies = getHeaderValues("Set-Cookie", false);
            cookies.add(cookie.getName() + "=" + cookie.getValue());
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            status = sc;
            message = msg;
        }


        @Override
        public void sendError(int sc) throws IOException {
            super.sendError(sc);
            status = sc;
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            status = sc;
        }

        @Override
        public void setStatus(int sc, String msg) {
            super.setStatus(sc, msg);
            status = sc;
            message = msg;
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, value);
            List<String> values = getHeaderValues(name, true);
            values.add(value);
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, value);
            List<String> values = getHeaderValues(name, false);
            values.add(value);
        }

        public StringBuilder getResponseInfo() {
            if (status == 0) {
                status = 200;
                message = "OK";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("\n").append("< ").append("status code: ").append(status);
            if (message != null) {
                sb.append(", message: ").append(message);
            }
            sb.append("\n");
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String value : entry.getValue()) {
                    sb.append("< ").append(entry.getKey()).append(": ").append(value).append("\n");
                }
            }
            sb.append("<");
            return sb;
        }
    }
}
