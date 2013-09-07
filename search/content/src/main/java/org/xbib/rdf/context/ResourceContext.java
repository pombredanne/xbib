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
package org.xbib.rdf.context;

import java.util.Collection;
import java.util.Map;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;
import org.xbib.rdf.ResourceFactory;
import org.xbib.rdf.xcontent.ContentBuilder;

/**
 * A Resource context. 
 * 
 * Resource contexts are useful in situation where you need to be aware
 * of the provenance of your data, or you want addressing resources to
 * different targets.
 * 
 */
public interface ResourceContext<R extends Resource> extends ResourceFactory<R> {

    /**
     * Set a new IRI namespace context
     * @param namespaces
     * @return
     */
    ResourceContext<R> newNamespaceContext(IRINamespaceContext namespaces);

    /**
     * Get IRI namespace context
     * @return
     */
    IRINamespaceContext namespaceContext();

    /**
     *
     * Set a new resource in this context.
     * @param resource
     * @return the current resource context
     */
    ResourceContext<R> setResource(R resource);

    /**
     * Get current resource in this context.
     * @return current resource
     */
    R resource();

    /**
     * Reset this context so the context becomes empty
     */
    ResourceContext<R> reset();

    /**
     * Prepare the context for output.
     * @return
     */
    ResourceContext<R> prepareForOutput();

    ContentBuilder<ResourceContext, R> contentBuilder();

}
