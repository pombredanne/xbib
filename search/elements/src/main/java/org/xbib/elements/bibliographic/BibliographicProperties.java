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

import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.simple.SimpleFactory;

/**
 * B bibliographic elements as RDF properties
 */
public interface BibliographicProperties extends BIB {

    final SimpleFactory<Identifier,Property,Node> SIMPLE_FACTORY = SimpleFactory.getInstance();
    
    Property BIB_TITLE_ABBREVIATED = SIMPLE_FACTORY.asPredicate(NS_URI + "/titleAbbreviated");
    Property BIB_TITLE_UNIFORM = SIMPLE_FACTORY.asPredicate(NS_URI + "/titleUniform");
    Property BIB_TITLE_TRANSLATED = SIMPLE_FACTORY.asPredicate(NS_URI + "/titleTranslated");
    Property BIB_TITLE_ALTERNATIVE = SIMPLE_FACTORY.asPredicate(NS_URI + "/titleAlternative");
    Property BIB_TITLE_SERIES = SIMPLE_FACTORY.asPredicate(NS_URI + "/titleSeries");

    Property BIB_NAME = SIMPLE_FACTORY.asPredicate(NS_URI + "/name");
    Property BIB_NAME_PERSONAL = SIMPLE_FACTORY.asPredicate(NS_URI + "/namePersonal");
    Property BIB_NAME_PERSONAL_FAMILY = SIMPLE_FACTORY.asPredicate(NS_URI +"/namePersonalFamily");
    Property BIB_NAME_PERSONAL_GIVEN = SIMPLE_FACTORY.asPredicate(NS_URI + "/namePersonalGiven");
    Property BIB_NAME_CORPORATE = SIMPLE_FACTORY.asPredicate(NS_URI + "/nameCorporate");
    Property BIB_NAME_CONFERENCE = SIMPLE_FACTORY.asPredicate(NS_URI + "/nameConference");
    
    Property BIB_SUBJECT = SIMPLE_FACTORY.asPredicate(NS_URI + "/subject");
    Property BIB_SUBJECT_PLACE = SIMPLE_FACTORY.asPredicate(NS_URI + "/subjectPlace");
    Property BIB_SUBJECT_TITLE = SIMPLE_FACTORY.asPredicate(NS_URI + "/subjectTitle");
    Property BIB_SUBJECT_NAME = SIMPLE_FACTORY.asPredicate(NS_URI + "/subjectName");
    Property BIB_SUBJECT_OCCUPATION = SIMPLE_FACTORY.asPredicate(NS_URI +"/subjectOccupation");
    
    Property BIB_DATE = SIMPLE_FACTORY.asPredicate(NS_URI + "/date");
    Property BIB_DATE_ISSUED = SIMPLE_FACTORY.asPredicate(NS_URI + "/dateIssued");
    Property BIB_DATE_CREATED = SIMPLE_FACTORY.asPredicate(NS_URI + "/dateCreated");
    Property BIB_DATE_VALID = SIMPLE_FACTORY.asPredicate(NS_URI + "/dateValid");
    Property BIB_DATE_MODIFIED = SIMPLE_FACTORY.asPredicate(NS_URI + "/dateModified");
    Property BIB_DATE_COPYRIGHT = SIMPLE_FACTORY.asPredicate(NS_URI + "/dateCopyright");

    Property BIB_VOLUME = SIMPLE_FACTORY.asPredicate(NS_URI + "/volume");
    Property BIB_ISSUE = SIMPLE_FACTORY.asPredicate(NS_URI + "/issue");
    Property BIB_STARTPAGE = SIMPLE_FACTORY.asPredicate(NS_URI + "/startPage");
    Property BIB_ENDPAGE = SIMPLE_FACTORY.asPredicate(NS_URI + "/endPage");
    Property BIB_GENRE = SIMPLE_FACTORY.asPredicate(NS_URI + "/genre");
    Property BIB_AUDIENCE = SIMPLE_FACTORY.asPredicate(NS_URI + "/audience");
    Property BIB_CLASSIFICATION = SIMPLE_FACTORY.asPredicate(NS_URI +"/classification");
    Property BIB_ORIGINPLACE = SIMPLE_FACTORY.asPredicate(NS_URI + "/originPlace");
    Property BIB_EDITION = SIMPLE_FACTORY.asPredicate(NS_URI + "/edition");
    Property BIB_ISSUANCE = SIMPLE_FACTORY.asPredicate(NS_URI + "/issuance");

}
