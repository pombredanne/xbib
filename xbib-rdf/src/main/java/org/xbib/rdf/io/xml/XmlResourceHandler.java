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

import java.util.Stack;
import javax.xml.namespace.QName;
import org.xbib.rdf.Factory;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;

/**
 * The XML resource handler can create nested RDF resources from arbitrary XML.
 */
public abstract class XmlResourceHandler extends AbstractXmlHandler {

    private final Factory factory = Factory.getInstance();

    private Stack<Element> stack = new Stack();

    public XmlResourceHandler(ResourceContext resourceContext) {
        super(resourceContext);
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
        // nested resource creation
        // always create newResource, even if there will be only a single literal. We will compact later.
        Resource r = stack.peek()
                .getResource()
                .newResource(makePrefix(name.getPrefix()) + ":" + name.getLocalPart());
        stack.push(new Element(r));
    }

    @Override
    public void addToPredicate(String content) {
        stack.peek().setValue(content);
    }

    @Override
    public void closePredicate(QName parent, QName name, int level) {
        Property p = (Property)factory.asPredicate(makePrefix(name.getPrefix()) + ":" + name.getLocalPart());
        Element element = stack.pop();
        if (level < 0) {
            // it's a newResource
            stack.peek().getResource().add(p, element.getResource());
        } else {
            // it's a property
            if (content() != null) {
                element.getResource().property(p,
                        resourceContext.resource().toObject(content()));
                // compact because it has only a single value
                stack.peek().getResource().compact(p);
            }
        }
    }

    class Element {

        private final Resource resource;
        private Object value;

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
