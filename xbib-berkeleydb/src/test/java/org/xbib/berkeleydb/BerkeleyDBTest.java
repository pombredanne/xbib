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

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.annotations.Test;
import org.xbib.io.Mode;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

public class BerkeleyDBTest<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>> {

    private static final Logger logger = Logger.getLogger(BerkeleyDBTest.class.getName());
    
    @Test
    public void testBerkeleyDB() throws Exception {
        URI uri = URI.create("bdbresource:localhost/testdb");
        BerkeleyDBSession session = new BerkeleyDBSession(uri);
        session.open(Mode.WRITE);
        Write write = new Write();
        Resource resource = createResource();
        logger.log(Level.INFO, "resource before write = " + resource );
        // write a resource
        write.write(session, resource);
        write.execute(session);
        session.close();
        // and read it again
        session.open(Mode.READ);
        Read read = new Read();
        read.query(session, "urn:resource");
        logger.log(Level.INFO, "resource read = " + read.getResource() );
        session.close();
    }
    
    private Resource<S,P,O> createResource() {
        Resource<S,P,O> resource = new SimpleResource();
        resource.setIdentifier(URI.create("urn:resource"));
        resource.addProperty("dc:title", "Hello")
        .addProperty("dc:title", "World")
        .addProperty("xbib:person", "Jörg Prante")
        .addProperty("dc:subject", "An")
        .addProperty("dc:subject", "example")
        .addProperty("dc:subject", "for")
        .addProperty("dc:subject", "a")
        .addProperty("dc:subject", "sequence")
        .addProperty("http://purl.org/dc/terms/place", "Köln");
        // sequence optimized for turtle output
        Resource<S,P,O> r1 = resource.createResource(resource.createPredicate("urn:res#1"));
        r1.addProperty("property1", "value1");
        r1.addProperty("property2", "value2");
        Resource<S,P,O> r2 = resource.createResource(resource.createPredicate("urn:res#2"));
        r2.addProperty("property3", "value3");
        r2.addProperty("property4", "value4");
        Resource<S,P,O> r3 = resource.createResource(resource.createPredicate("urn:res#3"));
        r3.addProperty("property5", "value5");
        r3.addProperty("property6", "value6");
        return resource;
    }
}
