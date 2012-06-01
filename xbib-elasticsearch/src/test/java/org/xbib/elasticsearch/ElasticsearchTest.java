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
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.discovery.MasterNotDiscoveredException;
import static org.elasticsearch.index.query.QueryBuilders.*;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.*;
import org.testng.annotations.Test;

public class ElasticsearchTest {

    private static final Logger logger = Logger.getLogger(ElasticsearchTest.class.getName());

    @Test
    public void testQuery() {

        String index = "test";
        String type = "test";
        Node node = null;
        try {
            node = nodeBuilder().client(true).node();
            Client client = node.client();
            client.prepareSearch().setIndices(index).
                    setTypes(type).
                    setFrom(0).setSize(10).setQuery(textQuery("_all", "test")).execute().actionGet();
        } catch (ClusterBlockException | NoNodeAvailableException | IndexMissingException e) {
            logger.log(Level.WARNING, e.getMessage());
        } finally {
            if (node !=null){
                node.stop();
                node.close();
            }
        }
    }

    @Test
    public void testSniff() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", "elasticsearch")
                .put("client.transport.sniff", false).build();
        try {
            TransportClient client = new TransportClient(settings);
            InetSocketTransportAddress address = new InetSocketTransportAddress("127.0.0.1", 9300);
            client.addTransportAddress(address);
            client.close();
        } catch (MasterNotDiscoveredException e) {
            logger.log(Level.WARNING, e.getMessage());
        }
    }
}