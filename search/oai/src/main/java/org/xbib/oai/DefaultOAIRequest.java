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
package org.xbib.oai;

import org.xbib.io.http.netty.DefaultHttpRequest;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.util.ResumptionToken;

import java.net.URI;
import java.util.Date;

/**
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class DefaultOAIRequest extends DefaultHttpRequest implements OAIRequest {

    private ResumptionToken token;

    private String set;

    private String prefix;

    private Date from;

    private Date until;

    protected DefaultOAIRequest(OAISession session) {
        super(session.getSession());
        setURI(session.getURI());
        setMethod("GET");
        addHeader("User-Agent", OAIClient.USER_AGENT);
    }

    public DefaultOAIRequest setURI(URI uri) {
        super.setURI(uri);
        return this;
    }

    @Override
    public DefaultOAIRequest addParameter(String name, String value) {
        if (value != null && value.length() > 0) {
            super.addParameter(name, value);
        }
        return this;
    }

    public OAIRequest setSet(String set) {
        this.set = set;
        return this;
    }

    public String getSet() {
        return set;
    }

    public OAIRequest setMetadataPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getMetadataPrefix() {
        return prefix;
    }

    public OAIRequest setFrom(Date from) {
        this.from = from;
        return this;
    }

    public Date getFrom() {
        return from;
    }

    public OAIRequest setUntil(Date until) {
        this.until = until;
        return this;
    }

    public Date getUntil() {
        return until;
    }

    public OAIRequest setResumptionToken(ResumptionToken token) {
        this.token = token;
        return this;
    }

    public ResumptionToken getResumptionToken() {
        return token;
    }
    
}
