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

import java.net.URI;

public interface RDF {

    String NS_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
    String NS_PREFIX = "rdf";
    
    URI RDF = URI.create(NS_URI);
    URI RDF_RDF = URI.create(NS_URI + "RDF");
    URI RDF_DESCRIPTION = URI.create(NS_URI + "Description");
    URI RDF_ABOUT = URI.create(NS_URI + "about");
    URI RDF_RESOURCE = URI.create(NS_URI + "resource");
    URI RDF_NODE_ID = URI.create(NS_URI + "nodeID");
    URI RDF_ID = URI.create(NS_URI + "ID");
    URI RDF_LI = URI.create(NS_URI + "li");
    URI RDF_TYPE = URI.create(NS_URI + "type");    
    URI RDF_SUBJECT = URI.create(NS_URI + "subject");    
    URI RDF_PREDICATE = URI.create(NS_URI + "predicate");    
    URI RDF_OBJECT = URI.create(NS_URI + "object");    
    URI RDF_STATEMENT = URI.create(NS_URI + "Statement"); 
    URI RDF_XMLLITERAL = URI.create(NS_URI + "XMLLiteral"); 
    URI RDF_NIL = URI.create(NS_URI + "nil"); 
    URI RDF_FIRST = URI.create(NS_URI + "first"); 
    URI RDF_REST = URI.create(NS_URI + "rest"); 
    
    
}
