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

import org.xbib.rdf.Factory;
import org.xbib.rdf.Property;

/**
 * Extra bibliographic elements as RDF properties
 */
public interface ExtraBibliographicProperties extends XBIB {

    final Factory factory = Factory.getInstance();
    Property XBIB_NAME_PERSONAL_IDENTIFIER = factory.create(NS_URI + "namePersonalID");
    Property XBIB_NAME_PERSONAL_ROLE = factory.create(NS_URI + "namePersonalRole");
    Property XBIB_NAME_PERSONAL_ALTERNATIVE = factory.create(NS_URI + "namePersonalAlt");

    Property XBIB_NAME_CORPORATE_IDENTIFIER = factory.create(NS_URI + "nameCorporateID");
    Property XBIB_NAME_CORPORATE_ROLE = factory.create(NS_URI + "nameCorporateRole");
    Property XBIB_NAME_CORPORATE_ALTERNATIVE = factory.create(NS_URI + "nameCorporateAlt");

    Property XBIB_NAME_CONFERENCE_IDENTIFIER = factory.create(NS_URI + "nameConferenceID");
    Property XBIB_NAME_CONFERENCE_ROLE = factory.create(NS_URI + "nameConferenceRole");
    Property XBIB_NAME_CONFERENCE_ALTERNATIVE = factory.create(NS_URI + "nameConferenceAlt");

    /**
     * RDF style extension
     */
    Property XBIB_NAME = factory.create(NS_URI + "name");
    Property XBIB_VALUE = factory.create(NS_URI + "value");
    Property XBIB_TYPE = factory.create(NS_URI + "type");
    
    /**
     * Dublin Core extension
     */
    Property XBIB_COVERAGE_LOCATION_PLACE_NAME = factory.create(NS_URI + "coverageLocationPlaceName");
    Property XBIB_COVERAGE_LOCATION_PLACE_IDENTIFIER = factory.create(NS_URI + "coverageLocationPlaceID");

    Property XBIB_DESCRIPTION = factory.create(NS_URI + "description");
    Property XBIB_DESCRIPTION_FOOTNOTE = factory.create(NS_URI + "descriptionFootNote");
    Property XBIB_DESCRIPTION_THESIS = factory.create(NS_URI + "descriptionThesis");
    Property XBIB_DESCRIPTION_CREATOR = factory.create(NS_URI + "creatorDescription");
    Property XBIB_DESCRIPTION_COLLECTION = factory.create(NS_URI + "collectionDescription");

    Property XBIB_IDENTIFIER_AUTHORITY_SYSID = factory.create(NS_URI + "sysID");
    Property XBIB_IDENTIFIER_AUTHORITY_MAB = factory.create(NS_URI + "identifierAuthorityMAB");
    Property XBIB_IDENTIFIER_AUTHORITY_MAB_WHOLE = factory.create(NS_URI + "identifierAuthorityMABWhole");
    Property XBIB_IDENTIFIER_AUTHORITY_EKI = factory.create(NS_URI + "identifierAuthorityEKI");
    Property XBIB_IDENTIFIER_AUTHORITY_NWBIB = factory.create(NS_URI + "identifierAuthorityNWBIB");
    Property XBIB_IDENTIFIER_AUTHORITY_NLZ = factory.create(NS_URI + "identifierAuthorityNLZ");
    Property XBIB_IDENTIFIER_AUTHORITY_ZDB = factory.create(NS_URI + "identifierAuthorityZDB");
    Property XBIB_IDENTIFIER_AUTHORITY_LOC = factory.create(NS_URI + "identifierAuthorityLOC");
    Property XBIB_IDENTIFIER_AUTHORITY_OCLC = factory.create(NS_URI + "identifierAuthorityOCLC");
    Property XBIB_IDENTIFIER_AUTHORITY_DNB = factory.create(NS_URI + "identifierAuthorityDNB");
    Property XBIB_IDENTIFIER_AUTHORITY_ISIL = factory.create(NS_URI + "identifierAuthorityISIL");
    Property XBIB_IDENTIFIER_AUTHORITY_EAN = factory.create(NS_URI + "identifierAuthorityEAN"); // ISBN-13
    Property XBIB_IDENTIFIER_AUTHORITY_ISBN = factory.create(NS_URI + "identifierAuthorityISBN");
    Property XBIB_IDENTIFIER_AUTHORITY_ISBN_ORIGINAL = factory.create(NS_URI + "identifierAuthorityOriginISBN");
    Property XBIB_IDENTIFIER_AUTHORITY_ISMN = factory.create(NS_URI + "identifierAuthorityISMN");
    Property XBIB_IDENTIFIER_AUTHORITY_ISMN_ORIGINAL = factory.create(NS_URI + "identifierAuthorityOriginISMN");
    Property XBIB_IDENTIFIER_AUTHORITY_ISSN = factory.create(NS_URI + "identifierAuthorityISSN");
    Property XBIB_IDENTIFIER_AUTHORITY_ISSN_ORIGINAL = factory.create(NS_URI + "identifierAuthorityOriginISSN");
    Property XBIB_IDENTIFIER_AUTHORITY_DOI = factory.create(NS_URI + "identifierAuthorityDOI");
    Property XBIB_IDENTIFIER_AUTHORITY_ISRN = factory.create(NS_URI + "identifierAuthorityISRN");

    Property XBIB_LANGUAGE_639_1 = factory.create(NS_URI + "languageISO6391");
    Property XBIB_LANGUAGE_639_2B = factory.create(NS_URI + "languageISO6392b");

    Property XBIB_PUBLISHER_NAME = factory.create(NS_URI + "publisherName");
    Property XBIB_PUBLISHER_NAME_ALTERNATIVE = factory.create(NS_URI + "publisherNameAlt");
    Property XBIB_PUBLISHER_PLACE = factory.create(NS_URI + "publisherPlace");

    Property XBIB_RELATION_NAME = factory.create(NS_URI + "relationName");
    Property XBIB_RELATION_TYPE = factory.create(NS_URI + "relationType");
    Property XBIB_RELATION_VALUE = factory.create(NS_URI + "relationValue");
    Property XBIB_RELATION_VALUE_IDENTIFIER = factory.create(NS_URI + "relationValueID");
    Property XBIB_RELATION_LABEL = factory.create(NS_URI + "relationLabel");
    Property XBIB_RELATION_UNIFORM_RESOURCE_LOCATOR = factory.create(NS_URI + "relationURL");
    Property XBIB_RELATION_UNIFORM_RESOURCE_LOCATOR_LABEL = factory.create(NS_URI + "relationURLLabel");
    
    Property XBIB_SOURCE = factory.create(NS_URI + "source");
    Property XBIB_SOURCE_TITLE = factory.create(NS_URI + "sourceTitle");
    Property XBIB_SOURCE_TITLE_SUB = factory.create(NS_URI + "sourceTitleSub");
    Property XBIB_SOURCE_TITLE_WHOLE = factory.create(NS_URI + "sourceTitleWhole");
    Property XBIB_SOURCE_CREATOR = factory.create(NS_URI + "sourceCreator");
    Property XBIB_SOURCE_DESCRIPTION = factory.create(NS_URI + "sourceDescription");
    Property XBIB_SOURCE_DESCRIPTION_VOLUME = factory.create(NS_URI + "sourceDescriptionVolume");
    Property XBIB_SOURCE_EDITION = factory.create(NS_URI + "sourceEdition");
    Property XBIB_SOURCE_DATE_ISSUED = factory.create(NS_URI + "sourceDateIssued");
    Property XBIB_SOURCE_IDENTIFIER = factory.create(NS_URI + "sourceID");
    Property XBIB_SOURCE_PUBLISHER_PLACE = factory.create(NS_URI + "sourcePublisherPlace");
    
    Property XBIB_SUBJECT_AUTHORITY_ASB = factory.create(NS_URI + "subjectAuthorityASB");
    Property XBIB_SUBJECT_AUTHORITY_BAY = factory.create(NS_URI + "subjectAuthorityBAY");
    Property XBIB_SUBJECT_AUTHORITY_BK = factory.create(NS_URI + "subjectAuthorityBK");
    Property XBIB_SUBJECT_AUTHORITY_BNB = factory.create(NS_URI + "subjectAuthorityBNB");
    Property XBIB_SUBJECT_AUTHORITY_DDC = factory.create(NS_URI + "subjectAuthorityDDC");
    Property XBIB_SUBJECT_AUTHORITY_DOPAED = factory.create(NS_URI + "subjectAuthorityDOPAED");
    Property XBIB_SUBJECT_AUTHORITY_DNB = factory.create(NS_URI + "subjectAuthorityDNB");
    Property XBIB_SUBJECT_AUTHORITY_EKZ = factory.create(NS_URI + "subjectAuthorityEKZ");
    Property XBIB_SUBJECT_AUTHORITY_EPP = factory.create(NS_URI + "subjectAuthorityEPP");
    Property XBIB_SUBJECT_AUTHORITY_GHB = factory.create( NS_URI + "subjectAuthorityGHB");
    Property XBIB_SUBJECT_AUTHORITY_IFZ = factory.create(NS_URI + "subjectAuthorityIFZ");
    Property XBIB_SUBJECT_AUTHORITY_KAB = factory.create(NS_URI + "subjectAuthorityKAB");
    Property XBIB_SUBJECT_AUTHORITY_LCC = factory.create(NS_URI + "subjectAuthorityLCC");
    Property XBIB_SUBJECT_AUTHORITY_MSC = factory.create(NS_URI + "subjectAuthorityMSC");
    Property XBIB_SUBJECT_AUTHORITY_NDC = factory.create(NS_URI + "subjectAuthorityNDC");
    Property XBIB_SUBJECT_AUTHORITY_NDLC = factory.create(NS_URI + "subjectAuthorityNDLC");
    Property XBIB_SUBJECT_AUTHORITY_NLZ = factory.create(NS_URI + "subjectAuthorityNLZ");
    Property XBIB_SUBJECT_AUTHORITY_NWBIB = factory.create(NS_URI + "subjectAuthorityNWBIB");
    Property XBIB_SUBJECT_AUTHORITY_RVK = factory.create(NS_URI + "subjectAuthorityRVK");
    Property XBIB_SUBJECT_AUTHORITY_RPB = factory.create(NS_URI + "subjectAuthorityRPB");
    Property XBIB_SUBJECT_AUTHORITY_SSD = factory.create(NS_URI + "subjectAuthoritySSD");
    Property XBIB_SUBJECT_AUTHORITY_SFB = factory.create(NS_URI + "subjectAuthoritySFB");
    Property XBIB_SUBJECT_AUTHORITY_RSWK = factory.create(NS_URI + "subjectAuthorityRSWK");
    Property XBIB_SUBJECT_AUTHORITY_TUM = factory.create(NS_URI + "subjectAuthorityTUM");
    Property XBIB_SUBJECT_AUTHORITY_UDC =factory.create( NS_URI + "subjectAuthorityUDC");
    Property XBIB_SUBJECT_AUTHORITY_ZDB = factory.create(NS_URI + "subjectAuthorityZDB");
    Property XBIB_SUBJECT_RSWK = factory.create(NS_URI +"rswk");
    Property XBIB_SUBJECT_RSWK_ID = factory.create(NS_URI + "subjectID");
    Property XBIB_SUBJECT_RSWK_TOPIC = factory.create(NS_URI +"subjectTopic");
    Property XBIB_SUBJECT_RSWK_GENRE = factory.create(NS_URI +"subjectGenre");
    Property XBIB_SUBJECT_RSWK_PERSON = factory.create(NS_URI +"subjectPerson");
    Property XBIB_SUBJECT_RSWK_CORPORATE = factory.create(NS_URI +"subjectCorporate");
    Property XBIB_SUBJECT_RSWK_SPATIAL = factory.create(NS_URI +"subjectSpatial");
    Property XBIB_SUBJECT_RSWK_TEMPORAL = factory.create(NS_URI +"subjectTemporal");
    Property XBIB_SUBJECT_RSWK_TITLE = factory.create(NS_URI +"subjectTitle");
    Property XBIB_SUBJECT_RSWK_SUB = factory.create(NS_URI +"subjectSub");

    Property XBIB_TITLE = factory.create(NS_URI + "title");
    Property XBIB_TITLE_ALTERNATIVE = factory.create(NS_URI + "titleAlternative");
    Property XBIB_TITLE_HEADING = factory.create(NS_URI + "titleHeading");
    Property XBIB_TITLE_SUB = factory.create(NS_URI + "titleSub");
    Property XBIB_TITLE_PART = factory.create(NS_URI + "titlePart");
    Property XBIB_TITLE_WHOLE = factory.create(NS_URI + "titleWhole");
    
    Property XBIB_TYPE_RECORD = factory.create(NS_URI + "recordType");
    Property XBIB_TYPE_DESCRIPTION = factory.create(NS_URI + "typeDescription");
    
    Property BOOST = factory.create(NS_URI + "boost");
    Property XBIB_HAS_GND = factory.create(NS_URI +"hasGND");
    
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
