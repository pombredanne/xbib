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
import org.xbib.rdf.io.xml.XMLResourceWriter;
import org.xbib.rdf.simple.SimpleBlankNode;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.rdf.simple.SimpleStatement;

/**
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
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
        root.property("urn:property", (O)null);
        assertEquals(root.properties.size(), 0);
    }

    @Test
    public void testIntegerLiteral() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource(URI.create("urn:root"));
        P pred = r.toPredicate("urn:property");
        O literal = r.toObject(new SimpleLiteral(123).type(Literal.XSD_INT));
        r.property(pred, literal);
        assertEquals(r.properties.size(), 1);
    }    
    
    @Test
    public void testProperties() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource(URI.create("urn:doc1"));
        String id = "urn:doc1";
        r.id(URI.create(id));
        r.property("urn:valueURI", "Hello World");
        r.property("urn:creator", "Smith");
        r.property("urn:creator", "Jones");
        assertEquals("urn:doc1 urn:valueURI Hello World\nurn:doc1 urn:creator Smith\nurn:doc1 urn:creator Jones\n", r.toString());
    }

    @Test
    public void testPropertyIterator() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource<S, P, O>(URI.create("urn:doc2"));
        String id = "urn:doc2";
        r.id(URI.create(id));
        r.property("urn:valueURI", "Hello World");
        r.property("urn:name", "Smith");
        r.property("urn:name", "Jones");
        Iterator<Statement<S, P, O>> it = r.iterator();
        assertEquals("urn:doc2 urn:valueURI Hello World", it.next().toString());
        assertEquals("urn:doc2 urn:name Smith", it.next().toString());
        assertEquals("urn:doc2 urn:name Jones", it.next().toString());
    }

    @Test
    public void testResourceIterator() throws Exception {
        SimpleBlankNode.reset();
        SimpleResource<S, P, O> parent = new SimpleResource<>();
        parent.id(URI.create("urn:doc3"));
        Resource<S, P, O> child = parent.newResource("urn:resource");
        child.property("urn:property", "value");
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
        m1.id(URI.create("urn:meta1"));
        Resource<S, P, O> r1 = m1.newResource("urn:res1");
        r1.property("urn:has", "a first resource value");
        m1.property("urn:has", "a first metadata value");

        SimpleResource<S, P, O> m2 = new SimpleResource<>(URI.create("urn:meta2"));
        m2.id(URI.create("urn:meta2"));
        Resource<S, P, O> r2 = m2.newResource("urn:res2");
        r2.property("urn:has", "a second resource value");
        m2.property("urn:has", "a second metadata value");

        m1.add(m1.toPredicate("a:res"), m2);

        Iterator<Statement<S, P, O>> it = m1.iterator(true);
        assertEquals("urn:meta1 urn:res1 _:a1", it.next().toString());
        assertEquals("_:a1 urn:has a first resource value", it.next().toString());
        assertEquals("urn:meta1 urn:has a first metadata value", it.next().toString());
        assertEquals("urn:meta1 urn:has a second metadata value", it.next().toString());
    }

    @Test
    public void testObjectSet() throws Exception {
        SimpleResource<S, P, O> resource = new SimpleResource<>(URI.create("urn:doc4"));
        resource.property("urn:hasAttribute", "a");
        resource.property("urn:hasAttribute", "b");
        resource.property("urn:hasAttribute", "a");
        resource.property("urn:hasAttribute", "c");
        assertEquals("[a, b, c]", resource.objectSet(resource.toPredicate("urn:hasAttribute")).toString());
    }

    @Test
    public void testPredicateIterator() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource<>();
        r.id(URI.create("urn:doc11"));
        r.property("urn:valueURI", "Hello World");
        r.property("urn:name", "Smith");
        r.property("urn:name", "Jones");
        Resource<S, P, O> r1 = r.newResource("urn:res1");
        r1.property("urn:has", "a first resource value");
        Resource<S, P, O> r2 = r.newResource("urn:res1");
        r2.property("urn:has", "a second resource value");
        Iterator<P> it = r.predicateSet(r.subject()).iterator();
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

    @Test
    public void testOptimize() throws Exception {
        SimpleResource<S, P, O> r = new SimpleResource<>();
        r.id(URI.create("urn:doc12"));
        r.property("urn:value1", "Hello World");
        P predicate = r.toPredicate("urn:value2");
        Resource<S, P, O> r1 = r.newResource(predicate.toString());
        r1.property(predicate, "a value");
        r.compact(predicate);
        Iterator<Statement<S, P, O>> it = r.iterator();
        int cnt = 0;
        /*
         * urn:doc12 urn:value1 Hello World 
         * urn:doc12 urn:value2 a value
         */
        while (it.hasNext()) {
            Statement<S, P, O> stmt = it.next();
            cnt++;
        }
        assertEquals(cnt, 2);
    }
}
