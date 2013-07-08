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
package org.xbib.rdf.io.turtle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.simple.SimpleResource;

public class TurtleTest<S extends Identifier, P extends Property, O extends Node>
        extends Assert {

    private final Logger logger = LoggerFactory.getLogger(TurtleTest.class.getName());

    public void testTurtleGND() throws Exception {
        InputStream in = getClass().getResourceAsStream("GND.ttl");
        TurtleReader reader = new TurtleReader(IRI.create("http://d-nb.info/gnd/"));
        reader.parse(in);
    }

    public void testTurtleReader() throws Exception {
        StringBuilder sb = new StringBuilder();
        String filename = "turtle-demo.ttl";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        String s1 = sb.toString().trim();
        Resource<S, P, O> resource = createResource();
        StringWriter sw = new StringWriter();

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("dcterms", "http://purl.org/dc/terms/");

        new TurtleWriter()
            .output(sw)
            .setContext(context)
            .writeNamespaces()
            .write(resource);
        String s2 = sw.toString().trim();
        assertEquals(s2, s1);
    }

    private Resource createResource() {
        Resource<S, P, O> resource = new SimpleResource()
                .id(IRI.create("urn:doc1"));
        resource.add("dc:creator", "Smith");
        resource.add("dc:creator", "Jones");
        Resource r = resource.newResource("dcterms:hasPart")
                .add("dc:title", "This is a part")
                .add("dc:title", "of the sample title")
                .add("dc:creator", "Jörg Prante")
                .add("dc:date", "2009");
        resource.add("dc:title", "A sample title");
        r = resource.newResource("dcterms:isPartOf")
                .add("dc:title", "another")
                .add("dc:title", "title");
        return resource;
    }

    public void testTurtleWrite() throws Exception {
        Resource resource = createResource2();
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter()
                .output(sw)
                .write(resource);
        sw.toString().trim();
    }

    private Resource<S, P, O> createResource2() {
        Resource<S, P, O> r = new SimpleResource()
                .id(IRI.create("urn:resource"))
                .add("dc:title", "Hello")
                .add("dc:title", "World")
                .add("xbib:person", "Jörg Prante")
                .add("dc:subject", "An")
                .add("dc:subject", "example")
                .add("dc:subject", "for")
                .add("dc:subject", "a")
                .add("dc:subject", "sequence")
                .add("http://purl.org/dc/terms/place", "Köln");
        // sequence optimized for turtle output
        Resource<S, P, O> r1 = r.newResource("urn:res1")
                .add("property1", "value1")
                .add("property2", "value2");
        Resource<S, P, O> r2 = r.newResource("urn:res2")
                .add("property3", "value3")
                .add("property4", "value4");
        Resource<S, P, O> r3 = r.newResource("urn:res3")
                .add("property5", "value5")
                .add("property6", "value6");
        return r;
    }


    @Test
    public void testTurtleResourceIndent() throws Exception {
        Resource resource = createNestedResources();
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter()
                .output(sw)
                .write(resource);
        logger.info(sw.toString().trim());
    }

    private Resource<S, P, O> createNestedResources() {
        Resource<S, P, O> r = new SimpleResource()
                .id(IRI.create("urn:resource"))
                .add("dc:title", "Hello")
                .add("dc:title", "World")
                .add("xbib:person", "Jörg Prante")
                .add("dc:subject", "An")
                .add("dc:subject", "example")
                .add("dc:subject", "for")
                .add("dc:subject", "a")
                .add("dc:subject", "sequence")
                .add("http://purl.org/dc/terms/place", "Köln");
        // sequence optimized for turtle output
        Resource<S, P, O> r1 = r.newResource("urn:res1")
                .add("property1", "value1")
                .add("property2", "value2");
        Resource<S, P, O> r2 = r1.newResource("urn:res2")
                .add("property3", "value3")
                .add("property4", "value4");
        Resource<S, P, O> r3 = r.newResource("urn:res3")
                .add("property5", "value5")
                .add("property6", "value6");
        return r;
    }


}
