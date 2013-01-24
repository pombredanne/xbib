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
package org.xbib.rdf.io.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Iterator;
import java.util.Stack;
import javax.xml.namespace.QName;

import org.xbib.iri.IRI;
import org.xbib.json.JsonXmlReader;
import org.xbib.json.JsonXmlReaderFactory;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.io.Triplifier;
import org.xbib.rdf.simple.Factory;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.xml.XMLFilterReader;
import org.xbib.xml.XMLNamespaceContext;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A triplifier for JSON (not JSON-LD)
 *
 * UNTESTED
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class JsonReader<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>>
        implements Triplifier<S, P, O> {

    private Factory<S,P,O> factory = Factory.getInstance();
    private XMLNamespaceContext context;
    private XMLNamespaceContext ignore;
    private StatementListener listener;
    private Resource<S, P, O> resource;
    private Resource<S, P, O> rootResource;
    private IRI identifier;
    private QName root;

    public JsonReader() {
        this(XMLNamespaceContext.getInstance(), null);
    }

    public JsonReader(QName root) {
        this(XMLNamespaceContext.getInstance(), root);
    }

    public JsonReader(XMLNamespaceContext context, QName root) {
        this.context = context;
        this.root = root;
    }

    @Override
    public JsonReader setListener(StatementListener listener) {
        this.listener = listener;
        return this;
    }

    public JsonReader setIdentifier(IRI identifier) {
        this.identifier = identifier;
        return this;
    }

    public JsonReader setIgnoreNamespaces(XMLNamespaceContext ignore) {
        this.ignore = ignore;
        return this;
    }

    @Override
    public JsonReader parse(InputStream in) throws IOException {
        return parse(new InputStreamReader(in, "UTF-8"));
    }

    @Override
    public JsonReader parse(Reader reader) throws IOException {
        try {
            parse(new InputSource(reader));
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        return this;
    }

    public JsonReader parse(InputSource source) throws IOException, SAXException {
        parse(JsonXmlReaderFactory.createJsonXmlReader(root), source);
        return this;
    }

    public JsonReader parse(JsonXmlReader reader, InputSource source) throws IOException, SAXException {
        Handler xmlHandler = new Handler();
        reader.setContentHandler(xmlHandler);
        reader.parse(source);
        return this;
    }

    public JsonReader parse(XMLFilterReader reader, InputSource source) throws IOException, SAXException {
        Handler xmlHandler = new Handler();
        reader.setContentHandler(xmlHandler);
        reader.parse(source);
        return this;
    }

    private String prefix(String name) {
        return name.replaceAll("[^a-zA-Z]+", "");
    }

    class Handler extends DefaultHandler {

        StringBuilder content = new StringBuilder();
        Stack<QName> stack = new Stack();
        Stack<Resource> resources = new Stack();

        @Override
        public void startDocument() throws SAXException {
            resource = new SimpleResource();
            rootResource = resource;
        }

        @Override
        public void endDocument() throws SAXException {
            rootResource.id(identifier);
            listener.newIdentifier(identifier);
            Iterator<Statement<S, P, O>> it = rootResource.iterator();
            while (it.hasNext()) {
                Statement<S, P, O> st = it.next();
                listener.statement(st);
            }
            root = null;
        }

        @Override
        public void startElement(String nsURI, String localname, String qname, Attributes atts) throws SAXException {
            if (ignore != null && ignore.getPrefix(nsURI) != null) {
                return;
            }
            if (!stack.empty()) {
                QName name = stack.peek();
                P property = factory.asPredicate(new IRI().curi(name.getPrefix(), name.getLocalPart()));
                resources.push(resource);
                resource = resource.newResource(property);
            }
            String prefix = context.getPrefix(nsURI);
            if (prefix == null) {
                throw new SAXException("namespace context does not contain prefix for namespace '"
                        + nsURI + "', local name = '" + localname + "', qname = '" + qname + "'");
            }
            stack.push(new QName(nsURI, localname, prefix));
            content.setLength(0);
        }

        @Override
        public void endElement(String nsURI, String localname, String qname) throws SAXException {
            if (ignore != null && ignore.getPrefix(nsURI) != null) {
                return;
            }
            QName name = stack.pop();
            P property = factory.asPredicate(new IRI().curi(name.getPrefix(), name.getLocalPart()));
            if (content.length() > 0) {
                resource.add(property, content.toString());
            }
            if (!resources.empty()) {
                resource = resources.pop();
            }
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            content.append(new String(chars, start, length));
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            if (ignore != null && ignore.getPrefix(uri) != null) {
                return;
            }
            context.addNamespace(prefix(prefix), uri);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }
    }
}
