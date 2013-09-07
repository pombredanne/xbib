/**
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

import org.xbib.rdf.Resource;

/**
 * An abstract resource context.
 *
 * @param <R>
 */
public abstract class AbstractResourceContext<R extends Resource> implements ResourceContext<R> {

    private IRINamespaceContext namespaces;

    private R resource;

    public ResourceContext<R> newNamespaceContext() {
        this.namespaces = IRINamespaceContext.newInstance();
        return this;
    }

    public ResourceContext<R> newNamespaceContext(IRINamespaceContext namespaces) {
        this.namespaces = namespaces;
        return this;
    }

    public IRINamespaceContext namespaceContext() {
        if (namespaces == null) {
            this.namespaces = IRINamespaceContext.newInstance();
        }
        return namespaces;
    }

    @Override
    public ResourceContext<R> setResource(R resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public R resource() {
        return resource;
    }

    @Override
    public ResourceContext<R> reset() {
        resource = null;
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(resource.toString()).append("\n");
        return sb.toString();
    }
}
