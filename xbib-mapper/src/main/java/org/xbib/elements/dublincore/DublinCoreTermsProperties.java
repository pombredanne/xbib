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
 * The Dublin Core Terms as RDF properties 
 * 
 * @author joerg
 */
public interface DublinCoreTermsProperties extends DCTERMS {

    Property DCTERMS_ABSTRACT = Property.create(NS_URI + "abstract");
    Property DCTERMS_ACCESSRIGHTS = Property.create(NS_URI + "accessRights");
    Property DCTERMS_ACCRUALMETHOD = Property.create(NS_URI + "accrualMethod");
    Property DCTERMS_ACCRUALPERIODICITY = Property.create(NS_URI + "accrualPeriodicity");
    Property DCTERMS_ACCRUALPOLICY = Property.create(NS_URI + "accrualPolicy");
    Property DCTERMS_ALTERNATIVE = Property.create(NS_URI + "alternative");
    Property DCTERMS_AUDIENCE = Property.create(NS_URI + "audience");
    Property DCTERMS_AVAILABLE = Property.create(NS_URI + "available");
    Property DCTERMS_BIBLIOGRAPHICCITATION = Property.create(NS_URI + "bibliographicCitation");
    Property DCTERMS_CONFORMSTO = Property.create(NS_URI + "conformsTo");
    Property DCTERMS_CONTRIBUTOR = Property.create(NS_URI + "contributor");
    Property DCTERMS_COVERAGE = Property.create(NS_URI + "coverage");
    Property DCTERMS_CREATED = Property.create(NS_URI + "created");
    Property DCTERMS_CREATOR = Property.create(NS_URI + "creator");
    Property DCTERMS_DATE = Property.create(NS_URI + "date");
    Property DCTERMS_DATEACCEPTED = Property.create(NS_URI + "dateAccepted");
    Property DCTERMS_DATECOPYRIGHTED = Property.create(NS_URI + "dateCopyrighted");
    Property DCTERMS_DATESUBMITTED = Property.create(NS_URI + "dateSubmitted");
    Property DCTERMS_DESCRIPTION = Property.create(NS_URI + "description");
    Property DCTERMS_EDUCATIONLEVEL = Property.create(NS_URI + "educationLevel");
    Property DCTERMS_EXTENT = Property.create(NS_URI + "extent");
    Property DCTERMS_FORMAT = Property.create(NS_URI + "format");
    Property DCTERMS_HASFORMAT = Property.create(NS_URI + "hasFormat");
    Property DCTERMS_HASPART = Property.create(NS_URI + "hasPart");
    Property DCTERMS_HASVERSION = Property.create(NS_URI + "hasVersion");
    Property DCTERMS_IDENTIFIER = Property.create(NS_URI + "identifier");
    Property DCTERMS_INSTRUCTIONALMETHOD = Property.create(NS_URI + "instructionalMethod");
    Property DCTERMS_ISFORMATOF = Property.create(NS_URI + "isFormatOf");
    Property DCTERMS_ISPARTOF = Property.create(NS_URI + "isPartOf");
    Property DCTERMS_ISREFERENCEDBY = Property.create(NS_URI + "isReferencedBy");
    Property DCTERMS_ISREQUIREDBY = Property.create(NS_URI + "isRequiredBy");
    Property DCTERMS_ISSUED = Property.create(NS_URI + "issued");
    Property DCTERMS_ISVERSIONOF = Property.create(NS_URI + "isVersionOf");
    Property DCTERMS_LANGUAGE = Property.create(NS_URI + "language");
    Property DCTERMS_LICENSE = Property.create(NS_URI + "license");
    Property DCTERMS_MEDIATOR = Property.create(NS_URI + "mediator");
    Property DCTERMS_MEDIUM = Property.create(NS_URI + "medium");
    Property DCTERMS_MODIFIED = Property.create(NS_URI + "modified");
    Property DCTERMS_PROVENANCE = Property.create(NS_URI + "provenance");
    Property DCTERMS_PUBLISHER = Property.create(NS_URI + "publisher");
    Property DCTERMS_REFERENCES= Property.create(NS_URI + "references");
    Property DCTERMS_RELATION = Property.create(NS_URI + "relation");
    Property DCTERMS_REPLACES = Property.create(NS_URI + "replaces");
    Property DCTERMS_REQUIRES = Property.create(NS_URI + "requires");
    Property DCTERMS_RIGHTS = Property.create(NS_URI + "rights");
    Property DCTERMS_RIGHTSHOLDER = Property.create(NS_URI + "rightsHolder");
    Property DCTERMS_SOURCE = Property.create(NS_URI + "source");
    Property DCTERMS_SPATIAL = Property.create(NS_URI + "spatial");
    Property DCTERMS_SUBJECT = Property.create(NS_URI + "subject");
    Property DCTERMS_TABLEOFCONTENTS = Property.create(NS_URI + "tableOfContents");
    Property DCTERMS_TEMPORAL = Property.create(NS_URI + "temporal");
    Property DCTERMS_TITLE = Property.create(NS_URI + "title");
    Property DCTERMS_TYPE = Property.create(NS_URI + "type");
    Property DCTERMS_VALID = Property.create(NS_URI + "valid");
}
