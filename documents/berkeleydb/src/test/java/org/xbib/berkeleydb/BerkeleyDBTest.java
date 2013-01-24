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
package org.xbib.berkeleydb;

import org.testng.annotations.Test;
import org.xbib.io.Session;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

public class BerkeleyDBTest<S extends Identifier, P extends Property, O extends Node> {

    private static final Logger logger = LoggerFactory.getLogger(BerkeleyDBTest.class.getName());
    
    @Test
    public void testBerkeleyDB() throws Exception {
        IRI uri = IRI.create("bdbresource:target/localhost/testdb");
        BerkeleyDBSession session = new BerkeleyDBSession(uri);
        session.open(Session.Mode.WRITE);
        Write write = new Write();
        Resource resource = createResource();
        logger.info("resource before write = " + resource );
        // write a resource
        write.write(session, resource);
        write.execute(session);
        session.close();
        // and read it again
        session.open(Session.Mode.READ);
        Read read = new Read();
        read.query(session, "urn:resource");
        logger.info("resource read = " + read.getResource() );
        session.close();
    }
    
    private Resource<S,P,O> createResource() {
        Resource<S,P,O> resource = new SimpleResource();
        resource.id(IRI.create("urn:resource"));
        resource.add("dc:title", "Hello")
        .add("dc:title", "World")
        .add("xbib:person", "Jörg Prante")
        .add("dc:subject", "An")
        .add("dc:subject", "example")
        .add("dc:subject", "for")
        .add("dc:subject", "a")
        .add("dc:subject", "sequence")
        .add("http://purl.org/dc/terms/place", "Köln");
        // sequence optimized for turtle output
        Resource<S,P,O> r1 = resource.newResource("urn:res#1");
        r1.add("property1", "value1");
        r1.add("property2", "value2");
        Resource<S,P,O> r2 = resource.newResource("urn:res#2");
        r2.add("property3", "value3");
        r2.add("property4", "value4");
        Resource<S,P,O> r3 = resource.newResource("urn:res#3");
        r3.add("property5", "value5");
        r3.add("property6", "value6");
        return resource;
    }
}
