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

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * Common AbstractFilter - infra filter code to be used by other filters
 * through inheritance
 * <p/>
 * This is to have following infra init parameters to all the filter
 * <p/>
 * - ignoreURLPattern - to ignore the URLs matching this regex
 * - acceptURLPattern - to process the URLs matching this regex (ignore precedes)
 * - ignoreMIMEPattern - to ignore if the response mime matches this regex
 * - acceptMIMEPattern - to process if the response mime matches this regex (ignore precedes)
 * - ignoreUAPattern - to ignore if request user agent name matches this regex
 * - acceptUAPattern - to process if request user agent name matches this regex
 * <p/>
 * This filter implements IgnoreAcceptContext with the help of above init parameters and provides
 * easy api for inherited filters to know if given req/res to be ignored or processes.
 *
 */
public abstract class AbstractFilter implements Filter, IgnoreAcceptContext {

    protected FilterConfig filterConfig;

    private String ignoreURLPattern;

    private String acceptURLPattern;

    private String ignoreMIMEPattern;

    private String acceptMIMEPattern;

    private String ignoreUAPattern;

    private String acceptUAPattern;

    private static final String INIT_PARAM_IGNORE_URL_PATTERN = "ignoreURLPattern";

    private static final String INIT_PARAM_ACCEPT_URL_PATTERN = "acceptURLPattern";

    private static final String INIT_PARAM_IGNORE_MIME_PATTERN = "ignoreMIMEPattern";

    private static final String INIT_PARAM_ACCEPT_MIME_PATTERN = "acceptMIMEPattern";

    private static final String INIT_PARAM_IGNORE_UA_PATTERN = "ignoreUAPattern";

    private static final String INIT_PARAM_ACCEPT_UA_PATTERN = "acceptUAPattern";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        this.ignoreURLPattern = filterConfig.getInitParameter(INIT_PARAM_IGNORE_URL_PATTERN);
        this.acceptURLPattern = filterConfig.getInitParameter(INIT_PARAM_ACCEPT_URL_PATTERN);
        this.ignoreMIMEPattern = filterConfig.getInitParameter(INIT_PARAM_IGNORE_MIME_PATTERN);
        this.acceptMIMEPattern = filterConfig.getInitParameter(INIT_PARAM_ACCEPT_MIME_PATTERN);
        this.ignoreUAPattern = filterConfig.getInitParameter(INIT_PARAM_IGNORE_UA_PATTERN);
        this.acceptUAPattern = filterConfig.getInitParameter(INIT_PARAM_ACCEPT_UA_PATTERN);
    }

    private boolean isURLIgnored(String url) {
        return this.ignoreURLPattern != null && url != null && url.matches(ignoreURLPattern);
    }

    @Override
    public boolean isURLAccepted(String url) {
        return !this.isURLIgnored(url) && (this.acceptURLPattern == null || (url != null && url.matches(acceptURLPattern)));
    }

    private boolean isMIMEIgnored(String mimeType) {
        return this.ignoreMIMEPattern != null && mimeType != null && mimeType.matches(ignoreMIMEPattern);
    }

    @Override
    public boolean isMIMEAccepted(String mimeType) {
        return !this.isMIMEIgnored(mimeType) && (this.acceptMIMEPattern == null || (mimeType != null && mimeType.matches(acceptMIMEPattern)));
    }

    private boolean isUserAgentIgnored(String userAgent) {
        return this.ignoreUAPattern != null && userAgent != null && userAgent.matches(ignoreUAPattern);
    }

    @Override
    public boolean isUserAgentAccepted(String userAgent) {
        return !this.isUserAgentIgnored(userAgent) && (this.acceptUAPattern == null || (userAgent != null && userAgent.matches(acceptUAPattern)));
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

}

