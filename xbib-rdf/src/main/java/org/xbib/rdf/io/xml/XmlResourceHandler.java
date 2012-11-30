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
package org.xbib.rdf.io.xml;

import java.net.URI;
import java.util.Stack;
import javax.xml.namespace.QName;
import org.xbib.rdf.Resource;
import org.xbib.rdf.ResourceContext;
import org.xbib.xml.NamespaceContext;

public abstract class XmlResourceHandler extends XmlHandler {

    private Stack<Element> stack = new Stack();

    public XmlResourceHandler(ResourceContext resourceContext) {
        super(resourceContext);
    }

    public XmlResourceHandler(ResourceContext resourceContext, NamespaceContext context) {
        super(resourceContext, context);
    }

    @Override
    public void openResource() {
        super.openResource();
        stack.push(new Element(resourceContext.resource()));
    }

    @Override
    public void closeResource() {
        super.closeResource();
        stack.clear();
    }

    @Override
    public void openPredicate(QName parent, QName name, int level) {
        URI uri = URI.create(makePrefix(name.getPrefix()) + ":" + name.getLocalPart());
        // always create resource (will compact later)
        Resource r = stack.peek().getResource().newResource(uri.toString());
        stack.push(new Element(r));
    }

    @Override
    public void addToPredicate(String content) {
        stack.peek().setValue(content);
    }

    @Override
    public void closePredicate(QName parent, QName name, int level) {
        URI uri = URI.create(makePrefix(name.getPrefix()) + ":" + name.getLocalPart());
        Element element = stack.pop();
        if (level < 0) {
            // it's a resource
            stack.peek().getResource().add(resourceContext.resource().toPredicate(uri), element.getResource());
        } else {
            // it's a property
            if (content() != null) {
                element.getResource().property(resourceContext.resource().toPredicate(uri),
                        resourceContext.resource().toObject(content()));
                // compact predicate because it has only a single value
                stack.peek().getResource().compact(resourceContext.resource().toPredicate(uri));
            }
        }
    }

    class Element {

        final Resource resource;
        Object value;

        Element(Resource resource) {
            this.resource = resource;
        }

        public Resource getResource() {
            return resource;
        }

        public void setValue(Object value) {
            String s = value.toString().trim();
            this.value = s.length() > 0 ? s : null;
        }

        public Object getValue() {
            return value;
        }
    }
}
