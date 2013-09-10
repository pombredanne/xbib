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
package org.xbib.oai.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xbib.date.DateUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.OAISession;
import org.xbib.oai.identify.IdentifyResponse;
import org.xbib.oai.identify.ListIdentifiersRequest;
import org.xbib.oai.identify.ListIdentifiersResponse;
import org.xbib.oai.metadata.ListMetadataFormatsRequest;
import org.xbib.oai.metadata.ListMetadataFormatsResponse;
import org.xbib.oai.record.ListRecordsResponse;
import org.xbib.oai.identify.IdentifyServerRequest;
import org.xbib.oai.record.ListRecordsServerRequest;
import org.xbib.oai.set.ListSetsRequest;
import org.xbib.oai.set.ListSetsResponse;
import org.xbib.oai.util.OAIRequestDumper;
import org.xbib.oai.util.ResumptionToken;
import org.xbib.oai.exceptions.OAIException;

/**
 *  OAI servlet
 *
 */
public class OAIServlet extends HttpServlet implements OAIConstants {

    private static final Logger logger = LoggerFactory.getLogger(OAIServlet.class.getName());

    private final OAIRequestDumper requestDumper = new OAIRequestDumper();

    private final String responseEncoding = "UTF-8";

    private final String contentType = "text/xml";

    private OAIService service;

    private OAISession session;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String serviceName = config.getInitParameter("name");
        String serviceURI = config.getInitParameter("uri");
        this.service = serviceName != null ?
                OAIServiceFactory.getService(serviceName) :
                serviceURI != null ?
                        OAIServiceFactory.getService(serviceURI) :
                        OAIServiceFactory.getDefaultService();
    }

    @Override
    public void doGet(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(contentType);
        final OutputStream out = response.getOutputStream();
        logger.info(requestDumper.toString(request));
        if (session == null) {
            session = service.newSession();
        }
        try {
            String verb = request.getParameter(VERB_PARAMETER);
            Writer writer = new OutputStreamWriter(response.getOutputStream(), responseEncoding);
            if (IDENTIFY.equals(verb)) {
                ServerIdentifyRequest oaiRequest = new ServerIdentifyRequest(session, request);
                IdentifyResponse oaiResponse = new IdentifyResponse(oaiRequest);
                oaiRequest.setURL(service.getBaseURL().toURI());
                service.identify(oaiRequest, oaiResponse);
                oaiResponse.to(writer);
            } else if (LIST_METADATA_FORMATS.equals(verb)) {
                ServerListMetadataFormatsRequest oaiRequest = new ServerListMetadataFormatsRequest(session, request);
                ListMetadataFormatsResponse oaiResponse = new ListMetadataFormatsResponse(oaiRequest);
                oaiRequest.setURL(service.getBaseURL().toURI());
                service.listMetadataFormats(oaiRequest, oaiResponse);
                oaiResponse.to(writer);
            } else if (LIST_SETS.equals(verb)) {
                ServerListSetsRequest oaiRequest = new ServerListSetsRequest(session, request);
                ListSetsResponse oaiResponse = new ListSetsResponse(oaiRequest);
                oaiRequest.setURL(service.getBaseURL().toURI());
                service.listSets(oaiRequest, oaiResponse);
                oaiResponse.to(writer);
            } else if (LIST_IDENTIFIERS.equals(verb)) {
                ServerListIdentifiersRequest oaiRequest = new ServerListIdentifiersRequest(session, request);
                ListIdentifiersResponse oaiResponse = new ListIdentifiersResponse(oaiRequest);
                oaiRequest.setURL(service.getBaseURL().toURI());
                service.listIdentifiers(oaiRequest, oaiResponse);
                oaiResponse.to(writer);
            } else if (LIST_RECORDS.equals(verb)) {
                ServerListRecordsRequest oaiRequest = new ServerListRecordsRequest(session, request);
                ListRecordsResponse oaiResponse = new ListRecordsResponse(oaiRequest);
                oaiRequest.setURL(service.getBaseURL().toURI());
                service.listRecords(oaiRequest, oaiResponse);
                oaiResponse.to(writer);
            }
        } catch (OAIException | URISyntaxException e) {
            logger.warn(e.getMessage(), e);
            response.setStatus(500);
        } finally {
            out.flush();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
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

    private class ServerIdentifyRequest extends IdentifyServerRequest {

        HttpServletRequest request;

        ServerIdentifyRequest(OAISession session, HttpServletRequest request) {
            super(session);
            this.request = request;
        }

        @Override
        public Map<String, List<String>> getParameterMap() {
            Map<String,String[]> m = request.getParameterMap();
            Map<String, List<String>> result = new HashMap();
            for (String key : m.keySet()) {
                result.put(key, Arrays.asList(m.get(key)));
            }
            return result;
        }

        @Override
        public String getPath() {
            return getPathInfo(request);
        }
    };

    private class ServerListMetadataFormatsRequest extends ListMetadataFormatsRequest {

        HttpServletRequest request;

        ServerListMetadataFormatsRequest(OAISession session, HttpServletRequest request) {
            super(session);
            this.request = request;
        }

    };

    private class ServerListSetsRequest extends ListSetsRequest {

        HttpServletRequest request;

        ServerListSetsRequest(OAISession session, HttpServletRequest request) {
            super(session);
            this.request = request;
        }

        @Override
        public ServerListSetsRequest setResumptionToken(ResumptionToken token) {
            return this;
        }

        @Override
        public ResumptionToken getResumptionToken() {
            UUID uuid = UUID.fromString(request.getParameter(RESUMPTION_TOKEN_PARAMETER));
            return ResumptionToken.get(uuid);
        }

    }

    private class ServerListRecordsRequest extends ListRecordsServerRequest {

        HttpServletRequest request;

        ServerListRecordsRequest(OAISession session, HttpServletRequest request) {
            super(session);
            this.request = request;
        }

        @Override
        public ServerListRecordsRequest setFrom(Date from) {
            return this;
        }

        @Override
        public Date getFrom() {
            return DateUtil.parseDateISO(request.getParameter(FROM_PARAMETER));
        }

        @Override
        public ServerListRecordsRequest setUntil(Date until) {
            return this;
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
        public ServerListRecordsRequest setResumptionToken(ResumptionToken token) {
            return this;
        }

        @Override
        public ResumptionToken getResumptionToken() {
            UUID uuid = UUID.fromString(request.getParameter(RESUMPTION_TOKEN_PARAMETER));
            return ResumptionToken.get(uuid);
        }

        @Override
        public Map<String, List<String>> getParameterMap() {
            Map<String,String[]> m = request.getParameterMap();
            Map<String, List<String>> result = new HashMap();
            for (String key : m.keySet()) {
                result.put(key, Arrays.asList(m.get(key)));
            }
            return result;
        }

        public String getPath() {
            return getPathInfo(request);
        }
    }

    private class ServerListIdentifiersRequest extends ListIdentifiersRequest {

        HttpServletRequest request;

        ServerListIdentifiersRequest(OAISession session, HttpServletRequest request) {
            super(session);
            this.request = request;
        }

        @Override
        public ServerListIdentifiersRequest setFrom(Date from) {
            return this;
        }

        @Override
        public Date getFrom() {
            return DateUtil.parseDateISO(request.getParameter(FROM_PARAMETER));
        }

        @Override
        public ServerListIdentifiersRequest setUntil(Date until) {
            return this;
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
        public ServerListIdentifiersRequest setResumptionToken(ResumptionToken token) {
            return this;
        }

        @Override
        public ResumptionToken getResumptionToken() {
            UUID uuid = UUID.fromString(request.getParameter(RESUMPTION_TOKEN_PARAMETER));
            return ResumptionToken.get(uuid);
        }

    }
}
