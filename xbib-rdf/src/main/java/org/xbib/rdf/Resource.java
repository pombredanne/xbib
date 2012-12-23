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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.xbib.iri.IRI;
import org.xbib.rdf.context.ResourceContext;

/**
 * A Resource is an iterable over statements of
 * subjects, predicates, and objects, based upon Literal.
 * It has predicate multimaps for associated resources and local properties.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public interface Resource<S,P,O>
        extends Literal<O>, Iterable<Statement<S,P,O>> {

    /**
     * Set the resource identifier
     *
     * @param identifier a resource identifier
     */
    Resource<S,P,O> id(IRI identifier);

    /**
     * Set the resource identifier
     *
     * @param identifier a resource identifier
     */
    Resource<S,P,O> id(String identifier);
    
    /**
     * Get the resource identifier
     * @return the resource identifier
     */
    IRI id();

    /**
     * Set resource context
     *
     * @param context
     */
    Resource<S,P,O> context(ResourceContext context);

    /**
     * Get resource context
     * @return the resource context
     */
    ResourceContext context();

    /**
     * Set the subject of this resource
     * @param subject
     */
    Resource<S,P,O> subject(S subject);

    /**
     * Get subject of this resource
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
    Resource<S,P,O> property(String predicate, String object);
    
    /**
     * Add a property to this resource.
     *
     * @param predicate a predicate identifier in its string representation form
     * @param object an object in its string representation form
     * @return the new resource with the property added
     */
    Resource<S,P,O> property(String predicate, O object);

    /**
     * Add a property to this resource with a string object value
     *
     * @param predicate a predicate identifier
     * @param object an object in its string representation form
     * @return the new resource with the property added
     */
    Resource<S,P,O> property(P predicate, String object);

    /**
     * Add a property to this resource
     *
     * @param predicate a predicate identifier
     * @param object a literal
     * @return the new resource with the property added
     */
    Resource<S,P,O> property(P predicate, O object);

    /**
     * Add a statement to this resource
     * @param statement
     * @return true if statement has been added, otherwise false
     */
    boolean add(Statement<S,P,O> statement);

    /**
     * Add another resource to this resource
     * @return true if the resource has been added, otherwise false
     * @param predicate
     * @param resource
     */
    boolean add(P predicate, Resource<S,P,O> resource);
    
    boolean add(String predicate,  Resource<S,P,O> resource);
    
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
    Resource<S,P,O> setDeleted(boolean delete);
    
    /**
     * Check if marker for resource deletion is set
     * 
     * @return true if the marker ist set
     */
    boolean isDeleted();

    /**
     * Create an anonymous resource and associate it with this resource.
     * If the resource under the given resource identifier already exists, the
     * existing resource is returned.
     *
     * @param predicate the predicate ID for the resource
     * @return the new anonymous resource
     */
    Resource<S,P,O> newResource(String predicate);

    /**
     * Create an anomymous blank node
     * @return a blank node
     */
    BlankNode<S,P,O> newBlankNode();

    /**
     * Create a named blank node
     * @param nodeID
     * @return a blank node
     */
    BlankNode<S,P,O> newBlankNode(String nodeID);

    /**
     * Create a literal
     *
     * @param value
     * @return a literal
     */
    Literal<?> newLiteral(String value);

    /**
     * Create a statement
     *
     * @param subject
     * @param predicate
     * @param object
     * @return a statement
     */
    Statement<S,P,O> newStatement(S subject, P predicate, O object);

    /**
     * Return the map of predicates for all associated resources
     * @return a map  of predicates for all associated resources
     */
    Map<P, Collection<Resource<S, P, O>>> resources();

    /**
     * Return the set of predicates for a given subject
     * @param subject
     * @return set of predicates
     */
    Set<P> predicateSet(S subject);
    
    Set<P> predicateSet(String subject);

    /**
     * Return object set for a given predicate
     * @param predicate
     * @return set of objects
     */
    Collection<O> objectSet(P predicate);
    
    Collection<O> objectSet(String predicate);
    
    /**
     * Get iterator over properties only or over
     * properties and resources (full)
     * @param full
     * @return iterator over statements
     */
    Iterator<Statement<S,P,O>> iterator(boolean full);

    /**
     * Create a fresh properties multimap
     * @return properties multimap
     */
    Multimap<P, O> newProperties();

    /**
     * Create a resource multimap
     * @return a multimap
     */
    Multimap<P, Resource<S, P, O>> newResources();

    /**
     * Create a subject
     * @param subject
     * @return a subject
     */
    S toSubject(Object subject);

    /**
     * Create a predicate
     * @param predicate
     * @return a predicate
     */
    //P toPredicate(Object predicate);

    /**
     * Create an object
     * @param object
     * @return an object
     */
    O toObject(Object object);
    
    /**
     * Compact a predicate. Under the predicate, there is a single blank node object
     * with a single value for the same predicate. In such case, the 
     * blank node can be removed and the single value can be promoted to
     * the predicate.
     * @param predicate 
     */
    void compact(P predicate);

}
