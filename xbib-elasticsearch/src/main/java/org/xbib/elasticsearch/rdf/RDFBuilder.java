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
package org.xbib.elasticsearch.rdf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.xbib.rdf.BlankNode;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.xml.NamespaceContext;
import org.xbib.xml.SimpleNamespaceContext;

public class RDFBuilder<S extends Resource<?, ?, ?>, P extends Property, O extends Literal<?>> {

    private static final NamespaceContext context = SimpleNamespaceContext.getInstance();    
    
    public void build(XContentBuilder builder, Resource<S, P, O> resource)
            throws IOException, URISyntaxException {
        // iterate over properties
        Iterator<P> it = resource.predicateSet(resource.subject()).iterator();
        while (it.hasNext()) {
            P predicate = it.next();
            Collection<O> values = resource.objectSet(predicate);
            if (values == null) {
                throw new IllegalArgumentException(
                        "can't build property value set for predicate URI "
                        + predicate);
            }
            // drop values with size 0 silently
            if (values.size() == 1) {
                // single value
                O value = values.iterator().next();
                if (!(value instanceof BlankNode)) {
                    builder.field(context.abbreviate(predicate.getURI()), value.nativeValue() );
                }
            } else if (values.size() > 1) {
                // array of values
                Collection<O> properties = filterBlankNodes(values, new ArrayList<O>());
                if (!properties.isEmpty()) {
                    builder.startArray(context.abbreviate(predicate.getURI()));
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
                builder.startObject(context.abbreviate(predicate.getURI()));
                build(builder, resources.iterator().next());
                builder.endObject();
            } else if (resources.size() > 1) {
                // array of resources
                builder.startArray(context.abbreviate(predicate.getURI()));
                for (Resource<S, P, O> child : resources) {
                    builder.startObject();
                    build(builder, child);
                    builder.endObject();
                }
                builder.endArray();
            }
        }
    }

    private Collection<O> filterBlankNodes(Collection<O> objects, Collection<O> properties) {
        for (O object : objects) {
            if (object instanceof BlankNode) {
                continue;
            }
            properties.add(object);
        }
        return properties;
    }
    
    
}
