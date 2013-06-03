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
package org.xbib.sru.elasticsearch;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.indices.IndexMissingException;
import org.xbib.elasticsearch.support.CQLSearchRequest;
import org.xbib.elasticsearch.support.CQLSearchResponse;
import org.xbib.elasticsearch.support.CQLSearchSupport;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.query.cql.SyntaxException;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;

import java.io.IOException;
import java.net.URI;

public class ElasticsearchSRUClient implements
        SRUClient<ElasticsearchSRURequest,ElasticsearchSRUResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchSRUService.class.getName());

    private final CQLSearchSupport es = new CQLSearchSupport().newClient();

    private ElasticsearchSRUService service;


    public ElasticsearchSRUClient(ElasticsearchSRUService service) {
        this.service = service;
    }

    @Override
    public String getEncoding() {
        return service.getEncoding();
    }

    @Override
    public String getRecordSchema() {
        return service.getRecordSchema();
    }

    @Override
    public String getRecordPacking() {
        return  service.getRecordPacking();
    }

    @Override
    public String getVersion() {
        return service.getVersion();
    }

    @Override
    public void close() throws IOException {
        // nothing
    }

    @Override
    public ElasticsearchSRURequest newSearchRetrieveRequest() {
        return new ElasticsearchSRURequest();

    }

    @Override
    public ElasticsearchSRUResponse execute(ElasticsearchSRURequest request)
            throws IOException {
        if (request == null) {
            throw new IOException("request not set");
        }
        ElasticsearchSRUResponse response = new ElasticsearchSRUResponse(request);
        if (request.getRecordSchema() != null && !service.getRecordSchema().equals(request.getRecordSchema())) {
            throw new Diagnostics(66, request.getRecordSchema());
        }
        if (request.getRecordPacking() != null && !service.getRecordPacking().equals(request.getRecordPacking())) {
            throw new Diagnostics(6, request.getRecordPacking());
        }
        try {
            searchRetrieve(request, response);
        } catch (SyntaxException e) {
            logger.error("CQL syntax error", e);
            throw new Diagnostics(10, e.getMessage());
        } catch (IOException e) {
            logger.error("SRU is unresponsive", e);
            throw new Diagnostics(1, e.getMessage());
        }
        return response;
    }

    protected void searchRetrieve(final ElasticsearchSRURequest request,
                                  final ElasticsearchSRUResponse response) throws IOException {
        // allow only our version
        if (request.getVersion() != null && !request.getVersion().equals(getVersion())) {
            throw new Diagnostics(5, request.getVersion() + " not supported. Supported version is " + getVersion());
        }
        // allow only 'mods'
        if (request.getRecordSchema() != null && !service.getRecordSchema().equals(request.getRecordSchema())) {
            throw new Diagnostics(66, request.getRecordSchema());
        }
        // allow only 'xml'
        if (request.getRecordPacking() != null && !service.getRecordPacking().equals(request.getRecordPacking())) {
            throw new Diagnostics(6, request.getRecordPacking());
        }
        // check for query
        if (request.getQuery() == null || request.getQuery().isEmpty()) {
            throw new Diagnostics(7, "no query parameter given");
        }
        try {
            CQLSearchRequest cqlRequest  = es.newSearchRequest()
                    .index(getIndex(request))
                    .type(getType(request))
                    .from(request.getStartRecord() - 1)
                    .size(request.getMaximumRecords())
                    .cql(getQuery(request))
                    .facet(request.getFacetLimit(), request.getFacetSort(), null);

            logger.info("query = {}", cqlRequest);

            Logger logger = LoggerFactory.getLogger(ElasticsearchSRUService.class.getName());

            CQLSearchResponse cqlResponse = cqlRequest.executeSearch(logger);

            response.setBuffer(cqlResponse.bytes());

            response.setFacets(cqlResponse.getSearchResponse().getFacets());

        } catch (SyntaxException e) {
            logger.error("SRU " + service.getURI() + ": database does not exist", e);
            throw new Diagnostics(10, e.getMessage());
        } catch (NoNodeAvailableException e) {
            logger.error("SRU " + service.getURI() + ": unresponsive", e);
            throw new Diagnostics(1, e.getMessage());
        } catch (IndexMissingException e) {
            logger.error("SRU " + service.getURI() + ": database does not exist", e);
            throw new Diagnostics(1, e.getMessage());
        } catch (IOException e) {
            logger.error("SRU " + service.getURI() + ": database is unresponsive", e);
            throw new Diagnostics(1, e.getMessage());
        } catch (Exception e) {
            logger.error("SRU " + service.getURI() + ": unknown error", e);
            throw new Diagnostics(1, e.getMessage());
        } finally {
            logger.info("SRU completed: query = {}", request.getQuery());
        }
    }

    private String getIndex(SearchRetrieveRequest request) {
        String index = null;
        String path = request.getPath();
        path = path != null && path.startsWith("/sru") ? path.substring(4) : path;
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

    private String getType(SearchRetrieveRequest request) {
        String type = null;
        String path = request.getPath();
        path = path != null && path.startsWith("/sru") ? path.substring(4) : path;
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

    private String getQuery(SearchRetrieveRequest request) {
        String location = null;
        String path = request.getPath();
        path = path != null && path.startsWith("/sru") ? path.substring(4) : path;
        if (path != null) {
            String[] spec = path.split("/");
            if (spec.length > 1) {
                if (!"*".equals(spec[spec.length - 1])) {
                    location = spec[spec.length - 1];
                }
            }
        }
        return (location != null ? "filter.location any \"" + location + "\" and " : "") + request.getQuery();
    }
}
