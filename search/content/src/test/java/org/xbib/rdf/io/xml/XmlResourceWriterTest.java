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
package org.xbib.rdf.io.xml;

import java.io.StringWriter;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

public class XmlResourceWriterTest<S extends Identifier, P extends Property, O extends Node> extends Assert {
    
    @Test
    public void testXMLResourceWriter() throws Exception {
        IdentifiableNode.reset();
        Resource<S, P, O> root = new SimpleResource().id(IRI.create("urn:root"));
        Resource resource = root.newResource("urn:resource");
        resource.add("urn:property", "value");
        Resource nestedResource = resource.newResource("urn:nestedresource");
        nestedResource.add("urn:nestedproperty", "nestedvalue");
        XmlResourceWriter w = new XmlResourceWriter();
        StringWriter sw = new StringWriter();
        w.toXML(root, sw);
        assertEquals("<urn:root><urn:resource><urn:property>value</urn:property><urn:nestedresource><urn:nestedproperty>nestedvalue</urn:nestedproperty></urn:nestedresource></urn:resource></urn:root>", sw.toString());
    }    
    
    @Test
    public void testResourceXml() throws Exception {
        IdentifiableNode.reset();
        SimpleResource<S, P, O> parent = new SimpleResource();
        parent.id(IRI.create("urn:doc3"));
        Resource<S, P, O> child = parent.newResource("urn:resource");
        child.add("urn:property", "value");
        XmlResourceWriter xmlrw = new XmlResourceWriter();
        // parent parent
        StringWriter sw = new StringWriter();
        xmlrw.toXML(parent, sw);
        assertEquals("<urn:doc3><urn:resource><urn:property>value</urn:property></urn:resource></urn:doc3>", sw.toString());
        // child parent
        sw = new StringWriter();
        xmlrw.toXML(child, sw);
        assertEquals("<genid:genid1><urn:property>value</urn:property></genid:genid1>", sw.toString());
    }
}
