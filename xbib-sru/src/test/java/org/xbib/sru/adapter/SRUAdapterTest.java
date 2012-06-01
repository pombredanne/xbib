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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.events.XMLEvent;
import org.testng.annotations.Test;
import org.xbib.io.Request;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.SRUAdapter;
import org.xbib.sru.SRUResponseAdapter;
import org.xbib.sru.SearchRetrieve;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;

public class SRUAdapterTest {

    private static final Logger logger = Logger.getLogger(SRUAdapterTest.class.getName());

    @Test
    public void testAdapterSearchRetrieve() throws Diagnostics, IOException {
        for (String adapterName : Arrays.asList("Gent", "Lund", "Bielefeld", "ZDB")) {
            String query = "title = linux";
            int from = 1;
            int size = 10;
            final SRUAdapter adapter = SRUAdapterFactory.getAdapter(adapterName);
            StylesheetTransformer transformer = new StylesheetTransformer(
                    "src/test/resources/xsl");
            FileOutputStream out = new FileOutputStream("target/sru-adapter-" + adapter.getURI().getHost() + ".xml");
            Writer w = new OutputStreamWriter(out, "UTF-8");
            SearchRetrieveResponse response = new SearchRetrieveResponse(w);
            response.setListener(new SRUResponseAdapter() {

                @Override
                public void onConnect(Request request) {
                    logger.log(Level.INFO, "connect, request = " + request);
                }

                @Override
                public void version(String version) {
                    logger.log(Level.INFO, "version = " + version);
                }

                @Override
                public void numberOfRecords(int numberOfRecords) {
                    logger.log(Level.INFO, "numberOfRecords = " + numberOfRecords);
                }

                @Override
                public void beginRecord() {
                    logger.log(Level.INFO, "begin record");
                }

                @Override
                public void recordMetadata(String recordSchema, String recordPacking, String recordIdentifier, int recordPosition) {
                    logger.log(Level.INFO, "got record from: adapter=" + adapter.getURI()
                            + " recordSchema=" + recordSchema
                            + " recordPacking=" + recordPacking
                            + " recordIdentifier=" + recordIdentifier
                            + " recordPosition=" + recordPosition);
                }

                @Override
                public void recordData(List<XMLEvent> record) {
                    //logger.log(Level.INFO, "recordData = " + record);
                }

                @Override
                public void extraRecordData(List<XMLEvent> record) {
                    //logger.log(Level.INFO, "extraRecordData = " + record);
                }

                @Override
                public void endRecord() {
                    logger.log(Level.INFO, "end record");
                }

                @Override
                public void onDisconnect(Request request) {
                    logger.log(Level.INFO, "disconnect, request = " + request);
                }
            });
            try {
                adapter.connect();
                adapter.setStylesheetTransformer(transformer);
                SearchRetrieve request = new SearchRetrieve();
                request.setURI(adapter.getURI()).setVersion(adapter.getVersion()).
                        setRecordPacking(adapter.getRecordPacking()).setRecordSchema(adapter.getRecordSchema()).setQuery(query).setStartRecord(from).setMaximumRecords(size);
                adapter.searchRetrieve(request, response);
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            } finally {
                adapter.disconnect();
            }
            w.close();
        }
    }
}
