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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.namespace.QName;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.text.CharUtils.Profile;
import org.xbib.text.UrlEncoding;
import org.xml.sax.InputSource;

public class XmlReaderTest extends Assert {

    private final Logger logger = Loggers.getLogger(XmlReaderTest.class);

    @Test
    public void testOAIDC() throws Exception {
        String filename = "/org/xbib/rdf/io/xml/oro-eprint-25656.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("oaidc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");

        final SimpleResourceContext resourceContext = new SimpleResourceContext();
        resourceContext.newNamespaceContext(context);

        XmlHandler xmlHandler = new AbstractXmlResourceHandler(resourceContext) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                return "oai_dc".equals(name.getLocalPart());
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if ("identifier".equals(name.getLocalPart()) && identifier == null) {
                    // make sure we can build an opaque IRI, whatever is out there
                    String s = UrlEncoding.encode(value, Profile.SCHEMESPECIFICPART.filter());
                    resourceContext.resource().id(IRI.create("id:" + s));
                }
            }
            
            @Override
            public boolean skip(QName name) {
                // skip dc:dc element
                return "dc".equals(name.getLocalPart());
            }

        };
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter()
                .output(sw)
                .setContext(context)
                .writeNamespaces();
        xmlHandler.setListener(t);
        new XmlReader()
                .setHandler(xmlHandler)
                .parse(new InputSource(in));
        t.close();
        String s = sw.toString().trim();
        logger.info(s);
        assertEquals(s.length(), 2115);
    }

    @Test
    public void testXmlArray() throws Exception {
        String filename = "/org/xbib/rdf/io/xml/array.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        final SimpleResourceContext resourceContext = new SimpleResourceContext();
        resourceContext.newNamespaceContext(context);
        AbstractXmlHandler xmlHandler = new AbstractXmlResourceHandler(resourceContext) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                return false; // only one resource
                //return "oai_dc".equals(name.getLocalPart());
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if (identifier == null) {
                    resourceContext.resource().id("id:1");
                }
            }

            @Override
            public boolean skip(QName name) {
                return false;
                // skip dc:dc element
                //return "dc".equals(name.getLocalPart());
            }

        };
        xmlHandler.setDefaultNamespace("xml", "http://xmltest")
                .setListener(new TripleListener() {
                    @Override
                    public TripleListener startPrefixMapping(String prefix, String uri) {
                        return this;
                    }

                    @Override
                    public TripleListener endPrefixMapping(String prefix) {
                        return this;
                    }

                    @Override
                    public TripleListener newIdentifier(IRI identifier) {
                        return this;
                    }

                    @Override
                    public TripleListener triple(Triple triple) {
                        logger.info("triple = {}", triple);
                        return this;
                    }
                });
        new XmlReader()
                .setHandler(xmlHandler)
                .parse(new InputSource(in));
    }

    @Test
    public void testXmlAttribute() throws Exception {
        String filename = "/org/xbib/rdf/io/xml/attribute.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        IRINamespaceContext context = IRINamespaceContext.newInstance();
        final SimpleResourceContext resourceContext = new SimpleResourceContext();
        resourceContext.newNamespaceContext(context);
        AbstractXmlResourceHandler xmlHandler = new AbstractXmlResourceHandler(resourceContext) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                return false;
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if (identifier ==null) {
                    resourceContext.resource().id("id:1");
                }
            }

            @Override
            public boolean skip(QName name) {
                return false;
            }

        };
        xmlHandler.setDefaultNamespace("xml", "http://localhost")
                .setListener(new TripleListener() {
            @Override
            public TripleListener startPrefixMapping(String prefix, String uri) {
                return this;
            }

            @Override
            public TripleListener endPrefixMapping(String prefix) {
                return this;
            }

            @Override
            public TripleListener newIdentifier(IRI identifier) {
                return this;
            }

            @Override
            public TripleListener triple(Triple triple) {
                logger.info("triple = {}", triple);
                return this;
            }
        });
        new XmlReader()
                .setHandler(xmlHandler)
                .parse(new InputSource(in));
    }

}
