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
import java.util.Iterator;
import java.util.Stack;
import javax.xml.namespace.QName;
import org.xbib.rdf.ResourceContext;
import org.xbib.rdf.Statement;
import org.xbib.rdf.io.StatementListener;
import org.xbib.xml.NamespaceContext;
import org.xbib.xml.SimpleNamespaceContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class XmlHandler extends DefaultHandler {

    protected final NamespaceContext namespaceContext;
    protected final ResourceContext resourceContext;
    protected StatementListener listener;
    private String defaultPrefix;
    private String defaultNamespace;
    private StringBuilder content = new StringBuilder();
    private Stack<QName> parents = new Stack();
    private URI identifier;
    private int lastlevel;

    public XmlHandler(ResourceContext context) {
        this(context, SimpleNamespaceContext.getInstance());
    }

    public XmlHandler(ResourceContext resourceContext, NamespaceContext namespaceContext) {
        this.resourceContext = resourceContext;
        this.namespaceContext = namespaceContext;
    }

    public XmlHandler setDefaultNamespace(String prefix, String namespaceURI) {
        this.defaultPrefix = prefix;
        this.defaultNamespace = namespaceURI;
        namespaceContext.addNamespace(prefix, namespaceURI);
        return this;
    }

    public XmlHandler setListener(StatementListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void startDocument() throws SAXException {
        openResource();
        parents.push(new QName("_"));
    }

    @Override
    public void endDocument() throws SAXException {
        closeResource();
    }

    @Override
    public void startElement(String nsURI, String localname, String qname, Attributes atts) throws SAXException {
        QName name = makeQName(nsURI, localname, qname);
        boolean delimiter = isResourceDelimiter(name);
        if (delimiter) {
            closeResource();
            openResource();
        }
        if (skip(name)) {
            return;
        }
        int level = parents.size();
        if (!delimiter) {
            openPredicate(parents.peek(), name, lastlevel - level);
        }
        if (atts != null) {
            for (int i = 0; i < atts.getLength(); i++) {
                startElement(atts.getURI(i), atts.getLocalName(i), atts.getQName(i), null);
                endElement(atts.getURI(i), '@'+atts.getLocalName(i), atts.getQName(i));
            }
        }
        parents.push(name);
        lastlevel = level;
    }

    @Override
    public void endElement(String nsURI, String localname, String qname) throws SAXException {
        QName name = makeQName(nsURI, localname, qname);
        if (skip(name)) {
            return;
        }
        int level = parents.size();
        parents.pop();
        URI id = identify(name, content(), identifier);
        if (id != null) {
            setIdentifier(id);
        }
        if (!isResourceDelimiter(name)) {
            closePredicate(parents.peek(), name, level - lastlevel);
        }
        content.setLength(0);
        lastlevel = level;
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        content.append(new String(chars, start, length));
        addToPredicate(content.toString());
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        namespaceContext.addNamespace(makePrefix(prefix), uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    protected String makePrefix(String name) {
        return name.replaceAll("[^a-zA-Z]+", "");
    }

    protected QName makeQName(String nsURI, String localname, String qname) {
        String prefix = namespaceContext.getPrefix(nsURI);
        return new QName(!isEmpty(nsURI) ? nsURI : defaultNamespace,
                !isEmpty(localname) ? localname : qname,
                !isEmpty(prefix) ? prefix : defaultPrefix);
    }

    public String content() {
        String s = content.toString().trim();
        return s.length() > 0 ? s : null;
    }

    public void setIdentifier(URI identifier) {
        this.identifier = identifier;
    }

    public void openResource() {
        resourceContext.newResource();
    }

    public void closeResource() {
        boolean empty = resourceContext.resource().isEmpty();
        if (identifier == null && !empty) {
            throw new IllegalArgumentException("no resource identifier set");
        }
        if (empty) {
            return;
        }
        resourceContext.resource().id(identifier);
        if (listener != null) {
            listener.newIdentifier(identifier);
            Iterator<Statement> it = resourceContext.resource().iterator(true);
            while (it.hasNext()) {
                listener.statement(it.next());
            }
        }
        identifier = null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public abstract boolean isResourceDelimiter(QName name);

    public abstract boolean skip(QName name);

    public abstract URI identify(QName name, String value, URI identifier);

    public abstract void openPredicate(QName parent, QName child, int level);

    public abstract void addToPredicate(String content);

    public abstract void closePredicate(QName parent, QName child, int level);
}
