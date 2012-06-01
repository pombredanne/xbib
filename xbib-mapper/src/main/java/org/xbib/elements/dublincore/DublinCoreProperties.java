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
package org.xbib.elements.dublincore;

import org.xbib.rdf.Property;

/**
 * THe Dublin Core Elements as RDF Properties
 */
public interface DublinCoreProperties extends DC {

    Property DC_CREATOR = Property.create(NS_URI + "creator");
    Property DC_CONTRIBUTOR = Property.create(NS_URI + "contributor");
    Property DC_COVERAGE = Property.create(NS_URI + "coverage");
    Property DC_DATE = Property.create(NS_URI  + "date");
    Property DC_DESCRIPTION = Property.create(NS_URI + "description");
    Property DC_FORMAT =Property.create( NS_URI + "format");
    Property DC_IDENTIFIER = Property.create(NS_URI + "identifier");
    Property DC_LANGUAGE = Property.create(NS_URI + "language");
    Property DC_PUBLISHER = Property.create(NS_URI + "publisher");
    Property DC_SOURCE = Property.create(NS_URI + "source");
    Property DC_SUBJECT = Property.create(NS_URI + "subject");
    Property DC_RELATION = Property.create(NS_URI + "relation");
    Property DC_RIGHTS = Property.create(NS_URI + "rights");
    Property DC_TITLE = Property.create(NS_URI + "title");
    Property DC_TYPE = Property.create(NS_URI + "type");
}
