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
package org.xbib.rdf.io;

import java.io.Writer;
import java.net.URI;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.util.XMLEventConsumer;
import org.xbib.rdf.BlankNode;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.xml.SimpleNamespaceContext;

/**
 * Write resource to XML stream
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class XMLResourceWriter<S extends Resource<?, ?, ?>, P extends Property, O extends Literal<?>> {

    private final NamespaceContext context;
    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    public XMLResourceWriter() {
        this(SimpleNamespaceContext.getInstance());
    }

    public XMLResourceWriter(NamespaceContext context) {
        this.context = context;
    }

    public void toXML(Resource<S, P, O> resource, Writer w)
            throws XMLStreamException {
        XMLEventWriter xew = XMLOutputFactory.newInstance().createXMLEventWriter(w);
        URI resourceURI = URI.create(resource.getIdentifier().toString());
        // copy all resource statements, this will reconstruct the structure
        SimpleResource<S, P, O> xmlResource = new SimpleResource<S, P, O>(resourceURI);
        Iterator<Statement<S, P, O>> it = resource.iterator(true);
        while (it.hasNext()) {
            Statement<S, P, O> st = it.next();
            xmlResource.add(st);
        }
        // dump resource as XML
        String nsPrefix = resourceURI.getScheme();
        String name = resourceURI.getSchemeSpecificPart();
        String nsURI = context.getNamespaceURI(nsPrefix);
        writeResource(xew, xmlResource, new QName(nsURI, name, nsPrefix));
        xew.close();
    }

    /**
     * Write resource to XML event consumer
     *
     * @param consumer
     * @param resource
     * @param parent
     * @throws XMLStreamException
     */
    public void writeResource(XMLEventConsumer consumer, Resource<S, P, O> resource, QName parent)
            throws XMLStreamException {
        boolean startElementWritten = false;
        Iterator<Statement<S, P, O>> it = resource.iterator(false);
        while (it.hasNext()) {
            Statement<S, P, O> statement = it.next();
            if (!startElementWritten) {
                if (parent != null) {
                    consumer.add(eventFactory.createStartElement(parent, null, null));
                }
                startElementWritten = true;
            }
            writeStatement(consumer, statement);
        }
        if (!startElementWritten) {
            if (parent != null) {
                consumer.add(eventFactory.createStartElement(parent, null, null));
            }
            startElementWritten = true;
        }
        if (parent != null) {
            consumer.add(eventFactory.createEndElement(parent, null));
        }
    }

    private void writeStatement(XMLEventConsumer consumer, Statement<S, P, O> statement)
            throws XMLStreamException {
        P predicate = statement.getPredicate();
        O object = statement.getObject();
        String nsPrefix = predicate.getURI().getScheme();
        String name = predicate.getURI().getSchemeSpecificPart();
        String nsURI = context.getNamespaceURI(nsPrefix);
        if (object instanceof BlankNode) {
            writeResource(consumer, (Resource<S, P, O>) object, new QName(nsURI, name, nsPrefix));
        } else if (object instanceof Literal) {
            String literal = object.getValue().toString();
            consumer.add(eventFactory.createStartElement(nsPrefix, nsURI, name));
            consumer.add(eventFactory.createCharacters(literal));
            consumer.add(eventFactory.createEndElement(nsPrefix, nsURI, name));
        } else {
            throw new XMLStreamException("unknown object class: " + object.getClass().getName());
        }

    }
}
