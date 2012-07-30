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
package org.xbib.sru.client;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javax.xml.stream.events.XMLEvent;
import org.testng.annotations.Test;
import org.xbib.io.Request;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.SRUResponseAdapter;
import org.xbib.sru.SearchRetrieve;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;

public class ClientTest {

    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class.getName());

    @Test
    public void testZDBClient() throws Exception {
        SRUClient client = SRUClientFactory.getClient("ZDB");
        String query = "title = linux";
        int from = 0;
        int size = 10;
        URI uri = client.getURI();
        logger.info("uri from properties = {}", uri);
        String authority = uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
        final URI uri2 = new URI(uri.getScheme(), authority, uri.getPath(), uri.getQuery(), uri.getFragment());
        logger.info("uri = {}, authority = {}, uri2 = {}", uri, authority, uri2);
        
        SearchRetrieve request = new SearchRetrieve()
                .setURI(client.getURI())           
                .setVersion(client.getVersion())
                .setRecordPacking(client.getRecordPacking())
                .setRecordSchema(client.getRecordSchema())
                .setQuery(query).setStartRecord(from).setMaximumRecords(size);

        if (client.getUsername() != null) {
            request.setUsername(client.getUsername());
            request.setPassword(client.getPassword());
        }
        FileOutputStream out = new FileOutputStream("target/sru-client-" + client.getURI().getHost() + ".xml");
        try (Writer fw = new OutputStreamWriter(out, "UTF-8")) {
            SearchRetrieveResponse response = new SearchRetrieveResponse(fw);
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
                    logger.info("got record:"
                            + " recordSchema=" + recordSchema
                            + " recordPacking=" + recordPacking
                            + " recordIdentifier=" + recordIdentifier
                            + " recordPosition=" + recordPosition);
                }

                @Override
                public void recordData(Collection<XMLEvent> record) {
                    //logger.info("recordData = " + record + " events");
                }

                @Override
                public void extraRecordData(Collection<XMLEvent> record) {
                    //logger.info("extraRecordData = " + record + " events");
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
            StylesheetTransformer transformer = new StylesheetTransformer("src/test/resources/xsl");
            client.setStylesheetTransformer(transformer);
            client.searchRetrieve(request, response).execute(30L, TimeUnit.SECONDS);     
            client.close();
        }
    }
}
