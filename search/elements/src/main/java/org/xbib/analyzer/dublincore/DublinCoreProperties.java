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
package org.xbib.analyzer.dublincore;

import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.simple.SimpleFactory;

/**
 *  Dublin Core Elements as RDF Properties
 */
public interface DublinCoreProperties extends DC {

    final SimpleFactory<Identifier,Property,Node> SIMPLE_FACTORY = SimpleFactory.getInstance();
    
    Property DC_CREATOR = SIMPLE_FACTORY.asPredicate(NS_URI + "creator");
    Property DC_CONTRIBUTOR = SIMPLE_FACTORY.asPredicate(NS_URI + "contributor");
    Property DC_COVERAGE = SIMPLE_FACTORY.asPredicate(NS_URI + "coverage");
    Property DC_DATE = SIMPLE_FACTORY.asPredicate(NS_URI  + "date");
    Property DC_DESCRIPTION = SIMPLE_FACTORY.asPredicate(NS_URI + "description");
    Property DC_FORMAT = SIMPLE_FACTORY.asPredicate( NS_URI + "format");
    Property DC_IDENTIFIER = SIMPLE_FACTORY.asPredicate(NS_URI + "identifier");
    Property DC_LANGUAGE = SIMPLE_FACTORY.asPredicate(NS_URI + "language");
    Property DC_PUBLISHER = SIMPLE_FACTORY.asPredicate(NS_URI + "publisher");
    Property DC_SOURCE = SIMPLE_FACTORY.asPredicate(NS_URI + "source");
    Property DC_SUBJECT = SIMPLE_FACTORY.asPredicate(NS_URI + "subject");
    Property DC_RELATION = SIMPLE_FACTORY.asPredicate(NS_URI + "relation");
    Property DC_RIGHTS = SIMPLE_FACTORY.asPredicate(NS_URI + "rights");
    Property DC_TITLE = SIMPLE_FACTORY.asPredicate(NS_URI + "title");
    Property DC_TYPE = SIMPLE_FACTORY.asPredicate(NS_URI + "type");
}
