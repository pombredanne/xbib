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
package org.xbib.rdf;

import java.io.StringWriter;
import java.net.URI;
import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.rdf.io.XMLResourceWriter;
import org.xbib.rdf.simple.SimpleBlankNode;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.rdf.simple.SimpleStatement;

/**
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SimpleResourceTest<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>> 
   extends Assert {

    @Test
    public void testStatementEquaivalence() {
        SimpleStatement a = SimpleStatement.createStatement(URI.create("urn:1"), URI.create("urn:2"), new SimpleLiteral("Hello World"));
        SimpleStatement b = SimpleStatement.createStatement(URI.create("urn:1"), URI.create("urn:2"), new SimpleLiteral("Hello World"));
        assertEquals(a.toString(), b.toString());
    }

    @Test
    public void testEmptyObject() throws Exception {
        SimpleResource<S, P, O> root = new SimpleResource(URI.create("urn:root"));
        root.addProperty("urn:property", null);
    }

    @Test
    public void testProperties() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource<S, P, O>(URI.create("urn:doc1"));
        String id = "urn:doc1";
        r.setIdentifier(URI.create(id));
        r.addProperty("urn:valueURI", "Hello World");
        r.addProperty("urn:creator", "Smith");
        r.addProperty("urn:creator", "Jones");
        assertEquals("urn:doc1 urn:valueURI Hello World\nurn:doc1 urn:creator Smith\nurn:doc1 urn:creator Jones\n", r.toString());
    }

    @Test
    public void testPropertyIterator() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource<S, P, O>(URI.create("urn:doc2"));
        String id = "urn:doc2";
        r.setIdentifier(URI.create(id));
        r.addProperty("urn:valueURI", "Hello World");
        r.addProperty("urn:name", "Smith");
        r.addProperty("urn:name", "Jones");
        Iterator<Statement<S, P, O>> it = r.iterator();
        assertEquals("urn:doc2 urn:valueURI Hello World", it.next().toString());
        assertEquals("urn:doc2 urn:name Smith", it.next().toString());
        assertEquals("urn:doc2 urn:name Jones", it.next().toString());
    }

    @Test
    public void testResourceIterator() throws Exception {
        SimpleBlankNode.reset();
        SimpleResource<S, P, O> parent = new SimpleResource<>();
        parent.setIdentifier(URI.create("urn:doc3"));
        Resource<S, P, O> child = parent.createResource(parent.createPredicate("urn:resource"));
        child.addProperty("urn:property", "value");
        XMLResourceWriter<S, P, O> xmlrw = new XMLResourceWriter<>();
        // parent parent
        StringWriter sw = new StringWriter();
        xmlrw.toXML(parent, sw);
        assertEquals(sw.toString(), "<urn:doc3><urn:resource><urn:property>value</urn:property></urn:resource></urn:doc3>");
        // child parent
        sw = new StringWriter();
        xmlrw.toXML(child, sw);
        assertEquals(sw.toString(), "<genid:a1><urn:property>value</urn:property></genid:a1>");
    }

    @Test
    public void testBlankNodeRenumbering() throws Exception {
        SimpleBlankNode.reset();
        SimpleResource<S, P, O> m1 = new SimpleResource<>(URI.create("urn:meta1"));
        m1.setIdentifier(URI.create("urn:meta1"));
        Resource<S, P, O> r1 = m1.createResource(m1.createPredicate("urn:res1"));
        r1.addProperty("urn:has", "a first resource value");
        m1.addProperty("urn:has", "a first metadata value");

        SimpleResource<S, P, O> m2 = new SimpleResource<>(URI.create("urn:meta2"));
        m2.setIdentifier(URI.create("urn:meta2"));
        Resource<S, P, O> r2 = m2.createResource(m2.createPredicate("urn:res2"));
        r2.addProperty("urn:has", "a second resource value");
        m2.addProperty("urn:has", "a second metadata value");

        m1.addResource(m1.createPredicate("a:res"), m2);

        Iterator<Statement<S, P, O>> it = m1.iterator(true);
        assertEquals("urn:meta1 urn:res1 _:a1", it.next().toString());
        assertEquals("_:a1 urn:has a first resource value", it.next().toString());
        assertEquals("urn:meta1 urn:has a first metadata value", it.next().toString());
        assertEquals("urn:meta1 urn:has a second metadata value", it.next().toString());
    }

    @Test
    public void testObjectSet() throws Exception {
        SimpleResource<S, P, O> resource = new SimpleResource<>(URI.create("urn:doc4"));
        resource.addProperty("urn:hasAttribute", "a");
        resource.addProperty("urn:hasAttribute", "b");
        resource.addProperty("urn:hasAttribute", "a");
        resource.addProperty("urn:hasAttribute", "c");
        assertEquals("[a, b, c]", resource.objectSet(resource.createPredicate("urn:hasAttribute")).toString());
    }
    
    @Test
    public void testPredicateIterator() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource<>();
        r.setIdentifier(URI.create("urn:doc11"));
        r.addProperty("urn:valueURI", "Hello World");
        r.addProperty("urn:name", "Smith");
        r.addProperty("urn:name", "Jones");
        URI resURI = URI.create("urn:res1");
        Resource<S, P, O> r1 = r.createResource(r.createPredicate(resURI));
        r1.addProperty("urn:has", "a first resource value");
        Resource<S, P, O> r2 = r.createResource(r.createPredicate(resURI));
        r2.addProperty("urn:has", "a second resource value");
        Iterator<P> it = r.predicateSet(r.getSubject()).iterator();
        int propCounter = 0;
        int valueCounter = 0;
        while (it.hasNext()) {
            P pred = it.next();
            propCounter++;
            Iterator<O> vit = r.objectSet(pred).iterator();
            while (vit.hasNext()) {
                vit.next();
                valueCounter++;
            }
        }
        assertEquals(valueCounter, 5);
        assertEquals(propCounter, 3);
    }

}
