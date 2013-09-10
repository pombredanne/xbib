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
package org.xbib.oai.util.rdf;

import org.xbib.iri.IRI;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.util.MetadataHandler;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.xml.XmlResourceHandler;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 *  RDF metadata handler
 */
public class RdfMetadataHandler extends MetadataHandler implements OAIConstants {

    private RdfResourceHandler handler;

    private ResourceContext resourceContext;

    private RdfOutput rdfOutput = new RdfOutput();

    private IRINamespaceContext context;

    public static IRINamespaceContext getDefaultContext() {
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace(DC_PREFIX, DC_NS_URI);
        context.addNamespace(OAIDC_NS_PREFIX, OAIDC_NS_URI);
        return context;
    }

    public RdfMetadataHandler() {
        this(getDefaultContext());
    }

    public RdfMetadataHandler(IRINamespaceContext context) {
        this.context = context;
        this.resourceContext = new SimpleResourceContext();
        resourceContext.newNamespaceContext(context);
        resourceContext.newResource();
        // set up our default handler
        this.handler = new RdfResourceHandler(resourceContext);
        handler.setDefaultNamespace(NS_PREFIX, NS_URI);
        handler.setListener(rdfOutput);
    }

    public IRINamespaceContext getContext() {
        return context;
    }

    public RdfMetadataHandler setResourceContext(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
        return this;
    }

    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    public RdfMetadataHandler setHandler(RdfResourceHandler handler) {
        this.handler = handler;
        handler.setDefaultNamespace(NS_PREFIX, NS_URI);
        handler.setListener(rdfOutput);
        return this;
    }

    public XmlResourceHandler getHandler() {
        return handler;
    }

    public RdfMetadataHandler setOutput(RdfOutput rdfOutput) {
        handler.setListener(rdfOutput);
        this.rdfOutput = rdfOutput;
        return this;
    }

    @Override
    public void startDocument() throws SAXException {
        if (handler != null) {
            handler.startDocument();
        }
    }

    /**
     * At the end of each OAI metadata, the resource context receives the identifier from
     * the metadata header. The resource context is pushed to the RDF output.
     * Any IOException is converted to a SAXException.
     *
     * @throws SAXException
     */
    @Override
    public void endDocument() throws SAXException {
        String id = getHeader().getIdentifier().trim();
        if (handler != null) {
            handler.identify(null, id, null);
            resourceContext.resource().id(IRI.create(id));
            handler.endDocument();
            try {
                rdfOutput.output(resourceContext);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String namespaceURI) throws SAXException {
        if (handler != null) {
            handler.startPrefixMapping(prefix, namespaceURI);
            if (prefix.isEmpty()) {
                handler.setDefaultNamespace("oai", namespaceURI);
            }
        }
    }

    @Override
    public void endPrefixMapping(String string) throws SAXException {
        if (handler != null) {
            handler.endPrefixMapping(string);
        }
    }

    @Override
    public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
        if (handler != null) {
            handler.startElement(ns, localname, string2, atrbts);
        }
    }

    @Override
    public void endElement(String ns, String localname, String string2) throws SAXException {
        if (handler != null) {
            handler.endElement(ns, localname, string2);
        }
    }

    @Override
    public void characters(char[] chars, int i, int i1) throws SAXException {
        if (handler != null) {
            handler.characters(chars, i, i1);
        }
    }

}
