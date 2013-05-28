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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.xbib.elasticsearch.support.ingest.ClientIngest;
import org.xbib.elements.output.ElementOutput;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.xcontent.Builder;

public class ElasticsearchResourceSink<C extends ResourceContext, R extends Resource>
        implements ElementOutput<C> {

    private final ClientIngest ingester;
    private final AtomicInteger resourceCounter = new AtomicInteger(0);
    private boolean enabled;

    public ElasticsearchResourceSink(final ClientIngest ingester) {
        this.ingester = ingester;
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

    final ResourceIndexer<R> resourceIndexer = new ResourceIndexer<R>() {
        @Override
        public void index(R resource, String source) throws IOException {
            String index = makeIndex(resource);
            String type = makeType(resource);
            String id = makeId(resource);
            ingester.indexDocument(index, type, id, source);
        }

        @Override
        public void delete(R resource) throws IOException {
            String index = makeIndex(resource);
            String type = makeType(resource);
            String id = makeId(resource);
            ingester.deleteDocument(index, type, id);
        }
    };

    @Override
    public void output(C context) throws IOException {
        Builder<C, R> builder = new Builder();
        Map<IRI,R> map = context.asMap();
        if (map.isEmpty()) {
            output(context, builder, (R)context.resource());
        } else {
            for (R resource : map.values()) {
                output(context, builder, resource);
            }
        }
    }

    private void output(C context, Builder<C, R> builder, R resource) throws IOException{
        if (resource.id() == null) {
            return;
        }
        if (resource.isEmpty()) {
            return;
        }
        if (resource.isDeleted()) {
            resourceIndexer.delete(resource);
        } else {
            resourceIndexer.index(resource, builder.build(context, resource));
        }
        resourceCounter.incrementAndGet();
    }

    public void flush() {
        ingester.flush();
    }

    /**
     * The IRI host is the Elasticsearch index
     *
     * @param resource
     * @return
     */
    protected String makeIndex(R resource) {
        return resource.id().getHost();
    }

    /**
     * The IRI query is the Elasticsearch index type
     *
     * @param resource
     * @return
     */
    protected String makeType(R resource) {
        return resource.id().getQuery();
    }

    /**
     * The IRI fragment is the Elasticsearch document ID
     *
     * @param resource
     * @return
     */
    protected String makeId(R resource) {
        String id = resource.id().getFragment();
        if (id == null) {
            id = resource.id().toString();
        }
        return id;
    }
}
