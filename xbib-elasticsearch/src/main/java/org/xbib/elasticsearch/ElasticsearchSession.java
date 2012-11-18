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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.xbib.io.Mode;
import org.xbib.io.Session;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class ElasticsearchSession implements Session {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchSession.class.getName());
    private final ElasticsearchConnection connection;
    private final Set<InetSocketTransportAddress> addresses = new HashSet();
    private TransportClient client;
    private Logger queryLogger;

    public ElasticsearchSession(ElasticsearchConnection connection,  Logger queryLogger) {
        this.connection = connection;
        this.queryLogger = queryLogger;
        this.client = createClient();
        try {
            findNodes();            
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void open(Mode mode) throws IOException {
        if (isOpen()) {
            return;
        }
        if (mode == Mode.SIMULATED_WRITE) {
            return;
        }
        if (mode == Mode.READ) {
            createQueryLog();
        }
        if (mode == Mode.WRITE) {
            checkHealth();
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public boolean isOpen() {
        return client != null && !client.connectedNodes().isEmpty();
    }

    public Client getClient() throws UnknownHostException, SocketException, IOException {
        // check if client can continue to work
        if (!isOpen()) {
            if (client != null) {
                client.close();
            }
            logger.warn("reconnecting");
            this.client = createClient();
            findNodes();            
        }
        return client;
    }
    
    public void setQueryLogger(Logger queryLogger) {
        this.queryLogger = queryLogger;
    }

    public Logger getQueryLogger() throws IOException {
        // ensure a query logger does exist
        if (queryLogger == null) {
            createQueryLog();
        }
        return queryLogger;
    }

    private void createQueryLog() throws IOException {
        if (queryLogger == null) {
            queryLogger = LoggerFactory.getLogger("es.query.logger");
        }
    }

    private TransportClient createClient() {
        logger.info("starting discovery for clustername {}", connection.getClusterName());
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", connection.getClusterName())
                .put("client.transport.sniff", connection.isSniff())
                
                .build();
        return new TransportClient(settings);
    }

    private void findNodes() throws UnknownHostException, SocketException, IOException {
        // find cluster nodes
        URI uri = connection.getURI();
        String hostname = uri.getHost();
        int port = uri.getPort(); // beware: 9300, not 9200
        boolean newaddresses = false;
        if ("es".equals(uri.getScheme())) {
            switch (uri.getHost()) {
                case "hostname": {
                    InetSocketTransportAddress address = new InetSocketTransportAddress(InetAddress.getLocalHost().getHostName(), port);
                    if (!addresses.contains(address)) {
                        logger.info("adding hostname address for transport client = {}", address);
                        client.addTransportAddress(address);
                        logger.info("hostname address added");
                        addresses.add(address);
                        newaddresses = true;
                    }
                    break;
                }
                case "interfaces": {
                    Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                    for (NetworkInterface netint : Collections.list(nets)) {
                        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                        for (InetAddress addr : Collections.list(inetAddresses)) {
                            // optional IPv6 support now, will change in the future
                            if (addr instanceof Inet4Address || connection.isIPV6()) {
                                InetSocketTransportAddress address = new InetSocketTransportAddress(addr, port);
                                if (!addresses.contains(address)) {
                                    logger.info("adding interface address for transport client = {}", address);
                                    client.addTransportAddress(address);
                                    addresses.add(address);
                                    newaddresses = true;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        } else {
            InetSocketTransportAddress address = new InetSocketTransportAddress(hostname, port);
            if (!addresses.contains(address)) {
                logger.info("adding custom address for transport client = {}", address);
                client.addTransportAddress(address);
                addresses.add(address);
                newaddresses = true;
            }
        }
        logger.info("addresses = {}", addresses);
        if (newaddresses) {
            List<DiscoveryNode> nodes = client.connectedNodes().asList();
            logger.info("connected nodes = {}", nodes);
            for (DiscoveryNode node : nodes) {
                logger.info("new connection to {} {}", node.getId(), node.getName());
            }
        }
    }

    public void checkHealth() throws IOException {
        logger.info("checking cluster health");
        if (isOpen()) {
            ClusterHealthResponse healthResponse =
                    client.admin().cluster().prepareHealth().setWaitForYellowStatus().setTimeout("30s").execute().actionGet();
            if (healthResponse.isTimedOut()) {
                throw new IOException("cluster not ready, cowardly refusing to continue");
            }
        }
    }
}
