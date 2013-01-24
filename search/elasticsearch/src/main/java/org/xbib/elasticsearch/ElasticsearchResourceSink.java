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
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.xbib.elements.output.ElementOutput;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.xcontent.Builder;

public class ElasticsearchResourceSink<C extends ResourceContext, R extends Resource>
        implements ElementOutput<C> {

    private final ElasticsearchIndexerInterface es;
    private final Builder<C, R> builder = new Builder();
    private final AtomicInteger resourceCounter = new AtomicInteger(0);
    private final String defaultIndex;
    private final String defaultType;
    private boolean enabled;

    public ElasticsearchResourceSink(final ElasticsearchIndexerInterface es) {
        this.es = es;
        this.defaultIndex = es.index();
        this.defaultType = es.type();
    }

    @Override
    public boolean enabled() {
        this.enabled = Boolean.parseBoolean(System.getProperty(getClass().getName())) || enabled;
        return enabled;
    }
    
    @Override
    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public long getCounter() {
        return resourceCounter.longValue();
    }

    @Override
    public void output(C context) throws IOException {
        ResourceIndexer<C, R> resourceIndexer = new ResourceIndexer<C, R>() {
            @Override
            public void index(C context, R resource, String source) throws IOException {
                String index = makeIndex(context, resource);
                String type = makeType(context, resource);
                String id = makeId(context, resource);
                es.index(index, type, id, source);
            }

            @Override
            public void delete(C context, R resource) throws IOException {
                String index = makeIndex(context, resource);
                String type = makeType(context, resource);
                String id = makeId(context, resource);
                es.delete(index, type, id);
            }
        };
        Iterator<R> it = context.resources();
        while (it.hasNext()) {
            R resource = it.next();
            if (resource.id() == null) {
                // no resource ID
                continue;
            }
            if (resource.isEmpty()) {
                // no properties or resources in the resource
                continue;
            }
            if (resource.isDeleted()) {
                // resource shall be deleted
                resourceIndexer.delete(context, resource);
            } else {
                resourceIndexer.index(context, resource, builder.build(context, resource));
            }
            resourceCounter.incrementAndGet();
        }
    }

    public void flush() {
        es.flush();
    }

    /**
     * The IRI host is the Elasticsearch index
     *
     * @param context
     * @param resource
     * @return
     */
    protected String makeIndex(C context, R resource) {
        String index = resource.id().getHost();
        if (index == null) {
            index = defaultIndex;
        }
        return index;
    }

    /**
     * The IRI query is the Elasticsearch index type
     *
     * @param context
     * @param resource
     * @return
     */
    protected String makeType(C context, R resource) {
        String type = resource.id().getQuery();
        if (type == null) {
            type = defaultType;
        }
        return type;
    }

    /**
     * The IRI fragment is the Elasticsearch document ID
     *
     * @param context
     * @param resource
     * @return
     */
    protected String makeId(C context, R resource) {
        String id = resource.id().getFragment();
        if (id == null) {
            id = resource.id().toString();
        }
        return id;
    }
}
