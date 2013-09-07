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
package org.xbib.atom;

import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.RouteManager;
import org.apache.abdera.protocol.server.provider.managed.BasicServerConfiguration;
import org.apache.abdera.protocol.server.provider.managed.ManagedProvider;
import org.apache.abdera.protocol.server.provider.managed.ServerConfiguration;

/**
 * Abdera provider for our search engine feeds.
 *
 * The URI pattern is implemented by the RouteManager with a simple
 * 'feed web app' pattern.
 *
 * Right now, only feed generation is supported, representing a search engine
 * result set. Atom service or entry generation are not implemented here.
 *
 * The single URI pattern consists therefore of only two parts. First, the
 * web app container URI, configured by web.xml for the Abdera servlet,
 * and second, the feed name, which is the base name of the Abdera property file
 * located in the package apache.abdera
 *
 * In this Abdera property file, the Abdera service class is specified,
 * beside the configFile property, which points to a resource which
 * is also a property file, but for guiding our AtomFeedController.
 * 
 */
public class AbderaProvider extends ManagedProvider {

    public AbderaProvider() {
        super();
        init();
    }

    private void init() {
        RouteManager routeManager = new RouteManager()
                .addRoute("feed", "/:webapp/:feed", TargetType.TYPE_COLLECTION);
        setTargetBuilder(routeManager);
        setTargetResolver(routeManager);
    }

    @Override
    public CollectionAdapter getCollectionAdapter(RequestContext request) {
        try {
            return getCollectionAdapterManager(request).getAdapter(request.getTarget().getParameter("feed"));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected ServerConfiguration getServerConfiguration(
            RequestContext request) {
        return new BasicServerConfiguration(request);
    }
}
