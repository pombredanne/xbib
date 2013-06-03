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
package org.xbib.atom.elasticsearch;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.provider.managed.FeedConfiguration;
import org.xbib.atom.AbderaFeedBuilder;
import org.xbib.atom.AtomFeedFactory;
import org.xbib.atom.AtomFeedProperties;
import org.xbib.elasticsearch.support.CQLSearchRequest;
import org.xbib.elasticsearch.support.CQLSearchResponse;
import org.xbib.elasticsearch.support.CQLSearchSupport;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

/**
 * Atom feed controller for Elasticsearch. The results are wrapped up in an Atom
 * feed format.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ElasticsearchAtomFeedController implements AtomFeedFactory {

    public final static String FEED_BASE_URI_KEY = "feed.base.uri";
    public final static String FEED_URI_PROPERTY_KEY = "feed.uri";
    public final static String FEED_AUTHOR_PROPERTY_KEY = "feed.author";
    public final static String FEED_TITLE_PATTERN_PROPERTY_KEY = "feed.title.pattern";
    public final static String FEED_SUBTITLE_PATTERN_PROPERTY_KEY = "feed.subtitle.pattern";
    public final static String FEED_RESULTSETLIMIT_FROM = "feed.resultset.limit.from";
    public final static String FEED_RESULTSETLIMIT_SIZE = "feed.resultset.limit.size";
    public final static String FEED_CONSTRUCTION_TIME_PATTERN_KEY = "feed.constructiontime.pattern";
    public final static String FEED_STYLESHEET_PROPERTY_KEY = "feed.stylesheet";
    public final static String FEED_SERVICE_PATH_KEY = "feed.service.path";
    public final static String FEED_INDEX = "feed.index";
    public final static String FEED_TYPE = "feed.type";
    
    private final static Logger logger = LoggerFactory.getLogger(ElasticsearchAtomFeedController.class.getName());
    protected AbderaFeedBuilder builder;
    private CQLSearchSupport support = new CQLSearchSupport();

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
     * @throws java.io.IOException
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
     * @param query the query
     * @return the Atom feed or null
     * @throws java.io.IOException if Atom feed can not be created
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
        String uriStr = properties.getProperty(FEED_URI_PROPERTY_KEY, "es://localhost:9300?es.cluster.name=joerg");
        URI uri = URI.create(uriStr);
        if (!support.isConnected()) {
            support.newClient(uri);
            if (!support.isConnected()) {
                throw new IOException("elasticsearch client not connected");
            }
        }
        String index = properties.getProperty(FEED_INDEX);
        String type =  properties.getProperty(FEED_TYPE);
        try {
            long t0 = System.currentTimeMillis();
            this.builder = new AbderaFeedBuilder(config, query);
            String mediaType = "application/xml";
            Logger logger = LoggerFactory.getLogger(mediaType, ElasticsearchAtomFeedController.class.getName());
            CQLSearchRequest request = support.newSearchRequest();
            CQLSearchResponse response =request.index(index)
                    .type(type)
                    .from(config.getFrom())
                    .size(config.getSize())
                    .cql(query)
                    .executeSearch(logger);
            response.to(builder);
            long t1 = System.currentTimeMillis();
            return builder.getFeed(query, t1 - t0,
                    -1L, config.getFrom(), config.getSize());
        } catch (Exception e) {
            logger.error("atom feed query " + query + " session is unresponsive", e);
            throw new IOException(e.getMessage());
        } finally {
            logger.info("atom feed query completed: {}", query);
        }
    }
}
