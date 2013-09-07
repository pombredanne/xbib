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

import java.util.Iterator;
import java.util.Stack;
import javax.xml.namespace.QName;

import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Abstract XML handler
 *
 */
public abstract class AbstractXmlHandler extends DefaultHandler implements XmlHandler {

    private final Logger logger = LoggerFactory.getLogger(AbstractXmlHandler.class.getName());

    private final ResourceContext resourceContext;

    private final StringBuilder content = new StringBuilder();

    private final Stack<QName> parents = new Stack();

    private TripleListener listener;

    private String defaultPrefix;

    private String defaultNamespace;

    private int lastlevel;

    public AbstractXmlHandler(ResourceContext context) {
        this.resourceContext = context;
    }

    public ResourceContext resourceContext() {
        return resourceContext;
    }

    public AbstractXmlHandler setDefaultNamespace(String prefix, String namespaceURI) {
        this.defaultPrefix = prefix;
        this.defaultNamespace = namespaceURI;
        resourceContext.namespaceContext().addNamespace(prefix, namespaceURI);
        return this;
    }

    public AbstractXmlHandler setListener(TripleListener listener) {
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
        try {
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
            parents.push(name);
            lastlevel = level;
            if (atts != null) {
                // transform attributes as if they were elements, but with a '@' prefix
                for (int i = 0; i < atts.getLength(); i++) {
                    String attrValue = atts.getValue(i);
                    if (attrValue != null && !attrValue.isEmpty()) {
                        String attrName = '@' + atts.getLocalName(i);
                        if (!skip(new QName(atts.getURI(i), attrName, atts.getQName(i)))) {
                            startElement(atts.getURI(i), attrName , atts.getQName(i), null);
                            characters(attrValue.toCharArray(), 0, attrValue.length());
                            endElement(atts.getURI(i), attrName, atts.getQName(i));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void endElement(String nsURI, String localname, String qname) throws SAXException {
        try {
            QName name = makeQName(nsURI, localname, qname);
            if (skip(name)) {
                content.setLength(0);
                return;
            }
            int level = parents.size();
            parents.pop();
            identify(name, content(), resourceContext.resource().id());
            if (!isResourceDelimiter(name)) {
                closePredicate(parents.peek(), name, level - lastlevel);
            }
            content.setLength(0);
            lastlevel = level;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        content.append(new String(chars, start, length));
        addToPredicate(parents.peek(), content());
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        resourceContext.namespaceContext().addNamespace(makePrefix(prefix), uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // we do not remove namespaces, or you will get trouble in RDF serializations...
        //resourceContext.namespaceContext().removeNamespace(prefix);
    }

    protected String makePrefix(String name) {
        return name.replaceAll("[^a-zA-Z]+", "");
    }

    protected QName makeQName(String nsURI, String localname, String qname) {
        String prefix = resourceContext.namespaceContext().getPrefix(nsURI);
        return new QName(!isEmpty(nsURI) ? nsURI : defaultNamespace,
                !isEmpty(localname) ? localname : qname,
                !isEmpty(prefix) ? prefix : defaultPrefix);
    }

    public String content() {
        String s = content.toString().trim();
        return s.length() > 0 ? s : null;
    }

    protected void openResource() {
        resourceContext.newResource();
    }

    protected void closeResource() {
        boolean empty = resourceContext.resource().isEmpty();
        if (empty) {
            return;
        }
        if (listener != null) {
            listener.newIdentifier(resourceContext.resource().id());
            Iterator<Triple> it = resourceContext.resource().iterator();
            while (it.hasNext()) {
                listener.triple(it.next());
            }
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public abstract boolean isResourceDelimiter(QName name);

    public abstract boolean skip(QName name);

    public abstract void identify(QName name, String value, IRI identifier);

    public abstract void openPredicate(QName parent, QName child, int level);

    public abstract void addToPredicate(QName parent, String content);

    public abstract void closePredicate(QName parent, QName child, int level);
}
