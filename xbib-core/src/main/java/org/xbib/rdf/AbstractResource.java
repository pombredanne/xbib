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
import java.net.URI;
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

    protected URI identifier;
    protected S subject;
    protected O object;
    protected Multimap<P, O> properties;
    protected Multimap<P, Resource<S, P, O>> resources;
    private boolean deleted;

    public AbstractResource() {
        this.properties = createProperties();
        this.resources = createResources();
    }

    protected AbstractResource(URI identifier) {
        this();
        setIdentifier(identifier);
    }

    protected AbstractResource(O value) {
        this();
        setValue(value);
    }

    @Override
    public final void setIdentifier(URI identifier) {
        this.identifier = identifier;
        setSubject(createSubject(identifier));
    }

    public final void setIdentifier(String identifier) {
        setIdentifier(URI.create("id:" + identifier));
    }

    @Override
    public URI getIdentifier() {
        return identifier;
    }

    @Override
    public void setSubject(S subject) {
        this.subject = subject;
    }

    @Override
    public S getSubject() {
        return subject;
    }

    @Override
    public void setValue(O object) {
        this.object = object;
    }

    @Override
    public O getValue() {
        return object;
    }

    @Override
    public void setType(URI type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLanguage(String lang) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLanguage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource<S, P, O> addProperty(String predicate, String value) {
        return addProperty(createPredicate(predicate), value);
    }

    @Override
    public Resource<S, P, O> addProperty(P predicate, String value) {
        return addProperty(predicate, createObject(value));
    }

    @Override
    public Resource<S, P, O> addProperty(P predicate, O object) {
        if (predicate == null) {
            throw new IllegalArgumentException("unable to add null predicate");
        }
        // drop null objects silently
        if (object != null) {
            properties.put(predicate, object);
        }
        return this;
    }

    @Override
    public Resource<S, P, O> createResource(String predicateStr) {
        return createResource(createPredicate(predicateStr));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Resource<S, P, O> createResource(P predicate) {
        BlankNode<S, P, O> bNode = createBlankNode();
        resources.put(predicate, bNode);
        properties.put(predicate, (O) bNode);
        return bNode;
    }

    @Override
    public Map<P, Collection<Resource<S, P, O>>> resources() {
        return resources.asMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean add(Statement<S, P, O> statement) {
        // auto-create a subject if this resource does not have one.
        // Can also be a blank node.
        if (getSubject() == null && statement.getSubject() != null) {
            setSubject(statement.getSubject());
            setIdentifier(statement.getSubject().getIdentifier());
        }
        if (statement.getSubject() instanceof BlankNode) {
            // accept only blank node subjects as property if this resource
            // blank node identifier matches
            boolean b = statement.getSubject().getIdentifier().equals(getSubject().getIdentifier());
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
            //if (!properties.containsKey(statement.getPredicate())) {
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
    public boolean addResource(P predicate, Resource<S, P, O> resource) {
        if (resource == null) {
            throw new IllegalArgumentException("unable to add null resource");
        }
        if (resource.getIdentifier() == null) {
            resource.setIdentifier(identifier);
            BlankNode<S, P, O> bNode = createBlankNode();
            resources.put(predicate, bNode);
            properties.put(predicate, (O) bNode);
            // take over all statements from resource
            Iterator<Statement<S, P, O>> it = resource.iterator();
            while (it.hasNext()) {
                Statement<S, P, O> stmt = it.next();
                bNode.add(stmt);
            }
        } else {
            resources.put(predicate, resource);
            properties.put(predicate, (O) resource);
            // take over all statements from resource
            Iterator<Statement<S, P, O>> it = resource.iterator();
            while (it.hasNext()) {
                Statement<S, P, O> stmt = it.next();
                add(stmt);
            }
        }
        return true;
    }

    @Override
    public P createPredicate(final Object predicate) {
        return (P) new Property(URI.create(predicate.toString()));
    }

    @Override
    public Set<P> predicateSet(S subject) {
        if (subject == null) {
            return new HashSet();
        }
        if (getSubject().equals(subject)) {
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
    public Collection<O> objectSet(P predicate) {
        return properties.get(predicate);
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
    public void delete(boolean delete) {
        this.deleted = delete;
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
