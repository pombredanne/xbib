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
package org.xbib.oai.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.xcontent.xml.namespace.ES;
import org.elasticsearch.indices.IndexMissingException;
import org.xbib.elasticsearch.ElasticsearchConnection;
import org.xbib.elasticsearch.ElasticsearchSession;
import org.xbib.elasticsearch.QueryResultAction;
import org.xbib.io.util.DateUtil;
import org.xbib.json.JsonXmlReader;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.GetRecordRequest;
import org.xbib.oai.IdentifyRequest;
import org.xbib.oai.IdentifyResponse;
import org.xbib.oai.ListIdentifiersRequest;
import org.xbib.oai.ListMetadataFormatsRequest;
import org.xbib.oai.ListRecordsRequest;
import org.xbib.oai.ListSetsRequest;
import org.xbib.oai.OAIResponse;
import org.xbib.oai.OAIServerRequest;
import org.xbib.oai.ResumptionToken;
import org.xbib.oai.adapter.OAIAdapter;
import org.xbib.oai.exceptions.OAIException;
import org.xbib.query.QuotedStringTokenizer;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.InputSource;

public class ElasticsearchOAIAdapter implements OAIAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchOAIAdapter.class.getName());
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org.xbib.sru.elasticsearch");
    private static final URI adapterURI = URI.create(bundle.getString("uri"));
    private ElasticsearchConnection connection;
    private ElasticsearchSession session;
    private StylesheetTransformer transformer;
    private String oaiStyleSheet = "es-oai-response.xsl";

    @Override
    public URI getURI() {
        return getURI();
    }

    @Override
    public void connect() {
        connection = ElasticsearchConnection.getInstance();
        try {
            session = connection.createSession();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (session != null) {
                session.close();
            }
            session = null;
            connection.close();
            connection = null;
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void setStylesheetTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public void identify(IdentifyRequest request, IdentifyResponse response) throws OAIException {
    }

    @Override
    public void listIdentifiers(ListIdentifiersRequest request, OAIResponse response) throws OAIException {
    }

    @Override
    public void listMetadataFormats(ListMetadataFormatsRequest request, OAIResponse response) throws OAIException {
    }

    @Override
    public void listSets(ListSetsRequest request, OAIResponse response) throws OAIException {
    }

    @Override
    public void listRecords(ListRecordsRequest request, OAIResponse response) throws OAIException {
        ResumptionToken resumptionToken = request.getResumptionToken();
        Date dateFrom = request.getFrom();
        Date dateUntil = request.getUntil();
        if (dateFrom != null && dateUntil != null && dateFrom.before(dateUntil)) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"from\":").append(resumptionToken.getPosition()).append(",\"size\":").append(resumptionToken.getInterval()).append(",\"query\":{\"range\":{\"xbib:timestamp\":{\"from\":\"").append(DateUtil.formatDateISO(dateFrom)).append("\",\"to\":\"").append(DateUtil.formatDateISO(dateUntil)).append("\",\"include_lower\":true,\"include_upper\":true}}}}");
            QueryResultAction action = createAction(request);
            perform(action, sb.toString(), response);
        }

    }

    @Override
    public void getRecord(GetRecordRequest request, OAIResponse response) throws OAIException {
    }

    @Override
    public Date getLastModified() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRepositoryName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URL getBaseURL() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getProtocolVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getAdminEmail() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getEarliestDatestamp() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDeletedRecord() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getGranularity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setStylesheet() {
    }

    @Override
    public String getStylesheet() {
        return oaiStyleSheet;
    }

    public QueryResultAction createAction(OAIServerRequest request) {
        return new ElasticsearchOAIAdapter.Elasticsearch2OAI(request.getPath());
    }

    private class Elasticsearch2OAI extends QueryResultAction {

        private final String path;

        Elasticsearch2OAI(String path) {
            this.path = path != null && path.startsWith("/oai") ? path.substring(4) : path;
        }

        @Override
        public String buildQuery(SearchRequestBuilder builder, String query) throws IOException {
            String location = null;
            if (path != null) {
                String[] spec = path.split("/");
                if (spec.length > 1) {
                    if (!"*".equals(spec[spec.length - 1])) {
                        location = spec[spec.length - 1];
                    }
                    setIndex(spec[spec.length - 2]);
                } else if (spec.length == 1) {
                    setIndex(spec[spec.length - 1]);
                }
            }
            String q = null;
            if (location != null) {
                StringBuilder qb = new StringBuilder().append("{\"filtered\":").append(query).append(",\"filter\":{\"or\":[");
                QuotedStringTokenizer t = new QuotedStringTokenizer(location);
                StringBuilder sb = new StringBuilder();
                while (t.hasMoreTokens()) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append("{\"term\":{\"dc:identifier.xbib:identifierAuthorityISIL\":\"").append(location).append("\"}}");
                }
                qb.append(sb);
                qb.append("]}}");
                q = qb.toString();
            }
            return super.buildQuery(builder, q);
        }

        @Override
        public void process(InputStream in) throws IOException {
            try {
                JsonXmlReader reader = new JsonXmlReader(new QName(ES.NS_URI, "result", ES.NS_PREFIX));
                transformer.setSource(new SAXSource(reader, new InputSource(in))).setXsl(getStylesheet()).setTarget(getTarget()).apply();
            } catch (TransformerException ex) {
                throw new IOException(ex);
            }
        }
    }

    private void perform(QueryResultAction action, String query, OAIResponse response) throws OAIException {
        try {
            action.setSession(session);
            action.setTarget(response.getOutput());
            action.searchAndProcess(query);
        } catch (NoNodeAvailableException e) {
            logger.error("OAI " + adapterURI + ": unresponsive", e);
            throw new OAIException(e);
        } catch (IndexMissingException e) {
            logger.error("OAI " + adapterURI + ": database does not exist", e);
            throw new OAIException(e);
        } catch (IOException e) {
            logger.error("OAI " + adapterURI + ": database is unresponsive", e);
            throw new OAIException(e);
        } finally {
            logger.info("OAI completed: query = {}", query);
        }
    }
}
