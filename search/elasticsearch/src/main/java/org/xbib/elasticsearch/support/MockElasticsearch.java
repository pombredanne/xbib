/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.xbib.elasticsearch.support;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;

/**
 *
 * A mock-up for Elasticsearch helper class
 *
 * @author Jörg Prante <joergprante@gmail.com>
 */
public class MockElasticsearch extends Elasticsearch {

    private final static ESLogger logger = ESLoggerFactory.getLogger(MockElasticsearch.class.getName());

    protected Settings settings;

    public MockElasticsearch() {
    }

    public MockElasticsearch settings(Settings settings) {
        this.settings = settings;
        return this;
    }

    public MockElasticsearch newClient() {
        return newClient(findURI());
    }

    public MockElasticsearch newClient(URI uri) {
        settings = initialSettings(uri);
        return this;
    }

    public synchronized void shutdown() {
    }

    public ElasticsearchRequest newRequest() {
        return new ElasticsearchRequest();
    }

    protected void connect(URI uri) throws IOException {
        int port = uri.getPort(); // beware: 9300, not 9200
        if ("es".equals(uri.getScheme())) {
            if ("hostname".equals(uri.getHost())) {
                InetSocketTransportAddress addr = new InetSocketTransportAddress(InetAddress.getLocalHost().getHostName(), port);
                logger.debug("address = {}", addr);
            } else if ("interfaces".equals(uri.getHost())) {
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface netint : Collections.list(nets)) {
                    Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                    for (InetAddress addr : Collections.list(inetAddresses)) {
                        logger.debug("address = {}", addr);
                    }
                }
            }
        }
    }

    public MockElasticsearch waitForHealthyCluster() throws IOException {
        return this;
    }

}
