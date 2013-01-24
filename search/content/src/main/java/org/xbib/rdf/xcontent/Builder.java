package org.xbib.rdf.xcontent;

import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.JsonLdContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.xml.CompactingNamespaceContext;

public class Builder<C extends ResourceContext, R extends Resource> {
    
    public <S extends Identifier, P extends Property, O extends Node> String build(C context, R resource)
            throws IOException {
        XContentBuilder builder = jsonBuilder();
        builder.startObject();
        if (context instanceof JsonLdContext) {
            JsonLdContext jsonLdContext = (JsonLdContext) context;
            // create @context
            build(builder, context, jsonLdContext.newResource());
            // create @id IRI
            builder.field("@id", resource.id().toString());
            /*if (resource. != null) {
             // create @type IRI
             builder.field("@type", resource.type().toString());
             }*/
        }
        build(builder, context, resource);
        builder.endObject();
        return builder.string();
    }

    protected <S extends Identifier, P extends Property, O extends Node> void build(XContentBuilder builder, C resourceContext, Resource<S, P, O> resource)
            throws IOException {
        CompactingNamespaceContext context = resourceContext.namespaceContext();
        // iterate over properties
        Iterator<P> it = resource.predicateSet(resource.subject()).iterator();
        while (it.hasNext()) {
            P predicate = it.next();
            Collection<O> values = resource.objects(predicate);
            if (values == null) {
                throw new IllegalArgumentException("can't build property value set for predicate URI " + predicate);
            }
            // drop values with size 0 silently
            if (values.size() == 1) {
                // single value
                O value = values.iterator().next();
                if (!(value instanceof Identifier)) {
                    builder.field(context.compact(predicate.id()), value.toString()); // nativeValue
                }
            } else if (values.size() > 1) {
                // array of values
                Collection<O> properties = filterBlankNodes(values);
                if (!properties.isEmpty()) {
                    builder.startArray(context.compact(predicate.id()));
                    for (O value : properties) {
                        builder.value(value.toString()); // nativeValue
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
                builder.startObject(context.compact(predicate.id()));
                build(builder, resourceContext, resources.iterator().next());
                builder.endObject();
            } else if (resources.size() > 1) {
                // array of resources
                builder.startArray(context.compact(predicate.id()));
                for (Resource<S, P, O> child : resources) {
                    builder.startObject();
                    build(builder, resourceContext, child);
                    builder.endObject();
                }
                builder.endArray();
            }
        }
    }

    private <O extends Node> Collection<O> filterBlankNodes(Collection<O> objects) {
        Collection<O> nodes = new ArrayList();
        for (O object : objects) {
            if (object instanceof Identifier) {
                continue;
            }
            nodes.add(object);
        }
        return nodes;
    }
}