/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.atom;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.provider.managed.FeedConfiguration;
import org.xbib.elasticsearch.xml.ES;
import org.xbib.elasticsearch.ElasticsearchConnection;
import org.xbib.elasticsearch.ElasticsearchSession;
import org.xbib.elasticsearch.QueryResult;
import org.xbib.elasticsearch.QueryResultAction;
import org.xbib.io.util.URIUtil;
import org.xbib.json.JsonXmlStreamer;
import org.xbib.json.JsonXmlValueMode;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 *  Atom feed controller for Elasticsearch DSL JSON query.
 *  The results are wrapped up in an Atom feed format.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ElasticsearchAtomFeedController implements AtomFeedFactory {
    
    final String FEED_BASE_URI_KEY = "feed.base.uri";
    final String FEED_URI_PROPERTY_KEY = "feed.uri";
    final String FEED_AUTHOR_PROPERTY_KEY = "feed.author";
    final String FEED_TITLE_PATTERN_PROPERTY_KEY = "feed.title.pattern";
    final String FEED_SUBTITLE_PATTERN_PROPERTY_KEY = "feed.subtitle.pattern";
    final String FEED_RESULTSETLIMIT_FROM = "feed.resultset.limit.from";
    final String FEED_RESULTSETLIMIT_SIZE = "feed.resultset.limit.size";
    final String FEED_CONSTRUCTION_TIME_PATTERN_KEY = "feed.constructiontime.pattern";
    final String FEED_STYLESHEET_PROPERTY_KEY = "feed.stylesheet";
    final String FEED_SERVICE_PATH_KEY = "feed.service.path";
    /** the logger */
    private final static Logger logger = LoggerFactory.getLogger(ElasticsearchAtomFeedController.class.getName());
    private final QueryResultAction defaultAction = new ElasticSearchAction();
    protected AbderaFeedBuilder builder;
    
    public ElasticsearchAtomFeedController() {
    }
    
    public Feed createFeed(Properties properties, String query, int from, int size)
            throws IOException {
        return createFeed(Abdera.getInstance(),
                "", "", "",
                AtomFeedProperties.getFeedConfiguration(query, properties, null),
                query, from, size);
    }

    /**
     * Create Atom feed
     *
     * @param config the feed configuration
     * @param query the query
     * @return
     * @throws IOException
     */
    @Override
    public Feed createFeed(RequestContext request, FeedConfiguration config,
            String query, int from, int size) throws IOException {
        if (config == null) {
            throw new IOException("feed configuration must not be null");
        }
        return createFeed(request.getAbdera(),
                request.getBaseUri().toASCIIString(),
                request.getContextPath(),
                config.getHref(request),
                config,
                query, from, size);
    }

    /**
     * Create Atom feed
     *
     * @param abdera the Abdera instance
     * @param feedURI the feed URI
     * @param feedAuthor the feed author
     * @param feedConfigLocation the feed config location
     * @param query the query
     * @return the Atom feed or null
     * @throws IOException if Atom feed can not be created
     */
    public Feed createFeed(Abdera abdera,
            String baseURI, String contextPath, String servicePath,
            FeedConfiguration config,
            String query, int from, int size) throws IOException {
        
        AtomFeedProperties properties = new AtomFeedProperties(config);
        properties.setAbdera(abdera);
        properties.setBaseURI(baseURI);
        properties.setContextPath(contextPath);
        properties.setServicePath(servicePath);
        properties.setFrom(from);
        properties.setSize(size);
        return createFeed(properties, query);
    }
    
    public Feed createFeed(AtomFeedProperties config,
            String query) throws IOException {
        // load properties from feed config
        String feedConfigLocation = config.getFeedConfigLocation();
        Properties properties = new Properties();
        if (feedConfigLocation != null) {
            InputStream in = getClass().getResourceAsStream(feedConfigLocation);
            if (in != null) {
                properties.load(in);
            } else {
                throw new IOException("feed config not found: " + feedConfigLocation);
            }
        }
        // transfer our properties to AtomFeedProperties
        config.setTitlePattern(properties.getProperty(FEED_TITLE_PATTERN_PROPERTY_KEY));
        config.setSubtitlePattern(properties.getProperty(FEED_SUBTITLE_PATTERN_PROPERTY_KEY));
        config.setTimePattern(properties.getProperty(FEED_CONSTRUCTION_TIME_PATTERN_KEY));
        config.setStylesheet(properties.getProperty(FEED_STYLESHEET_PROPERTY_KEY));
        if (properties.containsKey(FEED_SERVICE_PATH_KEY)) {
            config.setServicePath(properties.getProperty(FEED_SERVICE_PATH_KEY));
        }        
        String uriStr = properties.getProperty(FEED_URI_PROPERTY_KEY, "sniff://localhost:9300");
        URI uri = URI.create(uriStr);
        Properties p = URIUtil.getPropertiesFromURI(uri);
        String[] index = p.getProperty("index") != null ? p.getProperty("index").split(",") : null;
        String[] type = p.getProperty("type") != null ? p.getProperty("type").split(",") : null;
        ElasticsearchConnection connection = ElasticsearchConnection.getInstance();
        ElasticsearchSession session = connection.createSession();
        // we need not to setOutputStream
        try {
            this.builder = new AbderaFeedBuilder(config, query);
            QueryResultAction action = getAction();
            action.setSession(session);
            action.setIndex(index);
            action.setType(type);
            action.setFrom(config.getFrom());
            action.setSize(config.getSize());
            long t0 = System.currentTimeMillis();
            // will set index and type from session properties and use process() method for result stream to put it in 'builder'
            action.searchAndProcess(QueryResult.Format.JSON, query);            
            long t1 = System.currentTimeMillis();
            return builder.getFeed(query, t1 - t0, 
                    action.getTookInMillis(), action.getFrom(), action.getSize());
        } catch (IOException e) {
            logger.error("atom feed query " + query + " session is unresponsive", e);
            throw e;
        } finally {
            session.close();
            connection.close();
            logger.info("atom feed query completed: {}", query);
        }
    }
    
    protected QueryResultAction getAction() {
        return defaultAction;
    }
    
    class ElasticSearchAction extends QueryResultAction {
        
        @Override
        public void process(InputStream in) throws IOException {
            JsonXmlStreamer jsonXml = new JsonXmlStreamer(JsonXmlValueMode.SKIP_EMPTY_VALUES);
            try {
                jsonXml.toXML(in, builder, new QName(ES.NS_URI, "result", ES.NS_PREFIX));
            } catch (XMLStreamException ex) {
                logger.error(ex.getMessage(), ex);
                throw new IOException(ex);
            }
        }
    }
}
