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
package org.xbib.elements.bibliographic;

import org.xbib.rdf.Property;

/**
 * B bibliographic elements as RDF properties
 */
public interface BibliographicProperties extends BIB {

    Property BIB_TITLE_ABBREVIATED = Property.create(NS_URI + "/titleAbbreviated");
    Property BIB_TITLE_UNIFORM = Property.create(NS_URI + "/titleUniform");
    Property BIB_TITLE_TRANSLATED = Property.create(NS_URI + "/titleTranslated");
    Property BIB_TITLE_ALTERNATIVE = Property.create(NS_URI + "/titleAlternative");
    Property BIB_TITLE_SERIES = Property.create(NS_URI + "/titleSeries");

    Property BIB_NAME = Property.create(NS_URI + "/name");
    Property BIB_NAME_PERSONAL = Property.create(NS_URI + "/namePersonal");
    Property BIB_NAME_PERSONAL_FAMILY = Property.create(NS_URI +"/namePersonalFamily");
    Property BIB_NAME_PERSONAL_GIVEN = Property.create(NS_URI + "/namePersonalGiven");
    Property BIB_NAME_CORPORATE = Property.create(NS_URI + "/nameCorporate");
    Property BIB_NAME_CONFERENCE = Property.create(NS_URI + "/nameConference");
    
    Property BIB_SUBJECT = Property.create(NS_URI + "/subject");
    Property BIB_SUBJECT_PLACE = Property.create(NS_URI + "/subjectPlace");
    Property BIB_SUBJECT_TITLE = Property.create(NS_URI + "/subjectTitle");
    Property BIB_SUBJECT_NAME = Property.create(NS_URI + "/subjectName");
    Property BIB_SUBJECT_OCCUPATION = Property.create(NS_URI +"/subjectOccupation");
    
    Property BIB_DATE = Property.create(NS_URI + "/date");
    Property BIB_DATE_ISSUED = Property.create(NS_URI + "/dateIssued");
    Property BIB_DATE_CREATED = Property.create(NS_URI + "/dateCreated");
    Property BIB_DATE_VALID = Property.create(NS_URI + "/dateValid");
    Property BIB_DATE_MODIFIED = Property.create(NS_URI + "/dateModified");
    Property BIB_DATE_COPYRIGHT = Property.create(NS_URI + "/dateCopyright");

    Property BIB_VOLUME = Property.create(NS_URI + "/volume");
    Property BIB_ISSUE = Property.create(NS_URI + "/issue");
    Property BIB_STARTPAGE = Property.create(NS_URI + "/startPage");
    Property BIB_ENDPAGE = Property.create(NS_URI + "/endPage");
    Property BIB_GENRE = Property.create(NS_URI + "/genre");
    Property BIB_AUDIENCE = Property.create(NS_URI + "/audience");
    Property BIB_CLASSIFICATION = Property.create(NS_URI +"/classification");
    Property BIB_ORIGINPLACE = Property.create(NS_URI + "/originPlace");
    Property BIB_EDITION = Property.create(NS_URI + "/edition");
    Property BIB_ISSUANCE = Property.create(NS_URI + "/issuance");
    
   
}
