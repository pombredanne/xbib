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
package org.xbib.elasticsearch.mock;

import java.net.URI;
import org.testng.annotations.Test;
import org.xbib.elasticsearch.ElasticsearchIndexerMockDAO;
import org.xbib.io.Mode;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

/**
 * Elasticsearch Index mock test
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class MockIndexWriteTest<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>>  {

    @Test
    public void testWrite() throws Exception {
        
        final ElasticsearchIndexerMockDAO es = new ElasticsearchIndexerMockDAO()
                .setIndex("test")
                .setType("test");
        try {
            es.write(createResource());
        } finally {
            es.shutdown();
        }
    }

    private Resource<S,P,O> createResource() {
        Resource<S,P,O> resource = new SimpleResource();
        resource.id(URI.create("urn:document"));
        resource.property("dc:title", "Hello");
        resource.property("dc:title", "World");
        resource.property("xbib:person", "Jörg Prante");
        resource.property("dc:subject", "An");
        resource.property("dc:subject", "example");
        resource.property("dc:subject", "for");
        resource.property("dc:subject", "subject");
        resource.property("dc:subject", "sequence");
        resource.property("http://purl.org/dc/terms/place", "Köln");
        // sequence optimized for turtle output
        Resource<S,P,O> r1 = resource.newResource("urn:res1");
        r1.property("property1", "value1");
        r1.property("property2", "value2");
        Resource<S,P,O> r2 = resource.newResource("urn:res1");
        r2.property("property3", "value3");
        r2.property("property4", "value4");
        Resource<S,P,O> r3 = resource.newResource("urn:res1");
        r3.property("property5", "value5");
        r3.property("property6", "value6");
        return resource;
    }
}
