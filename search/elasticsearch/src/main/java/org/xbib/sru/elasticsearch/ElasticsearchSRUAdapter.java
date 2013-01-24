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

import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.indices.IndexMissingException;
import org.xbib.elasticsearch.Elasticsearch;
import org.xbib.elasticsearch.Elasticsearch;
import org.xbib.elasticsearch.Formatter;
import org.xbib.elasticsearch.OutputFormat;
import org.xbib.elasticsearch.OutputStatus;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.query.cql.SyntaxException;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.ExplainResponse;
import org.xbib.sru.SRUAdapter;
import org.xbib.sru.Scan;
import org.xbib.sru.ScanResponse;
import org.xbib.sru.SearchRetrieve;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.sru.explain.Explain;
import org.xbib.xml.transform.StylesheetTransformer;

public class ElasticsearchSRUAdapter implements SRUAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchSRUAdapter.class.getName());
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org.xbib.sru.elasticsearch");
    private static final URI adapterURI = URI.create(bundle.getString("uri"));
    private final String recordPacking = "xml";
    private final String recordSchema = "mods";
    private StylesheetTransformer transformer;
    private Elasticsearch elasticsearch = new Elasticsearch().newClient();

    @Override
    public URI getURI() {
        return adapterURI;
    }

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void setStylesheetTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public String getRecordSchema() {
        return recordSchema;
    }

    @Override
    public void explain(Explain op, ExplainResponse response) throws Diagnostics, IOException {
        response.write();
    }

    @Override
    public void searchRetrieve(final SearchRetrieve request, final SearchRetrieveResponse response) throws Diagnostics, IOException {
        if (transformer == null) {
            throw new Diagnostics(1, "no stylesheet transformer for mods installed");
        }
        // allow only our version
        if (request.getVersion() != null && !request.getVersion().equals(getVersion())) {
            throw new Diagnostics(5, request.getVersion() + " not supported. Supported version is " + getVersion());
        }
        // allow only 'mods'
        if (request.getRecordSchema() != null && !recordSchema.equals(request.getRecordSchema())) {
            throw new Diagnostics(66, request.getRecordSchema());
        }
        // allow only 'xml'
        if (request.getRecordPacking() != null && !recordPacking.equals(request.getRecordPacking())) {
            throw new Diagnostics(6, request.getRecordPacking());
        }
        // check for query
        if (request.getQuery() == null || request.getQuery().isEmpty()) {
            throw new Diagnostics(7, "no query parameter given");
        }
        // transport parameters into XSL transformer style sheets
        transformer.addParameter("version", getVersion());
        transformer.addParameter("operation", "searchRetrieve");
        transformer.addParameter("query", request.getQuery());
        transformer.addParameter("startRecord", request.getStartRecord());
        transformer.addParameter("maximumRecords", request.getMaximumRecords());
        transformer.addParameter("recordPacking", getRecordPacking());
        transformer.addParameter("recordSchema", getRecordSchema());
        try {
            String mediaType = "application/x-mods";
            Logger logger = LoggerFactory.getLogger(mediaType, ElasticsearchSRUAdapter.class.getName());
            elasticsearch
                    .newRequest()
                    .setIndex(getIndex(request))
                    .setType(getType(request))
                    .setFrom(request.getStartRecord() - 1)
                    .setSize(request.getMaximumRecords())
                    .cql(getQuery(request))
                    .execute(logger)
                    .format(OutputFormat.formatOf(mediaType))
                    .styleWith(transformer, getStylesheet(), response.getOutput())
                    .dispatchTo(new Formatter() {
                @Override
                public void format(OutputStatus status, OutputFormat format, byte[] message) throws IOException {
                    response.getOutput().write(message);
                }
            });
        } catch (NoNodeAvailableException e) {
            logger.error("SRU " + adapterURI + ": unresponsive", e);
            throw new Diagnostics(1, e.getMessage());
        } catch (IndexMissingException e) {
            logger.error("SRU " + adapterURI + ": database does not exist", e);
            throw new Diagnostics(1, e.getMessage());
        } catch (SyntaxException e) {
            logger.error("SRU " + adapterURI + ": database does not exist", e);
            throw new Diagnostics(10, e.getMessage());
        } catch (IOException e) {
            logger.error("SRU " + adapterURI + ": database is unresponsive", e);
            throw new Diagnostics(1, e.getMessage());
        } finally {
            logger.info("SRU completed: query = {}", request.getQuery());
        }
    }

    @Override
    public void scan(Scan request, ScanResponse response) throws Diagnostics, IOException {
        // todo
    }

    @Override
    public String getVersion() {
        return "1.2";
    }

    @Override
    public String getRecordPacking() {
        return "xml";
    }

    @Override
    public String getEncoding() {
        return "UTF-8";
    }

    @Override
    public String getStylesheet() {
        return "es-sru-response.xsl";
    }

    private String getIndex(SearchRetrieve request) {
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

    private String getType(SearchRetrieve request) {
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

    private String getQuery(SearchRetrieve request) {
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
