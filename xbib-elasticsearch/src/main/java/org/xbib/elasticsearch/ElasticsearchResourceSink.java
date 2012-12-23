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

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.xbib.elements.output.ElementOutput;
import org.xbib.rdf.BlankNode;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.JsonLdContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.xml.CompactingNamespaceContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticsearchResourceSink<C extends ResourceContext, R extends Resource>
        implements ElementOutput<C> {

    private final ElasticsearchIndexerInterface es;
    private static final AtomicInteger resourceCounter = new AtomicInteger(0);

    private final String defaultIndex;

    private final String defaultType;

    public ElasticsearchResourceSink(final ElasticsearchIndexerInterface es) {
        this.es = es;
        this.defaultIndex = es.index();
        this.defaultType = es.type();
    }

    public boolean enabled() {
        String enabled = System.getProperty(getClass().getName());
        return enabled == null || !"false".equalsIgnoreCase(enabled);
    }

    @Override
    public long getCounter() {
        return resourceCounter.longValue();
    }

    @Override
    public boolean output(C context) {
        try {
            ResourceIndexer<C, R> resourceIndexer = new ResourceIndexer<C, R>() {
                public void index(C context, R resource, String source) throws IOException {
                    String index = makeIndex(context, resource);
                    String type = makeType(context, resource);
                    String id = makeId(context, resource);
                    es.index(index, type, id, source);
                }

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
                    // full resource, build it with XContentBuilder
                    XContentBuilder builder = jsonBuilder();
                    builder.startObject();
                    if (context instanceof JsonLdContext) {
                        JsonLdContext jsonLdContext = (JsonLdContext)context;
                        // create @context
                        build(builder, context, jsonLdContext.newResource());
                        // create @id IRI
                        builder.field("@id", resource.id().toString());
                        if (resource.type() != null) {
                            // create @type IRI
                            builder.field("@type", resource.type().toString());
                        }
                    }
                    build(builder, context, resource);
                    builder.endObject();
                    resourceIndexer.index(context, resource, builder.string());
                }
                resourceCounter.incrementAndGet();
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void flush() {
        es.flush();
    }

    protected String makeIndex(C context, R resource) {
        String index = resource.id().getPath();
        if (index == null) {
            index = defaultIndex;
        }
        return index;
    }

    protected String makeType(C context, R resource) {
        String type = resource.id().getQuery();
        if (type == null) {
            type = defaultType;
        }
        return type;
    }

    protected String makeId(C context, R resource) {
        String id = resource.id().getFragment();
        if (id == null) {
            id = resource.id().toString();
        }
        return id;
    }

    protected <S extends Resource<?, ?, ?>, P extends Property, O extends Literal<?>>
    void build(XContentBuilder builder, C resourceContext, Resource<S, P, O> resource)
            throws IOException {
        CompactingNamespaceContext context = resourceContext.namespaceContext();
        // iterate over properties
        Iterator<P> it = resource.predicateSet(resource.subject()).iterator();
        while (it.hasNext()) {
            P predicate = it.next();
            Collection<O> values = resource.objectSet(predicate);
            if (values == null) {
                throw new IllegalArgumentException(
                        "can't build property value set for predicate URI " + predicate);
            }
            // drop values with size 0 silently
            if (values.size() == 1) {
                // single value
                O value = values.iterator().next();
                if (!(value instanceof BlankNode)) {
                    builder.field(context.compact(predicate.getURI()), value.nativeValue());
                }
            } else if (values.size() > 1) {
                // array of values
                Collection<O> properties = filterBlankNodes(values, new ArrayList<O>());
                if (!properties.isEmpty()) {
                    builder.startArray(context.compact(predicate.getURI()));
                    for (O value : properties) {
                        builder.value(value.nativeValue());
                    }
                    builder.endArray();
                }
            }
        }
        // then, iterate over resources
        Map<P, Collection<Resource<S, P, O>>> m = resource.resources();
        Iterator<P> resIt = m.keySet().iterator();
        while (resIt.hasNext()) {
            P predicate = resIt.next();
            Collection<Resource<S, P, O>> resources = m.get(predicate);
            // drop resources with size 0 silently
            if (resources.size() == 1) {
                // single resource
                builder.startObject(context.compact(predicate.getURI()));
                build(builder, resourceContext, resources.iterator().next());
                builder.endObject();
            } else if (resources.size() > 1) {
                // array of resources
                builder.startArray(context.compact(predicate.getURI()));
                for (Resource<S, P, O> child : resources) {
                    builder.startObject();
                    build(builder, resourceContext, child);
                    builder.endObject();
                }
                builder.endArray();
            }
        }
    }

    private <O extends Literal<?>>
    Collection<O> filterBlankNodes(Collection<O> objects, Collection<O> properties) {
        for (O object : objects) {
            if (object instanceof BlankNode) {
                continue;
            }
            properties.add(object);
        }
        return properties;
    }


}
