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
import java.util.List;
import java.util.Properties;

import org.xbib.io.iso23950.PropertiesZClient;
import org.xbib.io.iso23950.searchretrieve.CQLSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.PQFSearchRetrieveRequest;
import org.xbib.io.iso23950.ZServiceFactory;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveResponse;
import org.xbib.query.cql.SyntaxException;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.PropertiesSRUService;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;

public class ISO23950SRUService extends PropertiesSRUService {

    private final Properties properties;

    public ISO23950SRUService(Properties properties) {
        super(properties);
        this.properties = properties;
    }

    /**
     * CQL search
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws SyntaxException
     */
    public void searchRetrieve(CQLSearchRetrieveRequest request, SearchRetrieveResponse response)
            throws IOException {
        // sanity check for record schema
        if (request.getRecordSchema() != null && !getRecordSchema().equals(request.getRecordSchema())) {
            throw new Diagnostics(66, request.getRecordSchema());
        }
        // sanity check for record packing
        if (request.getRecordPacking() != null && !getRecordPacking().equals(request.getRecordPacking())) {
            throw new Diagnostics(6, request.getRecordPacking());
        }
        PropertiesZClient client = ZServiceFactory.getService(properties.getProperty("zAdapter"));
        String version = properties.getProperty("version", "1.2");
        List<String> databases = client.getDatabases();
        String preferredRecordSyntax = client.getPreferredRecordSyntax();
        String resultSetName = properties.getProperty("resultSetName", "default");
        String elementSetName = properties.getProperty("elementSetName", "F");
        String stylesheet = properties.getProperty("stylesheet");
        String format = properties.getProperty("format");
        String type = properties.getProperty("type");
        String cql = request.getQuery();
        int from = request.getStartRecord();
        int size = request.getMaximumRecords();
        try {
            ZSearchRetrieveResponse zResponse = request
                    .setDatabase(databases)
                    .setPreferredRecordSyntax(preferredRecordSyntax).
                    setResultSetName(resultSetName).
                    setElementSetName(elementSetName).
                    setQuery(cql).
                    setFrom(from).
                    setSize(size)
                    .execute();
            StylesheetTransformer transformer = new StylesheetTransformer("/xsl");
            transformer.addParameter("version", version);
            transformer.addParameter("format", format);
            transformer.addParameter("type", type);
            zResponse.setStylesheetTransformer(transformer)
                    .setStylesheets(stylesheet)
                    .to(response.getWriter());
        } catch (org.xbib.io.iso23950.Diagnostics d) {
            throw new Diagnostics(1, d.getPlainText());
        } finally {
            client.close();
        }
    }

    /**
     * PQF search for internal usage. Sending PQF queries is not SRU standard.
     * But Z servers in a federated environment may react differently to
     * queries, CQL is not possible to be translated to each member in most
     * cases, and we fall back to a list of PQF queries to each Z server. As a
     * side effect, from/size must be passed separately , since PQF is not able
     * to carry this information (startRecord/maximumRecords in SRU). The result
     * is fully adhering to SRU response.
     *
     * @param request
     * @throws Diagnostics
     * @throws IOException
     */
    public void searchRetrieve(PQFSearchRetrieveRequest request, SearchRetrieveResponse response)
            throws IOException {
        // sanity check for record schema
        if (request.getRecordSchema() != null && !getRecordSchema().equals(request.getRecordSchema())) {
            throw new Diagnostics(66, request.getRecordSchema());
        }
        // sanity check for record packing
        if (request.getRecordPacking() != null && !getRecordPacking().equals(request.getRecordPacking())) {
            throw new Diagnostics(6, request.getRecordPacking());
        }
        PropertiesZClient client = ZServiceFactory.getService(properties.getProperty("zAdapter"));
        String version = properties.getProperty("version", "1.2");
        List<String> databases = client.getDatabases();
        String preferredRecordSyntax = client.getPreferredRecordSyntax();
        String resultSetName = properties.getProperty("resultSetName", "default");
        String elementSetName = properties.getProperty("elementSetName", "F");
        String stylesheet = properties.getProperty("stylesheet");
        String format = properties.getProperty("format");
        String type = properties.getProperty("type");
        String pqf = request.getQuery();
        int from = request.getStartRecord();
        int size = request.getMaximumRecords();
        try {
            ZSearchRetrieveResponse zResponse = request
                    .setDatabase(databases)
                    .setPreferredRecordSyntax(preferredRecordSyntax).
                            setResultSetName(resultSetName).
                            setElementSetName(elementSetName).
                            setQuery(pqf).
                            setFrom(from).
                            setSize(size)
                    .execute();
            StylesheetTransformer transformer = new StylesheetTransformer("/xsl");
            transformer.addParameter("version", version);
            transformer.addParameter("format", format);
            transformer.addParameter("type", type);
            zResponse.setStylesheetTransformer(transformer)
                    .setStylesheets(stylesheet)
                    .to(response.getWriter());

        } catch (org.xbib.io.iso23950.Diagnostics d) {
            throw new Diagnostics(1, d.getPlainText());
        } finally {
            client.close();
        }
    }
}
