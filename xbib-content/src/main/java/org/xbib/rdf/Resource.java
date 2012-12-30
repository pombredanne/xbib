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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.xbib.iri.IRI;
import org.xbib.rdf.context.ResourceContext;

/**
 * A Resource is an iterable over statements of subjects, predicates, and
 * objects, based upon Literal. It has predicate multimaps for associated
 * resources and local properties.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public interface Resource<S extends Identifier, P extends Property, O extends Node>
        extends Identifier, Iterable<Statement<S, P, O>> {

    @Override
    Resource<S, P, O> id(IRI id);

    /**
     * Set resource context
     *
     * @param context
     */
    Resource<S, P, O> context(ResourceContext context);

    /**
     * Get resource context
     *
     * @return the resource context
     */
    ResourceContext context();

    /**
     * Set the subject of this resource
     *
     * @param subject
     */
    Resource<S, P, O> subject(S subject);

    /**
     * Get subject of this resource
     *
     * @return the subject
     */
    S subject();

    /**
     * Add a property to this resource.
     *
     * @param predicate a predicate identifier in its string representation form
     * @param object an object in its string representation form
     * @return the new resource with the property added
     */
    Resource<S, P, O> add(String predicate, String object);

    /**
     * Add a property to this resource.
     *
     * @param predicate a predicate identifier in its string representation form
     * @param object an object in its string representation form
     * @return the new resource with the property added
     */
    Resource<S, P, O> add(String predicate, Literal literal);

    /**
     * Add a property to this resource with a string object value
     *
     * @param predicate a predicate identifier
     * @param object an object in its string representation form
     * @return the new resource with the property added
     */
    Resource<S, P, O> add(P predicate, String object);

    /**
     * Add a property to this resource
     *
     * @param predicate a predicate identifier
     * @param object a literal
     * @return the new resource with the property added
     */
    Resource<S, P, O> add(P predicate, Literal literal);

    Resource<S, P, O> add(P predicate, IRI object);

    /**
     * Add another resource to this resource
     *
     * @param predicate
     * @param resource
     */
    Resource<S, P, O> add(P predicate, Resource<S, P, O> resource);

    Resource<S, P, O> add(String predicate, Resource<S, P, O> resource);

    Map<P, Collection<Node>> nodeMap();

    /**
     * Create an anonymous resource and associate it with this resource. If the
     * resource under the given resource identifier already exists, the existing
     * resource is returned.
     *
     * @param predicate the predicate ID for the resource
     * @return the new anonymous resource
     */
    Resource<S, P, O> newResource(IRI predicate);

    Resource<S, P, O> newResource(P predicate);

    Resource<S, P, O> newResource(String predicate);

    Map<P, Collection<Resource<S,P,O>>> resources();
    
    /**
     * Return the map of predicates for all associated resources
     *
     * @return a map of predicates for all associated resources
     */
    //Map<P, Collection<Resource<S, P, O>>> resources();
    /**
     * Return the set of predicates for a given subject
     *
     * @param subject
     * @return set of predicates
     */
    Set<P> predicateSet(S subject);

    Set<P> predicateSet(String subject);

    /**
     * Return object set for a given predicate
     *
     * @param predicate
     * @return set of objects
     */
    Collection<O> objects(P predicate);

    Collection<O> objects(String predicate);

    /**
     * Add a statement to this resource
     *
     * @param statement
     */
    Resource<S, P, O> add(Statement<S, P, O> statement);

    /**
     * Get statemen iterator over properties
     *
     * @return statements
     */
    Iterator<Statement<S, P, O>> iterator();

    Iterator<Statement<S, P, O>> propertyIterator();

    /**
     * Compact a predicate. Under the predicate, there is a single blank node
     * object with a single value for the same predicate. In such case, the
     * blank node can be removed and the single value can be promoted to the
     * predicate.
     *
     * @param predicate
     */
    void compactPredicate(P predicate);

    /**
     * Remove all properties and resources from this resource
     */
    void clear();

    /**
     * Check if resource is empty, if it has no properties and no resources
     */
    boolean isEmpty();

    /**
     * Set marker for resource deletion
     */
    Resource<S, P, O> setDeleted(boolean delete);

    /**
     * Check if marker for resource deletion is set
     *
     * @return true if the marker ist set
     */
    boolean isDeleted();
}
