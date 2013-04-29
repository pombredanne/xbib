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
import java.io.StringWriter;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import javax.xml.namespace.QName;

public class JsonReaderTest extends Assert {

    private static final Logger logger = LoggerFactory.getLogger(JsonReaderTest.class.getName());

    final SimpleResourceContext resourceContext = new SimpleResourceContext();

    @Test
    public void testGenericJsonReader() throws Exception {

        String filename = "dc.json";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("dcterms", "http://purl.org/dc/terms/");
        context.addNamespace("bib", "info:srw/cql-context-set/1/bib-v1/");
        context.addNamespace("xbib", "http://xbib.org/");
        context.addNamespace("lia", "http://xbib.org/lia/");

        resourceContext.newNamespaceContext(context);

        JsonResourceHandler jsonHandler = new JsonResourceHandler(resourceContext) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                //return "oai_dc".equals(name.getLocalPart());
                return false;
            }

            @Override
            public boolean skip(QName name) {
                // skip dc:dc element
                //return "dc".equals(name.getLocalPart());
                return false;
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if (identifier == null) {
                    // make sure we can build an opaque IRI, whatever is out there
                   // String s = UrlEncoding.encode(value, CharUtils.Profile.SCHEMESPECIFICPART.filter());
                    resourceContext.resource().id(IRI.create("id:doc1"));
                }
            }

        };
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter().output(sw);
        jsonHandler.setListener(t);
        new JsonReader()
                .setHandler(jsonHandler)
                .root(new QName("http://purl.org/dc/elements/1.1/", "root", "dc"))
                .parse(in);
        logger.info("resource={}", sw.toString());
    }

}
