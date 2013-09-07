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

import com.google.common.base.Predicate;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 * An abstract resource
 */
public abstract class AbstractResource<S extends Identifier, P extends Property, O extends Node>
        extends IdentifiableNode
        implements Resource<S, P, O>, Comparable<Resource<S, P, O>> {

    private final Logger logger = LoggerFactory.getLogger(AbstractResource.class.getName());

    protected Multimap<P, Node> attributes = LinkedHashMultimap.create();

    protected Map<IRI, O> resourceMap = new LinkedHashMap();

    private S subject;

    private boolean deleted;

    @Override
    public AbstractResource<S, P, O> id(IRI identifier) {
        super.id(identifier);
        this.subject = (S) new IdentifiableNode().id(identifier);
        return this;
    }

    @Override
    public Resource<S, P, O> subject(S subject) {
        this.subject = subject;
        return this;
    }

    public Resource<S, P, O> subject(IRI subject) {
        this.subject = (S) new IdentifiableNode().id(subject);
        return this;
    }

    @Override
    public S subject() {
        return subject;
    }

    @Override
    public Resource<S, P, O> add(Triple<S,P,O> triple) {
        if (triple == null) {
            return this;
        }
        if (triple.subject().id() == null || triple.subject().id().equals(id())) {
            add(triple.predicate(), triple.object());
        } else {
            Resource<S, P, O> r =  (Resource<S, P, O>)resourceMap.get(triple.subject().id());
            if (r == null) {
                logger.warn("ignoring triple: {}, no resource in {}", triple, this);
                return this;
            }
            return r.add(triple);
        }
        return this;
    }

    @Override
    public Resource<S, P, O> add(P predicate, O object) {
        attributes.put(predicate, object);
        if (object instanceof IdentifiableNode) {
            resourceMap.put(((IdentifiableNode) object).id(), object);
        }
        return this;
    }

    @Override
    public Resource<S, P, O> add(P predicate, IRI iri) {
        return add(predicate, (O)new IdentifiableNode().id(iri));
    }

    @Override
    public Resource<S, P, O> add(P predicate, Literal<O> literal) {
        // drop null literals silently
        if (literal != null) {
            attributes.put(predicate, literal);
        }
        return this;
    }    

    @Override
    public Resource<S, P, O> add(P predicate, Resource<S, P, O> resource) {
        if (resource == null) {
            return this;
        }
        if (resource.id() == null) {
            resource.id(super.id());
            Resource<S, P, O> r = newResource(predicate);
            Iterator<Triple<S,P,O>> it = resource.iterator();
            while (it.hasNext()) {
                r.add(it.next());
            }
        } else {
            attributes.put(predicate, resource);
        }
        return this;
    }

    @Override
    public Resource<S, P, O> a(IRI externalResource) {
        add("rdf:type", externalResource);
        return this;
    }

    @Override
    public Set<P> predicateSet(S subject) {
        return attributes.keySet();
    }

    @Override
    public Collection<O> objects(P predicate) {
        return (Collection<O>) attributes.get(predicate);
    }

    @Override
    public O literal(P predicate) {
        return attributes.containsKey(predicate) ?
                (O)attributes.get(predicate).iterator().next() : null;
    }
    
    @Override
    public Map<P, Collection<Node>> nodeMap() {
        return attributes.asMap();
    }

    /**
     * Compact a predicate. Under the predicate, there is a single blank node
     * object with a single value for the same predicate. In such case, the
     * blank node can be removed and the single value can be promoted to the
     * predicate.
     *
     * @param predicate the predicate
     */
    @Override
    public void compactPredicate(P predicate) {
        Collection<Resource<S, P, O>> res = resources().get(predicate);
        if (res.size() == 1) {
            Collection<Node> keep = new LinkedList();
            for (O o : objects(predicate)) {
                if (o instanceof Literal) {
                    keep.add(o);
                } else if (o instanceof IdentifiableNode) {
                    if (!((IdentifiableNode)o).isBlank()) {
                        keep.add(o);
                    }
                }
            }
            Resource<S, P, O> resource = res.iterator().next();
            keep.addAll(new LinkedList(resource.objects(predicate)));
            attributes.removeAll(predicate);
            attributes.putAll(predicate, keep);
        } else {
            logger.warn("more than one resource for {}", predicate);
        }
    }

    @Override
    public int compareTo(Resource<S, P, O> resource) {
        return toString().compareTo(resource.toString());
    }

    @Override
    public void clear() {
        attributes.clear();
    }

    @Override
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    @Override
    public int size() {
        return attributes.size();
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
    public String toString() {
        return "<" + (super.id() != null ? super.id() : "") + ">";
    }

    @Override
    public Map<P, Collection<Resource<S,P,O>>> resources() {
        Map<P, Collection<Resource<S,P,O>>> map = new HashMap();
        Multimap<P, Node> filtered = Multimaps.filterValues(attributes, resources);
        // copy resources...
        for (Map.Entry<P,Collection<Node>> me : filtered.asMap().entrySet()) {
            Collection<Resource<S,P,O>> c = new ArrayList();
            for (Node n : me.getValue()) {
                c.add((Resource<S,P,O>)n);
            }
            map.put(me.getKey(),c);
        }
        return map;
    }

    private final static Predicate<Node> resources = new Predicate<Node>() {
        @Override
        public boolean apply(Node n) {
            return n instanceof Resource;
        }
    };

    private final static Predicate<Node> literals = new Predicate<Node>() {
        @Override
        public boolean apply(Node n) {
            return n instanceof Literal;
        }
    };
}
