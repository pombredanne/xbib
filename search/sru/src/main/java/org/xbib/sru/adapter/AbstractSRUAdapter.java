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
package org.xbib.sru.adapter;

import java.io.IOException;
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

public abstract class AbstractSRUAdapter implements SRUAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSRUAdapter.class.getName());
    private StylesheetTransformer transformer = new StylesheetTransformer("/xsl");
    private Logger sruLogger;

    @Override
    public void setStylesheetTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public void explain(Explain op, ExplainResponse response) throws Diagnostics, IOException {
        performExplain(op, response);
    }

    @Override
    public void searchRetrieve(SearchRetrieve request, SearchRetrieveResponse response)
            throws Diagnostics, IOException {
        createLogger();
        if (transformer == null) {
            throw new Diagnostics(1, "no stylesheet transformer installed");
        }
        if (request.getRecordSchema() != null && !getRecordSchema().equals(request.getRecordSchema())) {
            throw new Diagnostics(66, request.getRecordSchema());
        }
        if (request.getRecordPacking() != null && !getRecordPacking().equals(request.getRecordPacking())) {
            throw new Diagnostics(6, request.getRecordPacking());
        }
        // transport parameters into XSL transformer style sheets
        transformer.addParameter("version", request.getVersion());
        transformer.addParameter("operation", "searchRetrieve");
        transformer.addParameter("query", request.getQuery());
        transformer.addParameter("startRecord", request.getStartRecord());
        transformer.addParameter("maximumRecords", request.getMaximumRecords());
        transformer.addParameter("recordPacking", getRecordPacking());
        transformer.addParameter("recordSchema", getRecordSchema());
        try {
            performSearchRetrieve(request, response, transformer);
        } catch (SyntaxException e) {
            logger.error("CQL syntax error", e);
            throw new Diagnostics(10, e.getMessage());
        } catch (IOException e) {
            logger.error("SRU is unresponsive", e);
            throw new Diagnostics(1, e.getMessage());
        }
    }

    @Override
    public void scan(Scan request, ScanResponse response)
            throws Diagnostics, IOException {
        createLogger();
        try {
            performScan(request, response);
        } catch (IOException e) {
            logger.error("SRU is unresponsive", e);
            throw new Diagnostics(1, e.getMessage());
        }
    }

    public Logger getLogger() {
        return sruLogger;
    }

    public StylesheetTransformer getStylesheetTransformer() {
        return transformer;
    }

    private void createLogger() throws IOException {
        if (sruLogger == null) {
            sruLogger = LoggerFactory.getLogger("org.xbib.sru.logger");
        }
    }

    protected void performExplain(Explain op, ExplainResponse response) throws Diagnostics, IOException {
        response.write();
    }

    protected abstract void performSearchRetrieve(SearchRetrieve op,
            SearchRetrieveResponse response,
            StylesheetTransformer transformer)
            throws IOException, SyntaxException;

    protected abstract void performScan(Scan op,
            ScanResponse response)
            throws IOException, SyntaxException;
}
