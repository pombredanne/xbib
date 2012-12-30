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
package org.xbib.rdf;

import org.xbib.iri.IRI;

public interface RDF {

    String NS_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
    String NS_PREFIX = "rdf";
    
    IRI RDF = IRI.create(NS_URI);
    IRI RDF_RDF = IRI.create(NS_URI + "RDF");
    IRI RDF_DESCRIPTION = IRI.create(NS_URI + "Description");
    IRI RDF_ABOUT = IRI.create(NS_URI + "about");
    IRI RDF_RESOURCE = IRI.create(NS_URI + "resource");
    IRI RDF_NODE_ID = IRI.create(NS_URI + "nodeID");
    IRI RDF_ID = IRI.create(NS_URI + "ID");
    IRI RDF_LI = IRI.create(NS_URI + "li");
    IRI RDF_TYPE = IRI.create(NS_URI + "type");    
    IRI RDF_SUBJECT = IRI.create(NS_URI + "subject");    
    IRI RDF_PREDICATE = IRI.create(NS_URI + "predicate");    
    IRI RDF_OBJECT = IRI.create(NS_URI + "object");    
    IRI RDF_STATEMENT = IRI.create(NS_URI + "Statement"); 
    IRI RDF_XMLLITERAL = IRI.create(NS_URI + "XMLLiteral"); 
    IRI RDF_NIL = IRI.create(NS_URI + "nil"); 
    IRI RDF_FIRST = IRI.create(NS_URI + "first"); 
    IRI RDF_REST = IRI.create(NS_URI + "rest");
    // non-standard tag for language
    IRI RDF_LANGUAGE = IRI.create(NS_URI + "language");

}
