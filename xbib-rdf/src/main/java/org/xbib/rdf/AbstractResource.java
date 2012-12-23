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
package org.xbib.rdf;

import com.google.common.collect.Multimap;
import org.xbib.iri.IRI;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.simple.SimpleResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An abstract resource
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public abstract class AbstractResource<S extends Resource<?, ?, ?>, P extends Property, O extends Literal<?>>
        implements Resource<S, P, O>, Comparable<Resource<S, P, O>> {

    protected transient final Factory<S, P, O> factory = Factory.getInstance();
    protected final P TYPE;
    protected final P LANG;
    protected ResourceContext context;
    protected IRI identifier;
    protected S subject;
    protected O object;
    protected Multimap<P, O> properties;
    protected Multimap<P, Resource<S, P, O>> resources;
    private boolean deleted;

    public AbstractResource() {
        this.properties = newProperties();
        this.resources = newResources();
        TYPE = factory.asPredicate(RDF.RDF_TYPE);
        LANG = factory.asPredicate(RDF.RDF_LANGUAGE);
    }

    protected AbstractResource(IRI identifier) {
        this();
        id(identifier);
    }

    protected AbstractResource(O value) {
        this();
        object(value);
    }

    public AbstractResource<S, P, O> context(ResourceContext context) {
        this.context = context;
        return this;
    }

    public ResourceContext context() {
        return context;
    }

    @Override
    public final AbstractResource<S, P, O> id(IRI identifier) {
        this.identifier = identifier;
        subject(toSubject(identifier));
        return this;
    }

    @Override
    public final AbstractResource<S, P, O> id(String identifier) {
        this.identifier = IRI.create(identifier);
        subject(toSubject(this.identifier));
        return this;
    }

    @Override
    public IRI id() {
        return identifier;
    }

    @Override
    public Resource<S, P, O> subject(S subject) {
        this.subject = subject;
        return this;
    }

    @Override
    public S subject() {
        return subject;
    }

    @Override
    public Resource<S, P, O> object(O object) {
        this.object = object;
        return this;
    }

    @Override
    public O object() {
        return object;
    }

    @Override
    public Resource<S, P, O> type(IRI type) {
        add(TYPE, new SimpleResource().id(type));
        return this;
    }

    @Override
    public IRI type() {
        Collection<Resource<S, P, O>> c = resources.get(TYPE);
        return c != null ? c.iterator().hasNext() ?
                c.iterator().next().id()
                : null : null;
    }

    @Override
    public Resource<S, P, O> language(String lang) {
        property(LANG, lang);
        return this;
    }

    @Override
    public String language() {
        Collection<O> c = properties.get(LANG);
        return c != null ? c.iterator().hasNext() ?
                c.iterator().next().nativeValue().toString()
                : null : null;
    }

    @Override
    public Resource<S, P, O> property(String predicate, String value) {
        return property(factory.asPredicate(predicate), value);
    }

    @Override
    public Resource<S, P, O> property(String predicate, O value) {
        return property(factory.asPredicate(predicate), value);
    }

    @Override
    public Resource<S, P, O> property(P predicate, String value) {
        return property(predicate, toObject(value));
    }

    @Override
    public Resource<S, P, O> property(P predicate, O object) {
        if (predicate == null) {
            throw new IllegalArgumentException("unable to add a null predicate");
        }
        // drop null objects silently
        if (object != null) {
            properties.put(predicate, object);
        }
        return this;
    }

    @Override
    public Resource<S, P, O> newResource(String predicate) {
        P p = factory.asPredicate(predicate);
        BlankNode<S, P, O> bNode = newBlankNode();
        resources.put(p, bNode);
        properties.put(p, (O) bNode);
        return bNode;
    }

    @Override
    public Map<P, Collection<Resource<S, P, O>>> resources() {
        return resources.asMap();
    }

    @Override
    public boolean add(Statement<S, P, O> statement) {
        // auto-create a subject if this resource does not have one.
        // Can also be a blank node.
        if (subject() == null && statement.getSubject() != null) {
            subject(statement.getSubject());
            id(statement.getSubject().id());
        }
        if (statement.getSubject() instanceof BlankNode) {
            // accept only blank node subjects as property if this resource
            // blank node identifier matches
            boolean b = subject().id() != null && subject().id().equals(statement.getSubject().id());
            if (b) {
                // this belongs to us
                properties.put(statement.getPredicate(), statement.getObject());
                return true;
            } else {
                // delegate to child resources
                boolean hasAdded = false;
                for (Resource<S, P, O> resource : resources.values()) {
                    if (resource.add(statement)) {
                        hasAdded = true;
                        break;
                    }
                }
                return hasAdded;
            }
        }
        if (statement.getObject() instanceof BlankNode) {
            resources.put(statement.getPredicate(),
                    (BlankNode<S, P, O>) statement.getObject());
            properties.put(statement.getPredicate(), statement.getObject());
            //}
        } else {
            properties.put(statement.getPredicate(), statement.getObject());
        }
        return true;
    }

    @Override
    public boolean add(P predicate, Resource<S, P, O> resource) {
        if (resource == null) {
            throw new IllegalArgumentException("unable to add null resource");
        }
        if (resource.id() == null) {
            resource.id(identifier);
            BlankNode<S, P, O> bNode = newBlankNode();
            resources.put(predicate, bNode);
            properties.put(predicate, (O) bNode);
            // copy statements from resource
            Iterator<Statement<S, P, O>> it = resource.iterator();
            while (it.hasNext()) {
                Statement<S, P, O> stmt = it.next();
                bNode.add(stmt);
            }
        } else {
            resources.put(predicate, resource);
            properties.put(predicate, (O) resource);
            // copy all statements from resource
            Iterator<Statement<S, P, O>> it = resource.iterator();
            while (it.hasNext()) {
                Statement<S, P, O> stmt = it.next();
                add(stmt);
            }
        }
        return true;
    }

    @Override
    public boolean add(String predicate, Resource<S, P, O> resource) {
        return add(factory.asPredicate(predicate), resource);
    }

    @Override
    public Set<P> predicateSet(S subject) {
        if (subject == null) {
            return new HashSet();
        }
        if (subject().equals(subject)) {
            return properties.keySet();
        } else {
            for (Resource<S, P, O> r : resources.values()) {
                Set<P> p = r.predicateSet(subject);
                if (p != null) {
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public Set<P> predicateSet(String subject) {
        return predicateSet(toSubject(subject));
    }

    @Override
    public Collection<O> objectSet(P predicate) {
        return properties.get(predicate);
    }

    @Override
    public Collection<O> objectSet(String predicate) {
        return properties.get(factory.asPredicate(predicate));
    }

    /**
     * Compact a predicate. Under the predicate, there is a single blank node
     * object with a single value for the same predicate. In such case, the
     * blank node can be removed and the single value can be promoted to the
     * predicate.
     *
     * @param predicate
     */
    @Override
    public void compact(P predicate) {
        Collection<Resource<S, P, O>> c = resources.get(predicate);
        if (c.size() == 1) {
            Resource<S, P, O> r = c.iterator().next();
            if (r instanceof BlankNode) {
                Collection<O> values = r.objectSet(predicate);
                // get the single value and put it to properties
                if (values.size() == 1) {
                    resources.removeAll(predicate);
                    properties.removeAll(predicate);
                    properties.put(predicate, values.iterator().next());
                }
            }
        }
    }

    @Override
    public int compareTo(Resource<S, P, O> resource) {
        return toString().compareTo(resource.toString());
    }

    @Override
    public void clear() {
        properties.clear();
        resources.clear();
    }

    @Override
    public boolean isEmpty() {
        return properties.isEmpty() && resources.isEmpty();
    }

    @Override
    public Resource<S, P, O> setDeleted(boolean delete) {
        this.deleted = delete;
        return this;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public Iterator<Statement<S, P, O>> iterator() {
        return iterator(true);
    }

    @Override
    public Iterator<Statement<S, P, O>> iterator(boolean recursion) {
        return new StatementIterator(this, recursion);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            if (object != null) {
                return object.toString();
            }
            if (identifier != null) {
                return identifier.toASCIIString();
            }
            return "<>";
        } else {
            StringBuilder sb = new StringBuilder();
            Iterator<Statement<S, P, O>> it = iterator();
            while (it.hasNext()) {
                sb.append(it.next()).append("\n");
            }
            return sb.toString();
        }
    }
}
