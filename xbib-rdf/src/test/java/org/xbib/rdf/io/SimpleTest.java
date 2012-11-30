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

import org.xbib.rdf.io.xml.XMLResourceWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.net.URI;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

public class SimpleTest <S extends Resource<S, P, O>, P extends Property, O extends Literal<O>> 
   extends Assert  {

    @Test
    public void testSerialization() throws Exception {
        SimpleResource<S, P, O> d1 = new SimpleResource<S, P, O>(URI.create("urn:doc1"));
        d1.id(URI.create("urn:doc1"));
        d1.property("urn:valueURI", "Hello World");
        Resource<S, P, O> resource = d1.newResource("urn:resource");
        resource.property(d1.toPredicate("urn:property"), "value");
        Resource<S, P, O> nestedResource = resource.newResource("urn:nestedresource");
        nestedResource.property("urn:nestedproperty", "nestedvalue");
        SimpleResource<S, P, O> d2;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(buffer);
        out.writeObject(d1);
        out.close();
        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        d2 = (SimpleResource<S, P, O>) in.readObject();
        in.close();
        assertEquals(d1, d2);
    }    
    
    @Test
    public void testXMLResourceWriter() throws Exception {
        SimpleResource<S, P, O> root = new SimpleResource<S, P, O>(URI.create("urn:root"));
        Resource resource = root.newResource("urn:resource");
        resource.property("urn:property", "value");
        Resource nestedResource = resource.newResource("urn:nestedresource");
        nestedResource.property("urn:nestedproperty", "nestedvalue");
        XMLResourceWriter w = new XMLResourceWriter();
        StringWriter sw = new StringWriter();
        w.toXML(root, sw);
        assertEquals("<urn:root><urn:resource><urn:property>value</urn:property><urn:nestedresource><urn:nestedproperty>nestedvalue</urn:nestedproperty></urn:nestedresource></urn:resource></urn:root>", sw.toString());
    }

}
