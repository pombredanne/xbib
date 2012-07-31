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
package org.xbib.oai.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xbib.io.util.DateUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.IdentifyRequest;
import org.xbib.oai.IdentifyResponse;
import org.xbib.oai.ListIdentifiersRequest;
import org.xbib.oai.ListMetadataFormatsRequest;
import org.xbib.oai.ListRecordsRequest;
import org.xbib.oai.ListSetsRequest;
import org.xbib.oai.OAI;
import org.xbib.oai.OAIResponse;
import org.xbib.oai.OAIServiceFactory;
import org.xbib.oai.ResumptionToken;
import org.xbib.oai.adapter.OAIAdapter;
import org.xbib.oai.adapter.OAIAdapterFactory;
import org.xbib.oai.exceptions.OAIException;
import org.xbib.xml.transform.StylesheetTransformer;

public class OAIServlet extends HttpServlet implements OAI {

    private static final Logger logger = LoggerFactory.getLogger(OAIServlet.class.getName());
    private final OAIRequestDumper requestDumper = new OAIRequestDumper();
    private final StylesheetTransformer transformer = new StylesheetTransformer("/xsl");
    private final String responseEncoding = "UTF-8";
    private OAIAdapter adapter;
    private String adapterName;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.adapterName = config.getInitParameter("name");
    }

    @Override
    public void doGet(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/xml");
        final OutputStream out = response.getOutputStream();
        if (adapter == null) {
            adapter = createAdapter(request);
        }
        logger.info(requestDumper.toString(request));
        try {
            adapter.connect();
            adapter.setStylesheetTransformer(transformer);
            String verb = request.getParameter(VERB_PARAMETER);
            if (IDENTIFY.equals(verb)) {
                IdentifyResponse oaiResponse = new IdentifyResponse(response.getOutputStream(), responseEncoding);
                IdentifyRequest oaiRequest = new ServerIdentifyRequest(adapter.getBaseURL().toURI(), request);
                adapter.identify(oaiRequest, oaiResponse);
            }
            if (LIST_METADATA_FORMATS.equals(verb)) {
                OAIResponse oaiResponse = new OAIResponse(response.getOutputStream(), responseEncoding);
                ListMetadataFormatsRequest oaiRequest = new ServerListMetadataFormatsRequest(adapter.getBaseURL().toURI(), request);
                adapter.listMetadataFormats(oaiRequest, oaiResponse);
            } else if (LIST_SETS.equals(verb)) {
                OAIResponse oaiResponse = new OAIResponse(response.getOutputStream(), responseEncoding);
                ListSetsRequest oaiRequest = new ServerListSetsRequest(adapter.getBaseURL().toURI(), request);
                adapter.listSets(oaiRequest, oaiResponse);
            } else if (LIST_IDENTIFIERS.equals(verb)) {
                OAIResponse oaiResponse = new OAIResponse(response.getOutputStream(), responseEncoding);
                ServerListIdentifiersRequest oaiRequest = new ServerListIdentifiersRequest(adapter.getBaseURL().toURI(), request);
                adapter.listIdentifiers(oaiRequest, oaiResponse);
            } else if (LIST_RECORDS.equals(verb)) {
                OAIResponse oaiResponse = new OAIResponse(response.getOutputStream(), responseEncoding);
                ServerListRecordsRequest oaiRequest = new ServerListRecordsRequest(adapter.getBaseURL().toURI(), request);
                adapter.listRecords(oaiRequest, oaiResponse);
            }
        } catch (OAIException | URISyntaxException e) {
            logger.warn(e.getMessage(), e);
            response.setStatus(500);
        } finally {
            out.flush();
            adapter.disconnect();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void destroy() {
        super.destroy();
        adapter.disconnect();
    }

    private OAIAdapter createAdapter(HttpServletRequest request)
            throws ServletException, IOException {
        String[] reqPath = request.getRequestURI().split("/");
        String name = reqPath[reqPath.length - 1];
        try {
            this.adapter = OAIAdapterFactory.getAdapter(name);
        } catch (IllegalArgumentException e) {
            // skip
        }
        if (adapter == null) {
            try {
                this.adapter = OAIAdapterFactory.getAdapter(adapterName);
            } catch (IllegalArgumentException e) {
                // skip
            }
        }
        if (adapter == null) {
            try {
                // class name in web.xml?
                this.adapter = OAIServiceFactory.getInstance().getAdapter(adapterName);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                // skip
            }
        }
        if (adapter == null) {
            throw new ServletException("can't get adapter from adapterName = " + adapterName
                    + " or request URI = " + request.getRequestURI());
        }
        return adapter;
    }

    private URI getBaseURI(HttpServletRequest request) {
        String uri = request.getRequestURL().toString();
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && forwardedHost.length() > 0) {
            uri = uri.replaceAll("://[^/]*", "://" + forwardedHost);
        }
        return URI.create(uri);
    }

    private String getPathInfo(HttpServletRequest request) {
        return request.getPathInfo();
    }

    private class ServerIdentifyRequest extends IdentifyRequest {

        HttpServletRequest request;

        ServerIdentifyRequest(URI uri, HttpServletRequest request) {
            super(uri);
            this.request = request;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return request.getParameterMap();
        }

        @Override
        public String getPath() {
            return getPathInfo(request);
        }
    };

    private class ServerListMetadataFormatsRequest extends ListMetadataFormatsRequest {

        HttpServletRequest request;

        ServerListMetadataFormatsRequest(URI uri, HttpServletRequest request) {
            super(uri);
            this.request = request;
        }

        @Override
        public String getIdentifier() {
            return request.getParameter(IDENTIFIER_PARAMETER);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return request.getParameterMap();
        }

        @Override
        public String getPath() {
            return getPathInfo(request);
        }
    };

    private class ServerListSetsRequest extends ListSetsRequest {

        HttpServletRequest request;

        ServerListSetsRequest(URI uri, HttpServletRequest request) {
            super(uri);
            this.request = request;
        }

        @Override
        public void setResumptionToken(ResumptionToken token) {
        }

        @Override
        public ResumptionToken getResumptionToken() {
            UUID uuid = UUID.fromString(request.getParameter(RESUMPTION_TOKEN_PARAMETER));
            return ResumptionToken.get(uuid);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return request.getParameterMap();
        }

        @Override
        public String getPath() {
            return getPathInfo(request);
        }
    }

    private class ServerListRecordsRequest extends ListRecordsRequest {

        HttpServletRequest request;

        ServerListRecordsRequest(URI uri, HttpServletRequest request) {
            super(uri);
            this.request = request;
        }

        @Override
        public void setFrom(Date from) {
        }

        @Override
        public Date getFrom() {
            return DateUtil.parseDateISO(request.getParameter(FROM_PARAMETER));
        }

        @Override
        public void setUntil(Date until) {
        }

        @Override
        public Date getUntil() {
            return DateUtil.parseDateISO(request.getParameter(UNTIL_PARAMETER));
        }

        @Override
        public String getSet() {
            return request.getParameter(SET_PARAMETER);
        }

        @Override
        public String getMetadataPrefix() {
            return request.getParameter(METADATA_PREFIX_PARAMETER);
        }

        @Override
        public void setResumptionToken(ResumptionToken token) {
        }

        @Override
        public ResumptionToken getResumptionToken() {
            UUID uuid = UUID.fromString(request.getParameter(RESUMPTION_TOKEN_PARAMETER));
            return ResumptionToken.get(uuid);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return request.getParameterMap();
        }

        @Override
        public String getPath() {
            return getPathInfo(request);
        }
    }

    private class ServerListIdentifiersRequest extends ListIdentifiersRequest {

        HttpServletRequest request;

        ServerListIdentifiersRequest(URI uri, HttpServletRequest request) {
            super(uri);
            this.request = request;
        }

        @Override
        public void setFrom(Date from) {
        }

        @Override
        public Date getFrom() {
            return DateUtil.parseDateISO(request.getParameter(FROM_PARAMETER));
        }

        @Override
        public void setUntil(Date until) {
        }

        @Override
        public Date getUntil() {
            return DateUtil.parseDateISO(request.getParameter(UNTIL_PARAMETER));
        }

        @Override
        public String getSet() {
            return request.getParameter(SET_PARAMETER);
        }

        @Override
        public String getMetadataPrefix() {
            return request.getParameter(METADATA_PREFIX_PARAMETER);
        }

        @Override
        public void setResumptionToken(ResumptionToken token) {
        }

        @Override
        public ResumptionToken getResumptionToken() {
            UUID uuid = UUID.fromString(request.getParameter(RESUMPTION_TOKEN_PARAMETER));
            return ResumptionToken.get(uuid);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return request.getParameterMap();
        }

        @Override
        public String getPath() {
            return getPathInfo(request);
        }
    }
}
