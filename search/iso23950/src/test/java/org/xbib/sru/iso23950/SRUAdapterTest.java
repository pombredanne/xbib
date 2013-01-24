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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import javax.xml.stream.events.XMLEvent;
import org.testng.annotations.Test;
import org.xbib.io.Request;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.SRUAdapter;
import org.xbib.sru.SRUResponseAdapter;
import org.xbib.sru.SearchRetrieve;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;

public class SRUAdapterTest {

    private static final Logger logger = LoggerFactory.getLogger(SRUAdapterTest.class.getName());

    @Test
    public void testAdapterSearchRetrieve() {
        for (String adapterName : Arrays.asList("OBVSG")) {
            try {
                logger.info("trying " + adapterName);
                StringWriter sw = new StringWriter();
                String query = "dc.title = Linux";
                int from = 1;
                int size = 10;
                SRUAdapter adapter = ISO23950SRUAdapterFactory.getAdapter(adapterName);
                StylesheetTransformer transformer = new StylesheetTransformer("src/main/resources/xsl");
                adapter.setStylesheetTransformer(transformer);
                try {
                    adapter.connect();
                    SearchRetrieve request = new SearchRetrieve();
                    request.setQuery(query).setStartRecord(from).setMaximumRecords(size);
                    SearchRetrieveResponse response = new SearchRetrieveResponse(sw);
                    response.setListener(new SRUResponseAdapter() {
                        @Override
                        public void onConnect(Request request) {
                            logger.info("connect, request = " + request);
                        }

                        @Override
                        public void version(String version) {
                            logger.info("version = " + version);
                        }

                        @Override
                        public void numberOfRecords(long numberOfRecords) {
                            logger.info("numberOfRecords = " + numberOfRecords);
                        }

                        @Override
                        public void beginRecord() {
                            logger.info("begin record");
                        }

                        @Override
                        public void recordMetadata(String recordSchema, String recordPacking, String recordIdentifier, int recordPosition) {
                            logger.info("got record: "
                                    + " recordSchema=" + recordSchema
                                    + " recordPacking=" + recordPacking
                                    + " recordIdentifier=" + recordIdentifier
                                    + " recordPosition=" + recordPosition);
                        }

                        @Override
                        public void recordData(Collection<XMLEvent> record) {
                            Iterator iterator = record.iterator();
                            while (iterator.hasNext()) {
                                logger.info("recordData = " + iterator.next());
                            }
                        }

                        @Override
                        public void extraRecordData(Collection<XMLEvent> record) {
                            Iterator iterator = record.iterator();
                            while (iterator.hasNext()) {
                                logger.info("extraRecordData = " + iterator.next());
                            }
                        }

                        @Override
                        public void endRecord() {
                            logger.info("end record");
                        }

                        @Override
                        public void onDisconnect(Request request) {
                            logger.info("disconnect, request = " + request);
                        }
                    });
                    adapter.searchRetrieve(request, response);
                } finally {
                    adapter.disconnect();
                }
                if (!sw.toString().isEmpty()) {
                    FileOutputStream out = new FileOutputStream("target/sru-" + adapter.getURI().getHost() + ".xml");
                    try (Writer w = new OutputStreamWriter(out, "UTF-8")) {
                        w.write(sw.toString());
                    }
                }
            } catch (Diagnostics d) {
                logger.error(d.getMessage(), d);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
