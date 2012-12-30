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

import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.simple.Factory;

/**
 * The Dublin Core Terms as RDF properties 
 * 
 * @author joerg
 */
public interface DublinCoreTermsProperties extends DCTERMS {

    final Factory<Identifier,Property,Node> factory = Factory.getInstance();
    
    Property DCTERMS_ABSTRACT = factory.asPredicate(NS_URI + "abstract");
    Property DCTERMS_ACCESSRIGHTS = factory.asPredicate(NS_URI + "accessRights");
    Property DCTERMS_ACCRUALMETHOD = factory.asPredicate(NS_URI + "accrualMethod");
    Property DCTERMS_ACCRUALPERIODICITY = factory.asPredicate(NS_URI + "accrualPeriodicity");
    Property DCTERMS_ACCRUALPOLICY = factory.asPredicate(NS_URI + "accrualPolicy");
    Property DCTERMS_ALTERNATIVE = factory.asPredicate(NS_URI + "alternative");
    Property DCTERMS_AUDIENCE = factory.asPredicate(NS_URI + "audience");
    Property DCTERMS_AVAILABLE = factory.asPredicate(NS_URI + "available");
    Property DCTERMS_BIBLIOGRAPHICCITATION = factory.asPredicate(NS_URI + "bibliographicCitation");
    Property DCTERMS_CONFORMSTO = factory.asPredicate(NS_URI + "conformsTo");
    Property DCTERMS_CONTRIBUTOR = factory.asPredicate(NS_URI + "contributor");
    Property DCTERMS_COVERAGE = factory.asPredicate(NS_URI + "coverage");
    Property DCTERMS_CREATED = factory.asPredicate(NS_URI + "created");
    Property DCTERMS_CREATOR = factory.asPredicate(NS_URI + "creator");
    Property DCTERMS_DATE = factory.asPredicate(NS_URI + "date");
    Property DCTERMS_DATEACCEPTED = factory.asPredicate(NS_URI + "dateAccepted");
    Property DCTERMS_DATECOPYRIGHTED = factory.asPredicate(NS_URI + "dateCopyrighted");
    Property DCTERMS_DATESUBMITTED = factory.asPredicate(NS_URI + "dateSubmitted");
    Property DCTERMS_DESCRIPTION = factory.asPredicate(NS_URI + "description");
    Property DCTERMS_EDUCATIONLEVEL = factory.asPredicate(NS_URI + "educationLevel");
    Property DCTERMS_EXTENT = factory.asPredicate(NS_URI + "extent");
    Property DCTERMS_FORMAT = factory.asPredicate(NS_URI + "format");
    Property DCTERMS_HASFORMAT = factory.asPredicate(NS_URI + "hasFormat");
    Property DCTERMS_HASPART = factory.asPredicate(NS_URI + "hasPart");
    Property DCTERMS_HASVERSION = factory.asPredicate(NS_URI + "hasVersion");
    Property DCTERMS_IDENTIFIER = factory.asPredicate(NS_URI + "identifier");
    Property DCTERMS_INSTRUCTIONALMETHOD = factory.asPredicate(NS_URI + "instructionalMethod");
    Property DCTERMS_ISFORMATOF = factory.asPredicate(NS_URI + "isFormatOf");
    Property DCTERMS_ISPARTOF = factory.asPredicate(NS_URI + "isPartOf");
    Property DCTERMS_ISREFERENCEDBY = factory.asPredicate(NS_URI + "isReferencedBy");
    Property DCTERMS_ISREQUIREDBY = factory.asPredicate(NS_URI + "isRequiredBy");
    Property DCTERMS_ISSUED = factory.asPredicate(NS_URI + "issued");
    Property DCTERMS_ISVERSIONOF = factory.asPredicate(NS_URI + "isVersionOf");
    Property DCTERMS_LANGUAGE = factory.asPredicate(NS_URI + "language");
    Property DCTERMS_LICENSE = factory.asPredicate(NS_URI + "license");
    Property DCTERMS_MEDIATOR = factory.asPredicate(NS_URI + "mediator");
    Property DCTERMS_MEDIUM = factory.asPredicate(NS_URI + "medium");
    Property DCTERMS_MODIFIED = factory.asPredicate(NS_URI + "modified");
    Property DCTERMS_PROVENANCE = factory.asPredicate(NS_URI + "provenance");
    Property DCTERMS_PUBLISHER = factory.asPredicate(NS_URI + "publisher");
    Property DCTERMS_REFERENCES= factory.asPredicate(NS_URI + "references");
    Property DCTERMS_RELATION = factory.asPredicate(NS_URI + "relation");
    Property DCTERMS_REPLACES = factory.asPredicate(NS_URI + "replaces");
    Property DCTERMS_REQUIRES = factory.asPredicate(NS_URI + "requires");
    Property DCTERMS_RIGHTS = factory.asPredicate(NS_URI + "rights");
    Property DCTERMS_RIGHTSHOLDER = factory.asPredicate(NS_URI + "rightsHolder");
    Property DCTERMS_SOURCE = factory.asPredicate(NS_URI + "source");
    Property DCTERMS_SPATIAL = factory.asPredicate(NS_URI + "spatial");
    Property DCTERMS_SUBJECT = factory.asPredicate(NS_URI + "subject");
    Property DCTERMS_TABLEOFCONTENTS = factory.asPredicate(NS_URI + "tableOfContents");
    Property DCTERMS_TEMPORAL = factory.asPredicate(NS_URI + "temporal");
    Property DCTERMS_TITLE = factory.asPredicate(NS_URI + "title");
    Property DCTERMS_TYPE = factory.asPredicate(NS_URI + "type");
    Property DCTERMS_VALID = factory.asPredicate(NS_URI + "valid");
}
