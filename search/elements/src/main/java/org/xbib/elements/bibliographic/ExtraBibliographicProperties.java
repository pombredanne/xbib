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
 * Extra bibliographic elements as RDF properties
 */
public interface ExtraBibliographicProperties extends XBIB {

    final SimpleFactory<Identifier,Property,Node> SIMPLE_FACTORY = SimpleFactory.getInstance();

    Property XBIB_NAME_PERSONAL_IDENTIFIER = SIMPLE_FACTORY.asPredicate(NS_URI + "namePersonalID");
    Property XBIB_NAME_PERSONAL_ROLE = SIMPLE_FACTORY.asPredicate(NS_URI + "namePersonalRole");
    Property XBIB_NAME_PERSONAL_ALTERNATIVE = SIMPLE_FACTORY.asPredicate(NS_URI + "namePersonalAlt");

    Property XBIB_NAME_CORPORATE_IDENTIFIER = SIMPLE_FACTORY.asPredicate(NS_URI + "nameCorporateID");
    Property XBIB_NAME_CORPORATE_ROLE = SIMPLE_FACTORY.asPredicate(NS_URI + "nameCorporateRole");
    Property XBIB_NAME_CORPORATE_ALTERNATIVE = SIMPLE_FACTORY.asPredicate(NS_URI + "nameCorporateAlt");

    Property XBIB_NAME_CONFERENCE_IDENTIFIER = SIMPLE_FACTORY.asPredicate(NS_URI + "nameConferenceID");
    Property XBIB_NAME_CONFERENCE_ROLE = SIMPLE_FACTORY.asPredicate(NS_URI + "nameConferenceRole");
    Property XBIB_NAME_CONFERENCE_ALTERNATIVE = SIMPLE_FACTORY.asPredicate(NS_URI + "nameConferenceAlt");

    /**
     * RDF style extension
     */
    Property XBIB_NAME = SIMPLE_FACTORY.asPredicate(NS_URI + "name");
    Property XBIB_VALUE = SIMPLE_FACTORY.asPredicate(NS_URI + "value");
    Property XBIB_TYPE = SIMPLE_FACTORY.asPredicate(NS_URI + "type");
    
    /**
     * Dublin Core extension
     */
    Property XBIB_COVERAGE_LOCATION_PLACE_NAME = SIMPLE_FACTORY.asPredicate(NS_URI + "coverageLocationPlaceName");
    Property XBIB_COVERAGE_LOCATION_PLACE_IDENTIFIER = SIMPLE_FACTORY.asPredicate(NS_URI + "coverageLocationPlaceID");

    Property XBIB_DESCRIPTION = SIMPLE_FACTORY.asPredicate(NS_URI + "description");
    Property XBIB_DESCRIPTION_FOOTNOTE = SIMPLE_FACTORY.asPredicate(NS_URI + "descriptionFootNote");
    Property XBIB_DESCRIPTION_THESIS = SIMPLE_FACTORY.asPredicate(NS_URI + "descriptionThesis");
    Property XBIB_DESCRIPTION_CREATOR = SIMPLE_FACTORY.asPredicate(NS_URI + "creatorDescription");
    Property XBIB_DESCRIPTION_COLLECTION = SIMPLE_FACTORY.asPredicate(NS_URI + "collectionDescription");

    Property XBIB_IDENTIFIER_AUTHORITY_SYSID = SIMPLE_FACTORY.asPredicate(NS_URI + "sysID");
    Property XBIB_IDENTIFIER_AUTHORITY_MAB = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityMAB");
    Property XBIB_IDENTIFIER_AUTHORITY_MAB_WHOLE = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityMABWhole");
    Property XBIB_IDENTIFIER_AUTHORITY_EKI = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityEKI");
    Property XBIB_IDENTIFIER_AUTHORITY_NWBIB = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityNWBIB");
    Property XBIB_IDENTIFIER_AUTHORITY_NLZ = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityNLZ");
    Property XBIB_IDENTIFIER_AUTHORITY_ZDB = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityZDB");
    Property XBIB_IDENTIFIER_AUTHORITY_LOC = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityLOC");
    Property XBIB_IDENTIFIER_AUTHORITY_OCLC = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityOCLC");
    Property XBIB_IDENTIFIER_AUTHORITY_DNB = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityDNB");
    Property XBIB_IDENTIFIER_AUTHORITY_ISIL = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityISIL");
    Property XBIB_IDENTIFIER_AUTHORITY_EAN = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityEAN"); // ISBN-13
    Property XBIB_IDENTIFIER_AUTHORITY_ISBN = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityISBN");
    Property XBIB_IDENTIFIER_AUTHORITY_ISBN_ORIGINAL = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityOriginISBN");
    Property XBIB_IDENTIFIER_AUTHORITY_ISMN = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityISMN");
    Property XBIB_IDENTIFIER_AUTHORITY_ISMN_ORIGINAL = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityOriginISMN");
    Property XBIB_IDENTIFIER_AUTHORITY_ISSN = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityISSN");
    Property XBIB_IDENTIFIER_AUTHORITY_ISSN_ORIGINAL = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityOriginISSN");
    Property XBIB_IDENTIFIER_AUTHORITY_DOI = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityDOI");
    Property XBIB_IDENTIFIER_AUTHORITY_ISRN = SIMPLE_FACTORY.asPredicate(NS_URI + "identifierAuthorityISRN");

    Property XBIB_LANGUAGE_639_1 = SIMPLE_FACTORY.asPredicate(NS_URI + "languageISO6391");
    Property XBIB_LANGUAGE_639_2B = SIMPLE_FACTORY.asPredicate(NS_URI + "languageISO6392b");

    Property XBIB_PUBLISHER_NAME = SIMPLE_FACTORY.asPredicate(NS_URI + "publisherName");
    Property XBIB_PUBLISHER_NAME_ALTERNATIVE = SIMPLE_FACTORY.asPredicate(NS_URI + "publisherNameAlt");
    Property XBIB_PUBLISHER_PLACE = SIMPLE_FACTORY.asPredicate(NS_URI + "publisherPlace");

    Property XBIB_RELATION_NAME = SIMPLE_FACTORY.asPredicate(NS_URI + "relationName");
    Property XBIB_RELATION_TYPE = SIMPLE_FACTORY.asPredicate(NS_URI + "relationType");
    Property XBIB_RELATION_VALUE = SIMPLE_FACTORY.asPredicate(NS_URI + "relationValue");
    Property XBIB_RELATION_VALUE_IDENTIFIER = SIMPLE_FACTORY.asPredicate(NS_URI + "relationValueID");
    Property XBIB_RELATION_LABEL = SIMPLE_FACTORY.asPredicate(NS_URI + "relationLabel");
    Property XBIB_RELATION_UNIFORM_RESOURCE_LOCATOR = SIMPLE_FACTORY.asPredicate(NS_URI + "relationURL");
    Property XBIB_RELATION_UNIFORM_RESOURCE_LOCATOR_LABEL = SIMPLE_FACTORY.asPredicate(NS_URI + "relationURLLabel");
    
    Property XBIB_SOURCE = SIMPLE_FACTORY.asPredicate(NS_URI + "source");
    Property XBIB_SOURCE_TITLE = SIMPLE_FACTORY.asPredicate(NS_URI + "sourceTitle");
    Property XBIB_SOURCE_TITLE_SUB = SIMPLE_FACTORY.asPredicate(NS_URI + "sourceTitleSub");
    Property XBIB_SOURCE_TITLE_WHOLE = SIMPLE_FACTORY.asPredicate(NS_URI + "sourceTitleWhole");
    Property XBIB_SOURCE_CREATOR = SIMPLE_FACTORY.asPredicate(NS_URI + "sourceCreator");
    Property XBIB_SOURCE_DESCRIPTION = SIMPLE_FACTORY.asPredicate(NS_URI + "sourceDescription");
    Property XBIB_SOURCE_DESCRIPTION_VOLUME = SIMPLE_FACTORY.asPredicate(NS_URI + "sourceDescriptionVolume");
    Property XBIB_SOURCE_EDITION = SIMPLE_FACTORY.asPredicate(NS_URI + "sourceEdition");
    Property XBIB_SOURCE_DATE_ISSUED = SIMPLE_FACTORY.asPredicate(NS_URI + "sourceDateIssued");
    Property XBIB_SOURCE_IDENTIFIER = SIMPLE_FACTORY.asPredicate(NS_URI + "sourceID");
    Property XBIB_SOURCE_PUBLISHER_PLACE = SIMPLE_FACTORY.asPredicate(NS_URI + "sourcePublisherPlace");
    
    Property XBIB_SUBJECT_AUTHORITY_ASB = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityASB");
    Property XBIB_SUBJECT_AUTHORITY_BAY = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityBAY");
    Property XBIB_SUBJECT_AUTHORITY_BK = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityBK");
    Property XBIB_SUBJECT_AUTHORITY_BNB = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityBNB");
    Property XBIB_SUBJECT_AUTHORITY_DDC = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityDDC");
    Property XBIB_SUBJECT_AUTHORITY_DOPAED = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityDOPAED");
    Property XBIB_SUBJECT_AUTHORITY_DNB = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityDNB");
    Property XBIB_SUBJECT_AUTHORITY_EKZ = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityEKZ");
    Property XBIB_SUBJECT_AUTHORITY_EPP = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityEPP");
    Property XBIB_SUBJECT_AUTHORITY_GHB = SIMPLE_FACTORY.asPredicate( NS_URI + "subjectAuthorityGHB");
    Property XBIB_SUBJECT_AUTHORITY_IFZ = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityIFZ");
    Property XBIB_SUBJECT_AUTHORITY_KAB = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityKAB");
    Property XBIB_SUBJECT_AUTHORITY_LCC = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityLCC");
    Property XBIB_SUBJECT_AUTHORITY_MSC = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityMSC");
    Property XBIB_SUBJECT_AUTHORITY_NDC = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityNDC");
    Property XBIB_SUBJECT_AUTHORITY_NDLC = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityNDLC");
    Property XBIB_SUBJECT_AUTHORITY_NLZ = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityNLZ");
    Property XBIB_SUBJECT_AUTHORITY_NWBIB = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityNWBIB");
    Property XBIB_SUBJECT_AUTHORITY_RVK = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityRVK");
    Property XBIB_SUBJECT_AUTHORITY_RPB = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityRPB");
    Property XBIB_SUBJECT_AUTHORITY_SSD = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthoritySSD");
    Property XBIB_SUBJECT_AUTHORITY_SFB = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthoritySFB");
    Property XBIB_SUBJECT_AUTHORITY_RSWK = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityRSWK");
    Property XBIB_SUBJECT_AUTHORITY_TUM = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityTUM");
    Property XBIB_SUBJECT_AUTHORITY_UDC = SIMPLE_FACTORY.asPredicate( NS_URI + "subjectAuthorityUDC");
    Property XBIB_SUBJECT_AUTHORITY_ZDB = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectAuthorityZDB");
    Property XBIB_SUBJECT_RSWK = SIMPLE_FACTORY.asPredicate(NS_URI +"rswk");
    Property XBIB_SUBJECT_RSWK_ID = SIMPLE_FACTORY.asPredicate(NS_URI + "subjectID");
    Property XBIB_SUBJECT_RSWK_TOPIC = SIMPLE_FACTORY.asPredicate(NS_URI +"subjectTopic");
    Property XBIB_SUBJECT_RSWK_GENRE = SIMPLE_FACTORY.asPredicate(NS_URI +"subjectGenre");
    Property XBIB_SUBJECT_RSWK_PERSON = SIMPLE_FACTORY.asPredicate(NS_URI +"subjectPerson");
    Property XBIB_SUBJECT_RSWK_CORPORATE = SIMPLE_FACTORY.asPredicate(NS_URI +"subjectCorporate");
    Property XBIB_SUBJECT_RSWK_SPATIAL = SIMPLE_FACTORY.asPredicate(NS_URI +"subjectSpatial");
    Property XBIB_SUBJECT_RSWK_TEMPORAL = SIMPLE_FACTORY.asPredicate(NS_URI +"subjectTemporal");
    Property XBIB_SUBJECT_RSWK_TITLE = SIMPLE_FACTORY.asPredicate(NS_URI +"subjectTitle");
    Property XBIB_SUBJECT_RSWK_SUB = SIMPLE_FACTORY.asPredicate(NS_URI +"subjectSub");

    Property XBIB_TITLE = SIMPLE_FACTORY.asPredicate(NS_URI + "title");
    Property XBIB_TITLE_ALTERNATIVE = SIMPLE_FACTORY.asPredicate(NS_URI + "titleAlternative");
    Property XBIB_TITLE_HEADING = SIMPLE_FACTORY.asPredicate(NS_URI + "titleHeading");
    Property XBIB_TITLE_SUB = SIMPLE_FACTORY.asPredicate(NS_URI + "titleSub");
    Property XBIB_TITLE_PART = SIMPLE_FACTORY.asPredicate(NS_URI + "titlePart");
    Property XBIB_TITLE_WHOLE = SIMPLE_FACTORY.asPredicate(NS_URI + "titleWhole");
    
    Property XBIB_TYPE_RECORD = SIMPLE_FACTORY.asPredicate(NS_URI + "recordType");
    Property XBIB_TYPE_DESCRIPTION = SIMPLE_FACTORY.asPredicate(NS_URI + "typeDescription");
    
    Property BOOST = SIMPLE_FACTORY.asPredicate(NS_URI + "boost");
    Property XBIB_HAS_GND = SIMPLE_FACTORY.asPredicate(NS_URI +"hasGND");
    
    /**
     * Issuance
     * 
     */
    String ISSUANCE_CONTINUING = "continuing";
    String ISSUANCE_MONOGRAPHIC = "monographic";
    String ISSUANCE_SINGLE_UNIT = "single unit";
    String ISSUANCE_MULTIPART_MONOGRAPH = "multipart monograph";
    String ISSUANCE_SERIAL = "serial";
    String ISSUANCE_INTEGRATING_RESOURCE = "integrating resource";
    
    /**
     * Type of resource
     */
    String TYPE_TEXT = "text";
    String TYPE_CARTOGRAPHIC = "cartographic";
    String TYPE_NOTATED_MUSIC = "notated music"; 
    String TYPE_SOUND_RECORDING_MUSICAL = "sound recording-musical";
    String TYPE_SOUND_RECORDING_NONMUSICAL = "sound recording-nonmusical";
    String TYPE_SOUND_RECORDING = "sound recording";
    String TYPE_STILL_IMAGE = "still image";
    String TYPE_MOVING_IMAGE ="moving image";
    String TYPE_3D_OBJECT = "three dimensional object";
    String TYPE_SOFTWARE = "software";
    String TYPE_MULTIMEDIA = "multimedia";
    String TYPE_MIXED_MATERIAL = "mixed material";
    
}
