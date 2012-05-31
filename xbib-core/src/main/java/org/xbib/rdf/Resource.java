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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.xbib.io.Data;

/**
 * A Resource is an iterable over statements of
 * subjects, predicates, and objects, based upon Literal.
 * It has predicate multimaps for associated resources and local properties.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public interface Resource<S,P,O>
        extends Literal<O>, Iterable<Statement<S,P,O>>, Data {

    /**
     * Set the resource identifier
     *
     * @param identifier a resource identifier
     */
    void setIdentifier(URI identifier);

    /**
     * Get the resource identifier
     * @return the resource identifier
     */
    URI getIdentifier();

    /**
     * Set the subject of this resource
     * @param subject
     */
    void setSubject(S subject);

    /**
     * Get subject of this resource
     * @return the subject
     */
    S getSubject();

    /**
     * Add a property to this resource
     *
     * @param property a predicate identifier in its string representation form
     * @param object an object in its string representation form
     * @return the new resource with the property added
     */
    Resource<S,P,O> addProperty(String property, String object);

    /**
     * Add a property to this resource
     *
     * @param property a predicate identifier
     * @param object an object in its string representation form
     * @return the new resource with the property added
     */
    Resource<S,P,O> addProperty(P property, String object);

    /**
     * Add a property to this resource
     *
     * @param property a predicate identifier
     * @param object a literal
     * @return the new resource with the property added
     */
    Resource<S,P,O> addProperty(P property, O object);

    /**
     * Add a statement to this resource
     * @param statement
     * @return true if statement has been added, otherwise false
     */
    boolean add(Statement<S,P,O> statement);

    /**
     * Add another resource to this resource
     * @return true if the resource has been added, otherwise false
     * @param resource
     */
    boolean addResource(P property, Resource<S,P,O> resource);
    
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
    void delete(boolean delete);
    
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
     * @param predicate the predicate for the resource
     * @return the new anonymous resource
     */
    Resource<S,P,O> createResource(String predicate);

    /**
     * Create a resource
     * @param predicate
     * @return a resource
     */
    Resource<S,P,O> createResource(P predicate);
    
    /**
     * Create an anomymous blank node
     * @return a blank node
     */
    BlankNode<S,P,O> createBlankNode();

    /**
     * Create a named blank node
     * @param nodeID
     * @return a blank node
     */
    BlankNode<S,P,O> createBlankNode(String nodeID);

    /**
     * Create a literal
     *
     * @param value
     * @return a literal
     */
    Literal<?> createLiteral(String value);

    /**
     * Create a literal
     *
     * @param value
     * @param language
     * @return a literal
     */
    Literal<?> createLiteral(String value, String language);

    /**
     * Create literal
     *
     * @param value
     * @param encodingScheme
     * @return a literal
     */
    Literal<?> createLiteral(String value, URI encodingScheme);

    /**
     * Create a statement
     *
     * @param subject
     * @param predicate
     * @param object
     * @return a statement
     */
    Statement<S,P,O> createStatement(S subject, P predicate, O object);

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

    /**
     * Return object set for a given predicate
     * @param predicate
     * @return set of objects
     */
    Collection<O> objectSet(P predicate);
    
    /**
     * Get iterator over properties only (no recursion) or over
     * properties and resources (recursion)
     * @param recursion
     * @return iterator over statements
     */
    Iterator<Statement<S,P,O>> iterator(boolean recursion);

    /**
     * Create a fresh properties multimap
     * @return properties multimap
     */
    Multimap<P, O> createProperties();

    /**
     * Create a resource multimap
     * @return a multimap
     */
    Multimap<P, Resource<S, P, O>> createResources();

    /**
     * Create a subject
     * @param subject
     * @return a subject
     */
    S createSubject(Object subject);

    /**
     * Create a predicate
     * @param predicate
     * @return a predicate
     */
    P createPredicate(Object predicate);

    /**
     * Create an object
     * @param object
     * @return an object
     */
    O createObject(Object object);

}
