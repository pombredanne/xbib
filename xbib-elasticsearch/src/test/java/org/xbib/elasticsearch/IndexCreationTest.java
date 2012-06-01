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
package org.xbib.elasticsearch;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.discovery.MasterNotDiscoveredException;
import org.testng.annotations.Test;
import org.xbib.io.Connection;
import org.xbib.io.ConnectionManager;
import org.xbib.io.Mode;

public class IndexCreationTest {

    private static final Logger logger = Logger.getLogger(IndexCreationTest.class.getName());

    @Test
    public void testDeleteCreate() throws Exception {
        Connection<ElasticsearchSession> conn =
                (Connection<ElasticsearchSession>) ConnectionManager.getConnection("es://hostname:9300");
        ElasticsearchSession session = null;
        try {
            session = conn.createSession();
            session.open(Mode.CONTROL);
            DeleteIndex removeOp = new DeleteIndex();
            removeOp.setIndex("test");
            CreateIndex createOp = new CreateIndex();
            createOp.setIndex("test");
            PutMapping mappingOp = new PutMapping();
            mappingOp.setIndex("test");
            mappingOp.setType("type");
            logger.log(Level.INFO, "deleting index");
            removeOp.execute(session);
            // wait for complete...
            Thread.sleep(2000L);
            logger.log(Level.INFO, "creating index");
            createOp.execute(session);
            mappingOp.execute(session);
            logger.log(Level.INFO, "created mapping");
        } catch (MasterNotDiscoveredException | NoNodeAvailableException e) {
            logger.log(Level.WARNING, e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }
}
