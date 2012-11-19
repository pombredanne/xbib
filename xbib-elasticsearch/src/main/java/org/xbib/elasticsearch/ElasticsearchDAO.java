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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.util.XMLEventConsumer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.xbib.elasticsearch.xml.ES;
import org.xbib.io.util.URIUtil;
import org.xbib.json.JsonXmlReader;
import org.xbib.json.JsonXmlStreamer;
import org.xbib.json.JsonXmlValueMode;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.query.cql.CQLParser;
import org.xbib.query.cql.elasticsearch.ESGenerator;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.InputSource;

public class ElasticsearchDAO {

    private final static Logger logger = LoggerFactory.getLogger(ElasticsearchDAO.class.getName());
    private static TransportClient client;
    private final Set<InetSocketTransportAddress> addresses = new HashSet();
    private Settings settings;
    private ImmutableSettings.Builder settingsBuilder;
    private SearchRequestBuilder searchRequestBuilder;
    private String originalQuery;
    private String query;
    private ESGenerator generator;
    private SearchResponse searchResponse;
    private OutputFormat format;
    private long tookInMillis;
    private Logger queryLogger;
    private String[] index;
    private String[] type;
    private String stylesheets;
    private OutputStream target;
    private StylesheetTransformer transformer;
    private XMLEventConsumer consumer;

    public ElasticsearchDAO() {
        this.settingsBuilder = ImmutableSettings.settingsBuilder();
    }

    public ElasticsearchDAO settings(Settings settings) {
        this.settings = settings;
        return this;
    }

    public ElasticsearchDAO newClient(boolean force) {
        if (force && client != null) {
            client.close();
            client = null;
        }
        if (client == null) {
            URI uri = findURI();
            if (settings == null) {
                settings = settingsBuilder.put("cluster.name", findClusterName(uri))
                        .put("client.transport.sniff", false)
                        .put("transport.netty.connections_per_node.low", 0)
                        .put("transport.netty.connections_per_node.med", 0)
                        .put("transport.netty.connections_per_node.high", 5)
                        .put("threadpool.search.type", "fixed")
                        .put("threadpool.search.size", "5")
                        .put("threadpool.get.type", "fixed")
                        .put("threadpool.get.size", "5")
                        .put("threadpool.index.type", "fixed")
                        .put("threadpool.index.size", "1")
                        .put("threadpool.bulk.type", "fixed")
                        .put("threadpool.bulk.size", "1")
                        .put("threadpool.refresh.type", "fixed")
                        .put("threadpool.refresh.size", "1")
                        .put("threadpool.percolate.type", "fixed")
                        .put("threadpool.percolate.size", "1")
                        .build();
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
    
    public synchronized void shutdown() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    public ElasticsearchDAO newRequest() {
        // for CQL 
        generator = new ESGenerator();
        // set preference to primary_first to prevent different scores
        searchRequestBuilder = client.prepareSearch().setPreference("_primary_first");
        return this;
    }

    public ElasticsearchDAO setIndex(String index) {
        if (index != null && !"*".equals(index)) {
            this.index = new String[]{index};
        }
        return this;
    }

    public ElasticsearchDAO setIndex(String... index) {
        this.index = index;
        return this;
    }

    public ElasticsearchDAO setType(String type) {
        if (type != null && !"*".equals(type)) {
            this.type = new String[]{type};
        }
        return this;
    }

    public ElasticsearchDAO setType(String... type) {
        this.type = type;
        return this;
    }

    public ElasticsearchDAO setFrom(int from) {
        searchRequestBuilder.setFrom(from);
        generator.setFrom(from);
        return this;
    }

    public ElasticsearchDAO setSize(int size) {
        searchRequestBuilder.setSize(size);
        generator.setSize(size);
        return this;
    }

    public ElasticsearchDAO filter(String filter) {
        searchRequestBuilder.setFilter(filter);
        return this;
    }

    public ElasticsearchDAO facets(String facets) {
        searchRequestBuilder.setFacets(facets.getBytes());
        return this;
    }

    public ElasticsearchDAO timeout(TimeValue timeout) {
        searchRequestBuilder.setTimeout(timeout);
        return this;
    }

    public ElasticsearchDAO logger(Logger queryLogger) {
        this.queryLogger = queryLogger;
        return this;
    }

    public ElasticsearchDAO query(String query) {
        this.originalQuery = this.query;
        this.query = query == null || query.trim().length() == 0 ? "{\"query\":{\"match_all\":{}}}" : query;
        return this;
    }

    public ElasticsearchDAO fromCQL(String query) {
        if (generator == null) {
            logger.warn("not a new request?");
            return this;
        }
        if (query == null || query.trim().length() == 0) {
            setFrom(0).setSize(10).query(null);
            return this;
        }
        this.originalQuery = query;
        CQLParser parser = new CQLParser(new StringReader(query));
        parser.parse();
        parser.getCQLQuery().accept(generator);
        try {
            this.query = generator.getRequestResult();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    public ElasticsearchDAO execute() throws IOException {
        if (searchRequestBuilder == null) {
            return this;
        }
        if (query == null) {
            return this;
        }
        long t0 = System.currentTimeMillis();
        try {
            if (hasIndex(index)) {
                searchRequestBuilder.setIndices(fixIndexName(index));
            }
            if (hasType(type)) {
                searchRequestBuilder.setTypes(type);
            }
            this.searchResponse = searchRequestBuilder
                    .setExtraSource(query)
                    .execute().actionGet();
            long t1 = System.currentTimeMillis();
            this.tookInMillis = searchResponse.getTookInMillis();
            long hits = searchResponse.getHits().getTotalHits();
            if (queryLogger != null) {
                queryLogger.info(" [{}] [{}ms] [{}ms] [{}] [{}] [{}]",
                        formatIndexType(), t1 - t0, tookInMillis, hits, originalQuery, query);
            }
            // default format: JSON
            this.format = OutputFormat.JSON;
            return this;
        } catch (NoNodeAvailableException e) {
            logger.error(e.getMessage(), e);
            return this;
        }
    }

    public long getTookInMillis() {
        return tookInMillis;
    }

    public ElasticsearchDAO outputFormat(OutputFormat format) {
        this.format = format;
        return this;
    }

    public ElasticsearchDAO toJson(OutputStream out) throws IOException {
        if (out == null) {
            return this;
        }
        if (searchResponse == null) {
            out.write(jsonErrorMessage("no response yet"));
            return this;
        }
        XContentBuilder jsonBuilder = new XContentBuilder(JsonXContent.jsonXContent, out);
        jsonBuilder.startObject();
        searchResponse.toXContent(jsonBuilder, ToXContent.EMPTY_PARAMS);
        jsonBuilder.endObject();
        jsonBuilder.close();
        return this;
    }

    public ElasticsearchDAO xmlEventConsumer(XMLEventConsumer consumer) throws IOException {
        this.consumer = consumer;
        return this;

    }

    public ElasticsearchDAO styleWith(StylesheetTransformer transformer, String stylesheets, OutputStream target) throws IOException {
        this.transformer = transformer;
        this.stylesheets = stylesheets;
        this.target = target;
        return this;
    }

    public ElasticsearchDAO dispatch() throws IOException {
        return dispatchTo(null);
    }

    public ElasticsearchDAO dispatchTo(OutputProcessor processor) throws IOException {
        if (searchResponse == null) {
            if (processor != null) {
                processor.process(OutputStatus.ERROR, OutputFormat.JSON, jsonErrorMessage("no response yet"));
            }
            return this;
        }
        final boolean error = searchResponse.failedShards() > 0 || searchResponse.isTimedOut();

        // error handling
        if (error) {
            StringBuilder sb = new StringBuilder();
            if (searchResponse.failedShards() > 0) {
                for (ShardSearchFailure shf : searchResponse.getShardFailures()) {
                    sb.append(Integer.toString(shf.shardId())).append("=").append(shf.reason()).append(" ");
                }
            }
            if (processor != null) {
                processor.process(OutputStatus.ERROR, format, jsonErrorMessage(sb.toString()));
            }
            return this;
        }

        // fill bi-diectional buffer with JSON
        StreamByteBuffer buffer = new StreamByteBuffer();
        toJson(buffer.getOutputStream());
        buffer.getOutputStream().flush();

        // stylesheet transformation?
        if (transformer != null && stylesheets != null && target != null) {
            try {
                QName root = new QName(ES.NS_URI, "result", ES.NS_PREFIX); // TODO configure this element
                JsonXmlReader reader = new JsonXmlReader(root);
                SAXSource source = new SAXSource(reader, new InputSource(buffer.getInputStream()));
                String[] styles = stylesheets.split(",");
                if (styles.length == 1) {
                    transformer.setSource(source).setXsl(styles[0]).setTarget(target).apply();
                } else if (styles.length == 2) {
                    transformer.setSource(source).setXsl(styles[0]).setXsl2(styles[1]).setTarget(target).apply();
                } else {
                    throw new IOException("stylesheet error: " + stylesheets);
                }
                return this;
            } catch (TransformerException ex) {
                throw new IOException(ex);
            }
        }

        // XML without stylesheet?
        if (format == OutputFormat.XML && stylesheets == null) {
            try {
                QName root = new QName(ES.NS_URI, "result", ES.NS_PREFIX); // TODO configure this element
                JsonXmlStreamer jsonXml = new JsonXmlStreamer(JsonXmlValueMode.SKIP_EMPTY_VALUES);
                if (consumer != null) {
                    jsonXml.toXML(buffer.getInputStream(), consumer, root);
                } else if (target != null) {
                    XMLEventWriter events = jsonXml.openWriter(target, "UTF-8");
                    jsonXml.toXML(buffer.getInputStream(), events, root);
                    events.flush();
                }
                return this;
            } catch (XMLStreamException ex) {
                throw new IOException(ex);
            }
        }

        // all other formats
        if (processor != null) {
            processor.process(OutputStatus.OK, format, buffer.readAsByteArray());
        }

        return this;
    }

    /**
     * Get a single document.
     *
     * @param index
     * @param type
     * @param id
     * @param processor
     * @throws IOException
     */
    public ElasticsearchDAO get(String index, String type, String id, OutputProcessor processor) throws IOException {
        if (format == null) {
            return this;
        }
        long t0 = System.currentTimeMillis();
        try {
            GetResponse getResponse = client.prepareGet(index, type, id).execute().actionGet();
            long t1 = System.currentTimeMillis();
            if (queryLogger != null) {
                queryLogger.info("get complete: {} {}/{}/{} [{}ms] {}",
                        format, index, type, id, (t1 - t0), getResponse.exists());
            }
            if (!getResponse.exists() || getResponse.isSourceEmpty()) {
                if (processor != null) {
                    processor.process(OutputStatus.EMPTY, format, jsonEmptyMessage("not found"));
                }
                return this;
            }
            // stylesheet transformation?
            if (transformer != null && stylesheets != null && target != null) {
                try {
                    QName root = new QName(ES.NS_URI, "source", ES.NS_PREFIX); // TODO configure this element
                    JsonXmlReader reader = new JsonXmlReader(root);
                    String[] styles = stylesheets.split(",");
                    SAXSource source = new SAXSource(reader, new InputSource(new ByteArrayInputStream(getResponse.source())));
                    if (styles.length == 1) {
                        transformer.setSource(source).setXsl(styles[0]).setTarget(target).apply();
                    } else if (styles.length == 2) {
                        transformer.setSource(source).setXsl(styles[0]).setXsl2(styles[1]).setTarget(target).apply();
                    } else {
                        throw new IOException("stylesheet error: " + stylesheets);
                    }
                    return this;
                } catch (TransformerException ex) {
                    throw new IOException(ex);
                }
            }
            if (format == OutputFormat.XML && stylesheets == null) {
                try {
                    QName root = new QName(ES.NS_URI, "source", ES.NS_PREFIX); // TODO configure this element
                    JsonXmlStreamer jsonXml = new JsonXmlStreamer(JsonXmlValueMode.SKIP_EMPTY_VALUES);
                    if (consumer != null) {
                        jsonXml.toXML(new ByteArrayInputStream(getResponse.source()), consumer, root);
                    } else if (target != null) {
                        XMLEventWriter events = jsonXml.openWriter(target, "UTF-8");
                        jsonXml.toXML(new ByteArrayInputStream(getResponse.source()), events, root);
                        events.flush();
                    }
                    return this;
                } catch (XMLStreamException ex) {
                    throw new IOException(ex);
                }
            }
            // json and other formats
            if (processor != null) {
                processor.process(OutputStatus.OK, format, getResponse.source());
            }

        } catch (ElasticSearchException e) {
            logger.error(e.getMessage(), e);
            if (processor != null) {
                processor.process(OutputStatus.ERROR, format, e.getMessage().getBytes());
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            if (processor != null) {
                processor.process(OutputStatus.ERROR, format, e.getMessage().getBytes());
            }
        }
        return this;
    }

    private static byte[] jsonEmptyMessage(String message) {
        return ("{\"error\":404,\"message\":\"" + message + "\"}").getBytes();
    }

    private static byte[] jsonErrorMessage(String message) {
        return ("{\"error\":500,\"message\":\"" + message + "\"}").getBytes();
    }

    private String formatIndexType() {
        StringBuilder indexes = new StringBuilder();
        if (index != null) {
            for (String s : index) {
                if (s != null && s.length() > 0) {
                    if (indexes.length() > 0) {
                        indexes.append(',');
                    }
                    indexes.append(s);
                }
            }
        }
        if (indexes.length() == 0) {
            indexes.append('*');
        }
        StringBuilder types = new StringBuilder();
        if (type != null) {
            for (String s : type) {
                if (s != null && s.length() > 0) {
                    if (types.length() > 0) {
                        types.append(',');
                    }
                    types.append(s);
                }
            }
        }
        if (types.length() == 0) {
            types.append('*');
        }
        return indexes.append("/").append(types).toString();
    }

    private boolean hasIndex(String[] s) {
        if (s == null) {
            return false;
        }
        if (s.length == 0) {
            return false;
        }
        if (s[0] == null) {
            return false;
        }
        return true;
    }

    private boolean hasType(String[] s) {
        if (s == null) {
            return false;
        }
        if (s.length == 0) {
            return false;
        }
        if (s[0] == null) {
            return false;
        }
        return true;
    }

    private String[] fixIndexName(String[] s) {
        if (s == null) {
            return new String[]{"*"};
        }
        if (s.length == 0) {
            return new String[]{"*"};
        }
        for (int i = 0; i < s.length; i++) {
            if (s[i] == null || s[i].length() == 0) {
                s[i] = "*";
            }
        }
        return s;
    }
    private final static String DEFAULT_CLUSTER_NAME = "elasticsearch";
    private final static URI DEFAULT_URI = URI.create("es://interfaces:9300");

    private static URI findURI() {
        URI uri = DEFAULT_URI;
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
            logger.debug("the hostname is {}", hostname);
            Properties p = new Properties();
            try (InputStream in = ElasticsearchDAO.class.getResource("/org/xbib/elasticsearch/cluster.properties").openStream()) {
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

    private String findClusterName(URI uri) {
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

    private void connect(URI uri) throws UnknownHostException, SocketException, IOException {
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
}
