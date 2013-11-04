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
import org.xbib.elasticsearch.CQLSearchRequest;
import org.xbib.elasticsearch.CQLSearchResponse;
import org.xbib.elasticsearch.CQLSearchSupport;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.query.cql.SyntaxException;
import org.xbib.search.NotFoundError;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;

import java.io.IOException;
import java.net.URI;

/**
 * SearchRetrieve by URL for Elasticseach
 */
public class SRUClient implements org.xbib.sru.client.SRUClient {

    private final Logger logger = LoggerFactory.getLogger(SRUClient.class.getName());

    private final SRUService service;

    private final CQLSearchSupport support;

    public SRUClient(SRUService service, CQLSearchSupport support) {
        this.service = service;
        this.support = support;
    }

    public void close() throws IOException {
        if (support != null && support.client() != null) {
            support.client().close();
            support.shutdown();
        }
        if (service != null) {
            service.close();
        }
    }

    public URI getClientIdentifier() {
        return service.getURI();
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
    public SRURequest newSearchRetrieveRequest() {
        return new SRURequest();
    }

    public SRUResponse searchRetrieve(SearchRetrieveRequest request) throws IOException {
        if (request == null) {
            throw new IOException("request not set");
        }
        SRUResponse response = new SRUResponse(request);
        // allow only our versions
        boolean versionfound = false;
        String[] versions = getVersion().split(",");
        if (request.getVersion() != null) {
            for (int i = 0; i < versions.length; i++) {
                if (request.getVersion().equals(versions[i])) {
                    versionfound = true;
                }
            }
        }
        if (!versionfound) {
            throw new Diagnostics(5, request.getVersion() + " not supported. Supported versions are " + getVersion());
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
            int from = request.getStartRecord() - 1;
            if (from < 0) {
                from = 0;
            }
            int size = request.getMaximumRecords();
            if (size < 0) {
                size = 0;
            }
            // creating CQL from SearchRetrieve request
            CQLSearchRequest cqlRequest = support.newSearchRequest()
                    .index(getIndex(request))
                    .from(from)
                    .size(size)
                    .cql(request.getQuery())
                    .cqlFilter(request.getFilter())
                    .cqlFacetFilter(request.getFilter())
                    .facet(request.getFacetLimit(), request.getFacetSort(), null);
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
        } catch (NotFoundError e) {
            logger.error("SRU " + service.getURI() + ": nothing found for query " + request.getQuery(), e);
            // no diagnostics!
        } catch (IOException e) {
            logger.error("SRU " + service.getURI() + ": database is unresponsive", e);
            throw new Diagnostics(1, e.getMessage());
        } catch (Exception e) {
            logger.error("SRU " + service.getURI() + ": unknown error", e);
            throw new Diagnostics(1, e.getMessage());
        } finally {
            logger.info("SRU completed: query = {}", request.getQuery());
        }
        return response;
    }

    private String getIndex(SearchRetrieveRequest request) {
        String index = null;
        String[] spec = extractPathInfo(request.getPath());
        if (spec != null) {
            if (spec.length > 1) {
                // both index and type are there
                index = spec[spec.length - 2];
            } else if (spec.length == 1) {
                index = spec[0];
            }
        }
        logger.debug("path = {} got index = {}", request.getPath(), index);
        return index;
    }

    private String[] extractPathInfo(String path) {
        if (path == null) {
            return null;
        }
        // delete leading and trailing slash
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        // swallow "sru"
        if (path.startsWith("sru/")) {
            path = path.substring(4);
        }
        return path.split("/");
    }

}
