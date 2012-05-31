/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.rdf.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

public class TurtleTest<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>> extends Assert {

    private static final Logger logger = Logger.getLogger(TurtleTest.class.getName());

    @Test
    public void testTurtleDemoReader() throws Exception {
        StringBuilder sb = new StringBuilder();
        String filename = "/org/xbib/rdf/io/turtle-demo.ttl";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        String s1 = sb.toString().trim();
        //logger.log(Level.INFO, "turtle content from file = {0}", s1.length());
        SimpleResource<S, P, O> resource = createResource();
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter();
        t.write(resource, true, sw);
        String s2 = sw.toString().trim();
        logger.log(Level.INFO, "turtle content = {0}", s2);
        //assertEquals(s2, s1);
    }

    private SimpleResource createResource() {
        SimpleResource<S, P, O> resource = new SimpleResource();
        String id = "urn:doc1";
        resource.setIdentifier(URI.create(id));
        resource.addProperty("dc:creator", "Smith");
        resource.addProperty("dc:creator", "Jones");
        Resource r = resource.createResource(resource.createPredicate("dcterms:hasPart"));
        r.addProperty("dc:title", "This is a part");
        r.addProperty("dc:title", "of the sample title");
        r.addProperty("dc:creator", "Jörg Prante");
        r.addProperty("dc:date", "2009");
        resource.addProperty("dc:title", "A sample title");
        r = resource.createResource(resource.createPredicate("dcterms:isPartOf"));
        r.addProperty("dc:title", "another");
        r.addProperty("dc:title", "title");
        return resource;
    }
    
    
    @Test
    public void testTurtleReadWrite() throws Exception {
        Resource resource = createResource2();
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter();
        t.write(resource, true, sw);
        String s2 = sw.toString().trim();
        logger.log(Level.INFO, "turtle content = {0}", s2);
    }
    
    
    private Resource<S,P,O> createResource2() {
        Resource<S,P,O> resource = new SimpleResource();
        resource.setIdentifier(URI.create("urn:resource"));
        resource.addProperty("dc:title", "Hello");
        resource.addProperty("dc:title", "World");
        resource.addProperty("xbib:person", "Jörg Prante");
        resource.addProperty("dc:subject", "An");
        resource.addProperty("dc:subject", "example");
        resource.addProperty("dc:subject", "for");
        resource.addProperty("dc:subject", "a");
        resource.addProperty("dc:subject", "sequence");
        resource.addProperty("http://purl.org/dc/terms/place", "Köln");
        // sequence optimized for turtle output
        Resource<S,P,O> r1 = resource.createResource(resource.createPredicate("urn:res1"));
        r1.addProperty("property1", "value1");
        r1.addProperty("property2", "value2");
        Resource<S,P,O> r2 = resource.createResource(resource.createPredicate("urn:res2"));
        r2.addProperty("property3", "value3");
        r2.addProperty("property4", "value4");
        Resource<S,P,O> r3 = resource.createResource(resource.createPredicate("urn:res3"));
        r3.addProperty("property5", "value5");
        r3.addProperty("property6", "value6");
        return resource;
    }
}
