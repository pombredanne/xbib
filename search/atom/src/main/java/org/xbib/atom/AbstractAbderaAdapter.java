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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.provider.managed.FeedConfiguration;
import org.apache.abdera.protocol.server.provider.managed.ManagedCollectionAdapter;
import org.xbib.io.util.URIUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 * An abstract Abdera managed collection service
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public abstract class AbstractAbderaAdapter extends ManagedCollectionAdapter {

    private final static Logger logger = LoggerFactory.getLogger(AbstractAbderaAdapter.class.getName());

    protected abstract AtomFeedFactory getFeedFactory();

    /**
     * Construct Adapter for Abdera
     */
    public AbstractAbderaAdapter(Abdera abdera, FeedConfiguration config) {
        super(abdera, config);
    }

    /**
     * Get Atom feed
     *
     * @param request a request context
     *
     * @return a response context
     */
    @Override
    public ResponseContext getFeed(final RequestContext request) {
        int from = 0;
        int size = 10;
        String query = null;
        try {
            URI uri = request.getUri().toURI();
            Map<String, String> params = URIUtil.parseQueryString(uri, "UTF-8");
            query = params.get("q");
            if (query == null) {
                query = params.get("query");
            }
            from = Integer.parseInt(params.get("from"));
            size = Integer.parseInt(params.get("size"));
        } catch (UnsupportedEncodingException ex) {
        } catch (URISyntaxException ex) {
        } catch (Exception ex) {
        }
        if (query == null) {
            return ProviderHelper.badrequest(request, "bad query parameter");
        }
        try {
            Feed feed = getFeedFactory().createFeed(request, // request context
                    config, // feed configuration
                    query, // query string
                    from, // result set size offset 
                    size // result set size
                    );
            if (feed == null) {
                logger.error("unable to create feed");
                ProviderHelper.servererror(request, "could not generate feed", null);
            }
            return ProviderHelper.returnBase(feed.getDocument(), 200, null);
        } catch (Exception e) {
            return ProviderHelper.servererror(request, e.getMessage(), e);
        }
    }

    /**
     * Delete entry
     *
     * @param request the request context
     *
     * @return the response context
     */
    @Override
    public ResponseContext deleteEntry(RequestContext request) {
        return ProviderHelper.notsupported(request);
    }

    /**
     * Get entry
     *
     * @param request the request context
     *
     * @return the response context
     */
    @Override
    public ResponseContext getEntry(RequestContext request) {
        return ProviderHelper.notsupported(request);
    }

    /**
     * Post entry
     *
     * @param request the request context
     *
     * @return the response context
     */
    @Override
    public ResponseContext postEntry(RequestContext request) {
        return ProviderHelper.notsupported(request);
    }

    /**
     * Put entry
     *
     * @param request the request context
     *
     * @return the response context
     */
    @Override
    public ResponseContext putEntry(RequestContext request) {
        return ProviderHelper.notsupported(request);
    }
}
