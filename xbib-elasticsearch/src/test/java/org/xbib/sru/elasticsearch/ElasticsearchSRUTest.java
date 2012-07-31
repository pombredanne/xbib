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

import java.io.ByteArrayOutputStream;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.ExplainResponse;
import org.xbib.sru.SRUAdapter;
import org.xbib.sru.SRUServiceFactory;
import org.xbib.sru.SearchRetrieve;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.sru.explain.Explain;
import org.xbib.xml.transform.StylesheetTransformer;

public class ElasticsearchSRUTest {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchSRUTest.class.getName());

    @Test
    public void testAdapterInit() throws Exception {
        SRUAdapter adapter = SRUServiceFactory.getInstance().getDefaultAdapter();
        adapter.connect();
        adapter.disconnect();
    }

    @Test
    public void testAdapterExplain() throws Exception {
        SRUAdapter adapter = SRUServiceFactory.getInstance().getDefaultAdapter();
        adapter.connect();
        Explain op = new Explain();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            adapter.explain(op, new ExplainResponse(out, "UTF-8"));
           logger.info("out = {}", out.toString());
        } catch (Diagnostics d) {
            logger.warn("error, diag = {}", d);
        } finally {
            adapter.disconnect();
        }
    }    
    
    @Test
    public void testAdapterSearchRetrieve() throws Exception {
        SRUAdapter adapter = SRUServiceFactory.getInstance().getDefaultAdapter();
        adapter.connect();
        StylesheetTransformer transformer = new StylesheetTransformer(
                "src/test/resources",
                "src/test/resources/xsl"
        );
        adapter.setStylesheetTransformer(transformer);
        SearchRetrieve op = new SearchRetrieve();
        op.setVersion("1.2");
        op.setQuery("dc.creator = \"John\"");
        op.setStartRecord(1);
        op.setMaximumRecords(10);
        op.setRecordPacking("xml");
        op.setRecordSchema("mods");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            adapter.searchRetrieve(op, new SearchRetrieveResponse(out, "UTF-8"));
            logger.info("output", out.toString());
        } catch (Diagnostics d) {
            logger.warn("error", d);
        } finally {
            adapter.disconnect();
        }
    }
}
