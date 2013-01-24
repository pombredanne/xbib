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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.xbib.io.util.URIUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 *
 */
public class Elasticsearch implements ElasticsearchInterface {

    private final static Logger logger = LoggerFactory.getLogger(Elasticsearch.class.getName());
    private final static Set<InetSocketTransportAddress> addresses = new HashSet();
    protected static TransportClient client;
    private Settings settings;

    public Elasticsearch() {
    }

    @Override
    public Elasticsearch settings(Settings settings) {
        this.settings = settings;
        return this;
    }

    @Override
    public Elasticsearch newClient() {
        return newClient(findURI());
    }

    @Override
    public Elasticsearch newClient(URI uri) {
        if (client != null) {
            client.close();
            client = null;
        }
        if (client == null) {
            if (settings == null) {
                settings = initialSettings(uri);
            }
            client = new TransportClient(settings);
            try {
                connect(uri);
            } catch (UnknownHostException e) {
                logger.error(e.getMessage(), e);
            } catch (SocketException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return this;
    }

    protected Settings initialSettings(URI uri) {
        return ImmutableSettings.settingsBuilder().put("cluster.name", findClusterName(uri))
                .put("client.transport.sniff", false)
                .put("transport.netty.connections_per_node.low", 0)
                .put("transport.netty.connections_per_node.med", 0)
                .put("transport.netty.connections_per_node.high", 10)
                .put("threadpool.index.type", "fixed")
                .put("threadpool.index.size", "1")
                .put("threadpool.bulk.type", "fixed")
                .put("threadpool.bulk.size", "1")
                .put("threadpool.get.type", "fixed")
                .put("threadpool.get.size", "10")
                .put("threadpool.search.type", "fixed")
                .put("threadpool.search.size", "10")
                .put("threadpool.percolate.type", "fixed")
                .put("threadpool.percolate.size", "1")
                .put("threadpool.management.type", "fixed")
                .put("threadpool.management.size", "1")
                .put("threadpool.flush.type", "fixed")
                .put("threadpool.flush.size", "1")
                .put("threadpool.merge.type", "fixed")
                .put("threadpool.merge.size", "1")
                .put("threadpool.refresh.type", "fixed")
                .put("threadpool.refresh.size", "1")
                .put("threadpool.cache.type", "fixed")
                .put("threadpool.cache.size", "1")
                .put("threadpool.snapshot.type", "fixed")
                .put("threadpool.snapshot.size", "1")
                .build();
    }

    @Override
    public synchronized void shutdown() {
        if (client != null) {
            client.close();
            client = null;
            addresses.clear();
        }
    }

    @Override
    public ElasticsearchRequest newRequest() {
        return new ElasticsearchRequest().newRequest(client.prepareSearch().setPreference("_primary_first"));
    }
    
    public ElasticsearchRequest newGetRequest() {
        return new ElasticsearchRequest().newRequest(client.prepareGet());
    }

    private final static String DEFAULT_CLUSTER_NAME = "elasticsearch";
    private final static URI DEFAULT_URI = URI.create("es://interfaces:9300");

    protected static URI findURI() {
        URI uri = DEFAULT_URI;
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
            logger.debug("the hostname is {}", hostname);
            Properties p = new Properties();
            try (InputStream in = Elasticsearch.class.getResource("/org/xbib/elasticsearch/cluster.properties").openStream()) {
                p.load(in);
                if (p.containsKey(hostname)) {
                    uri = URI.create(p.getProperty(hostname));
                    logger.debug("URI found in cluster.properties for hostname {} = {}",
                            hostname, uri);
                    return uri;
                }
            }
        } catch (UnknownHostException e) {
            logger.warn("can't resolve host name, probably something wrong with network config: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        logger.debug("URI for hostname {} = {}", hostname, uri);
        return uri;
    }

    protected String findClusterName(URI uri) {
        String clustername;
        try {
            // look for URI parameters
            Map<String, String> map = URIUtil.parseQueryString(uri);
            clustername = map.get("es.cluster.name");
            if (clustername != null) {
                logger.info("cluster name found in URI {}", uri);
                return clustername;
            }
        } catch (UnsupportedEncodingException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        logger.info("cluster name not found in URI {}, parameter es.cluster.name", uri);
        clustername = System.getProperty("es.cluster.name");
        if (clustername != null) {
            logger.info("cluster name found in es.cluster.name system property = {}", clustername);
            return clustername;
        }
        logger.info("cluster name not found, falling back to default " + DEFAULT_CLUSTER_NAME);
        clustername = DEFAULT_CLUSTER_NAME;
        return clustername;
    }

    protected void connect(URI uri) throws UnknownHostException, SocketException, IOException {
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
                            if (addr instanceof Inet4Address) {
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

    @Override
    public Elasticsearch waitForHealthyCluster() throws IOException {
        ClusterHealthResponse healthResponse =
                client.admin().cluster().prepareHealth().setWaitForGreenStatus().setTimeout("30s").execute().actionGet(30000);
        if (healthResponse.isTimedOut()) {
            throw new IOException("cluster not healthy, cowardly refusing to continue with operations");
        }
        return this;
    }
}
