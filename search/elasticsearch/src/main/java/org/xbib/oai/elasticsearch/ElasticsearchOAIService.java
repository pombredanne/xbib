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
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.indices.IndexMissingException;
import org.xbib.date.DateUtil;
import org.xbib.elasticsearch.support.CQLSearchSupport;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.client.DefaultOAIClient;
import org.xbib.oai.OAISession;
import org.xbib.oai.identify.IdentifyServerRequest;
import org.xbib.oai.identify.ListIdentifiersResponse;
import org.xbib.oai.metadata.ListMetadataFormatsResponse;
import org.xbib.oai.record.GetRecordResponse;
import org.xbib.oai.record.ListRecordsResponse;
import org.xbib.oai.service.OAIService;
import org.xbib.oai.record.GetRecordRequest;
import org.xbib.oai.identify.IdentifyResponse;
import org.xbib.oai.identify.ListIdentifiersRequest;
import org.xbib.oai.metadata.ListMetadataFormatsRequest;
import org.xbib.oai.service.ServerOAIRequest;
import org.xbib.oai.record.ListRecordsServerRequest;
import org.xbib.oai.set.ListSetsRequest;
import org.xbib.oai.set.ListSetsResponse;
import org.xbib.oai.util.ResumptionToken;
import org.xbib.oai.exceptions.OAIException;
import org.xbib.query.cql.SyntaxException;
import org.xbib.strings.encode.QuotedStringTokenizer;

/**
 * Elasticsearch OAI service
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ElasticsearchOAIService implements OAIService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchOAIService.class.getName());

    private static final ResourceBundle bundle = ResourceBundle.getBundle("org.xbib.sru.elasticsearch");

    private static final URI serviceURI = URI.create(bundle.getString("uri"));

    private CQLSearchSupport es = new CQLSearchSupport().newClient();

    @Override
    public URI getURI() {
        return getURI();
    }

    @Override
    public OAISession connect() {
        return new DefaultOAIClient();
    }

    @Override
    public void disconnect(OAISession session) throws IOException {

    }

    @Override
    public void identify(IdentifyServerRequest request, IdentifyResponse response) throws OAIException {
    }

    @Override
    public void listIdentifiers(ListIdentifiersRequest request, ListIdentifiersResponse response) throws OAIException {
    }

    @Override
    public void listMetadataFormats(ListMetadataFormatsRequest request, ListMetadataFormatsResponse response) throws OAIException {
    }

    @Override
    public void listSets(ListSetsRequest request, ListSetsResponse response) throws OAIException {
    }

    @Override
    public void listRecords(final ListRecordsServerRequest request, final ListRecordsResponse response) throws OAIException {
        String mediaType = "application/x-mods";
        String query = getQuery(request);        
        try {
            Logger logger = LoggerFactory.getLogger(mediaType, ElasticsearchOAIService.class.getName());
            InputStream in = es.newSearchRequest()
                    .index(getIndex(request))
                    .type(getType(request))
                    .from(request.getResumptionToken().getPosition())
                    .size(1000)
                    .query(query)
                    .executeSearch(logger)
                    .read();
            response.setReader(new InputStreamReader(in, "UTF-8"));
        } catch (NoNodeAvailableException e) {
            logger.error("SRU " + serviceURI + ": unresponsive", e);
            throw new OAIException(e.getMessage());
        } catch (IndexMissingException e) {
            logger.error("SRU " + serviceURI + ": database does not exist", e);
            throw new OAIException(e.getMessage());
        } catch (SyntaxException e) {
            logger.error("SRU " + serviceURI + ": database does not exist", e);
            throw new OAIException(e.getMessage());
        } catch (IOException e) {
            logger.error("SRU " + serviceURI + ": database is unresponsive", e);
            throw new OAIException(e.getMessage());
        } finally {
            logger.info("SRU completed: query = {}", query);
        }        
    }

    @Override
    public void getRecord(GetRecordRequest request, GetRecordResponse response) throws OAIException {
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

    private String getIndex(ServerOAIRequest request) {
        String index = null;
        String path = request.getPath();
        path = path != null && path.startsWith("/oai") ? path.substring(4) : path;
        if (path != null) {
            String[] spec = path.split("/");
            if (spec.length > 1) {
                index = spec[spec.length - 2];
            } else if (spec.length == 1) {
                index = spec[spec.length - 1];
            }
        }
        return index;
    }

    private String getType(ListRecordsServerRequest request) {
        String type = null;
        String path = request.getPath();
        path = path != null && path.startsWith("/oai") ? path.substring(4) : path;
        if (path != null) {
            String[] spec = path.split("/");
            if (spec.length > 1) {
                type = spec[spec.length - 1];
            } else if (spec.length == 1) {
                type = null;
            }
        }
        return type;
    }

    private String getQuery(ListRecordsServerRequest request) throws OAIException {
        String location = null;
        String path = request.getPath();
        path = path != null && path.startsWith("/oai") ? path.substring(4) : path;
        if (path != null) {
            String[] spec = path.split("/");
            if (spec.length > 1) {
                if (!"*".equals(spec[spec.length - 1])) {
                    location = spec[spec.length - 1];
                }
            }
        }
        ResumptionToken resumptionToken = request.getResumptionToken();
        Date dateFrom = request.getFrom();
        Date dateUntil = request.getUntil();
        if (dateFrom == null || dateUntil == null || dateFrom.before(dateUntil)) {
            throw new OAIException("illegal date arguments");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{\"from\":").append(resumptionToken.getPosition()).append(",\"size\":").append(resumptionToken.getInterval()).append(",\"query\":{\"range\":{\"xbib:timestamp\":{\"from\":\"").append(DateUtil.formatDateISO(dateFrom)).append("\",\"to\":\"").append(DateUtil.formatDateISO(dateUntil)).append("\",\"include_lower\":true,\"include_upper\":true}}}}");
        String query = sb.toString();
            if (location != null) {
                StringBuilder qb = new StringBuilder().append("{\"filtered\":").append(query).append(",\"filter\":{\"or\":[");
                QuotedStringTokenizer t = new QuotedStringTokenizer(location);
                sb = new StringBuilder();
                while (t.hasMoreTokens()) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append("{\"term\":{\"dc:identifier.xbib:identifierAuthorityISIL\":\"").append(location).append("\"}}");
                }
                qb.append(sb);
                qb.append("]}}");
                query = qb.toString();
            }
        return query;
    }
 
}