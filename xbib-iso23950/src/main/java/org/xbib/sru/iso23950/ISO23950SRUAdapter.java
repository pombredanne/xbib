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
package org.xbib.sru.iso23950;

import java.io.IOException;
import java.util.Properties;
import javax.xml.transform.TransformerException;
import org.xbib.io.iso23950.CQLSearchRetrieve;
import org.xbib.io.iso23950.ZAdapter;
import org.xbib.io.iso23950.ZAdapterFactory;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.query.cql.SyntaxException;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.SearchRetrieve;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.sru.adapter.SRUPropertiesAdapter;
import org.xbib.xml.transform.StylesheetTransformer;

public class ISO23950SRUAdapter extends SRUPropertiesAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ISO23950SRUAdapter.class.getName());
    private final ZAdapter zAdapter;
    private final String resultSetName;
    private final String elementSetName;
    private final String version;
    private final String stylesheet;
    private final String format;
    private final String type;

    public ISO23950SRUAdapter(Properties properties) {
        super(properties);
        this.zAdapter = ZAdapterFactory.getAdapter(properties.getProperty("zAdapter"));
        this.version = properties.getProperty("version", "1.2");
        this.resultSetName = properties.getProperty("resultSetName", "default");
        this.elementSetName = properties.getProperty("elementSetName", "F");
        this.stylesheet = properties.getProperty("stylesheet");
        this.format = properties.getProperty("format");
        this.type = properties.getProperty("type");
    }

    @Override
    public void performSearchRetrieve(SearchRetrieve request, SearchRetrieveResponse response, StylesheetTransformer transformer)
            throws IOException, SyntaxException {
        if (transformer == null) {
            throw new Diagnostics(1, "no stylesheet transformer installed");
        }
        // sanity check for record schema
        if (request.getRecordSchema() != null && !getRecordSchema().equals(request.getRecordSchema())) {
            throw new Diagnostics(66, request.getRecordSchema());
        }
        // sanity check for record packing
        if (request.getRecordPacking() != null && !getRecordPacking().equals(request.getRecordPacking())) {
            throw new Diagnostics(6, request.getRecordPacking());
        }
        String cql = request.getQuery();
        int from = request.getStartRecord();
        int size = request.getMaximumRecords();
        transformer.addParameter("version", version);
        transformer.addParameter("format", format);
        transformer.addParameter("type", type);
        if (stylesheet != null) {
            try {
                transformer.setXsl(stylesheet);
            } catch (TransformerException ex) {
                logger.warn(ex.getMessage(), ex);
            }
        }
        zAdapter.setStylesheetTransformer(transformer);
        try {
            long t0 = System.currentTimeMillis();
            zAdapter.connect();
            CQLSearchRetrieve zRequest = new CQLSearchRetrieve();
            zRequest.setDatabase(zAdapter.getDatabases()).
                    setPreferredRecordSyntax(zAdapter.getPreferredRecordSyntax()).
                    setResultSetName(resultSetName).
                    setElementSetName(elementSetName).
                    setQuery(cql).
                    setFrom(from).setSize(size);
            zAdapter.searchRetrieve(zRequest, response);
            long t1 = System.currentTimeMillis();
            response.addResponseParameter("X-SRU-version",
                    version);
            response.addResponseParameter("X-SRU-recordSchema",
                    getRecordSchema());
            response.addResponseParameter("X-SRU-recordPacking",
                    getRecordPacking());
            response.addResponseParameter("X-SRU-numberOfRecords", 
                    Integer.toString(zRequest.getResultCount()));
            getLogger().info("{} [{}ms] [{}] [{}] [{}] [{}]",
                    getURI().getPath(), t1 - t0,
                        zAdapter.getURI().getHost(),
                        zAdapter.getDatabases(),
                        zRequest.getResultCount(),
                        request.getQuery()
                    );
        } finally {
            zAdapter.disconnect();
        }
    }
}
