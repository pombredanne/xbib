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
package org.xbib.io.iso23950.adapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import org.testng.annotations.Test;
import org.xbib.io.iso23950.CQLSearchRetrieve;
import org.xbib.io.iso23950.Diagnostics;
import org.xbib.io.iso23950.ZAdapter;
import org.xbib.io.iso23950.ZAdapterFactory;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;

public class ZAdapterTest {

    private static final Logger logger = LoggerFactory.getLogger(ZAdapterTest.class.getName());
    
    @Test
    public void testAdapterSearchRetrieve() {
        for (String adapterName : Arrays.asList("BNF", "LIBRIS", "LOC", "OBVSG")) {
            try {
                logger.info("trying " + adapterName);
                String query = "dc.title = Linux";
                String resultSetName = "default";
                String elementSetName = "F";
                int from = 1;
                int size = 10;
                ZAdapter adapter = ZAdapterFactory.getAdapter(adapterName);
                FileOutputStream out = new FileOutputStream("target/" + adapter.getURI().getHost() + ".xml");
                try (Writer sw = new OutputStreamWriter(out, "UTF-8")) {
                    StylesheetTransformer transformer = new StylesheetTransformer("src/main/resources");
                    try {
                        adapter.connect();
                        adapter.setStylesheetTransformer(transformer);
                        CQLSearchRetrieve op = new CQLSearchRetrieve();
                        op.setDatabase(adapter.getDatabases()).setQuery(query).setResultSetName(resultSetName).setElementSetName(elementSetName).setPreferredRecordSyntax(adapter.getPreferredRecordSyntax()).setFrom(from).setSize(size);
                        adapter.searchRetrieve(op, new SearchRetrieveResponse(sw));
                    } finally {
                        adapter.disconnect();
                    }
                }
            } catch (Diagnostics d) {
                d.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
