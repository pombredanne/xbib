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

import java.io.FileWriter;
import java.io.IOException;

import org.testng.annotations.Test;
import org.xbib.io.OutputFormat;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.SRUService;
import org.xbib.sru.SRUServiceFactory;
import org.xbib.xml.transform.StylesheetTransformer;

public class ElasticsearchSRUTest {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchSRUTest.class.getName());

    SRUService<ElasticsearchSRUClient> service = SRUServiceFactory.getInstance().getDefaultService();

    @Test
    public void testService() throws Exception {
        if (service != null) {
            service.newClient();
        } else {
            logger.error("service not found");
        }
    }

    @Test
    public void testElasticsearchSearchRetrieve() throws Exception {
        OutputFormat format = OutputFormat.MODS;
        ElasticsearchSRUClient client = service.newClient();
        ElasticsearchSRURequest request = client.newSearchRetrieveRequest();
        request.setVersion("2.0")
            .setQuery("dc.creator = \"John\"")
            .setFacetLimit("100:dc.language")
            .setStartRecord(1)
            .setMaximumRecords(10)
            .setRecordPacking("xml")
            .setRecordSchema("mods")
            .setPath("/sru/hbz/*");
        FileWriter w = new FileWriter("target/es." + format.suffix());
        try {
            ElasticsearchSRUResponse response = client.execute(request);
            StylesheetTransformer transformer = new StylesheetTransformer(
                    "src/test/resources",
                    "src/test/resources/xsl");
            response.setOutputFormat(format)
                    .setStylesheetTransformer(transformer)
                    .setStylesheets("es-searchretrieve-response.xsl")
                    .to(w);
            w.close();
        } catch (Diagnostics d) {
            logger.warn("There were diagnostics", d);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            client.close();
        }
    }
}
