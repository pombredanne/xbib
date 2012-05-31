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

import java.io.StringWriter;
import java.net.URI;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResource;

public class NTripleTest<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>> {

    @Test
    public void testNTripleWrite() throws Exception {
        SimpleResource<S, P, O> m = createResource();
        StringWriter w = new StringWriter();
        NTripleWriter t = new NTripleWriter();
        t.write(m, w);
    }

    @Test
    public void testNTripleWriteInt() throws Exception {
        SimpleResource<S, P, O> resource = new SimpleResource();
        resource.setIdentifier(URI.create("urn:doc1"));
        resource.addProperty(
                resource.createPredicate("http://purl.org/dc/elements/1.1/date"),
                (O)new SimpleLiteral("2010", URI.create("http://www.w3.org/2001/XMLSchema#integer")));
        StringWriter w = new StringWriter();
        NTripleWriter t = new NTripleWriter();
        t.write(resource, w);
        Assert.assertEquals( w.toString(),
                "<urn:doc1> <http://purl.org/dc/elements/1.1/date> \"2010\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n"
        );
    }

    private SimpleResource<S, P, O> createResource() {
        SimpleResource<S, P, O> m = new SimpleResource();
        String id = "urn:doc1";
        m.setIdentifier(URI.create(id));
        //m.setPrimaryKey(URIKey.create(id));
        m.addProperty("http://purl.org/dc/elements/1.1/creator", "Smith");
        m.addProperty("http://purl.org/dc/elements/1.1/creator", "Jones");
        Resource r = m.createResource(m.createPredicate("dcterms:hasPart"));
        r.addProperty("http://purl.org/dc/elements/1.1/title", "This is a part");
        r.addProperty("http://purl.org/dc/elements/1.1/title", "of a title");
        r.addProperty("http://purl.org/dc/elements/1.1/creator", "Jörg Prante");
        r.addProperty("http://purl.org/dc/elements/1.1/date", "2009");
        m.addProperty("http://purl.org/dc/elements/1.1/title", "A sample title");
        r = m.createResource(m.createPredicate("http://purl.org/dc/terms/isPartOf"));
        r.addProperty("http://purl.org/dc/elements/1.1/title", "another");
        r.addProperty("http://purl.org/dc/elements/1.1/title", "title");
        return m;
    }
}
