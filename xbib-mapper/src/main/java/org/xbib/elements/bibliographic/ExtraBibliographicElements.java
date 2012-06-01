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

/**
 *  Extra bibliographic elements
 *
 */
public interface ExtraBibliographicElements extends XBIB {

    String NAME_PERSONAL_IDENTIFIER = NS_URI + "namePersonalID";
    String NAME_PERSONAL_ROLE = NS_URI + "namePersonalRole";
    String NAME_PERSONAL_ALTERNATIVE = NS_URI + "namePersonalAlt";

    String NAME_CORPORATE_IDENTIFIER = NS_URI + "nameCorporateID";
    String NAME_CORPORATE_ROLE = NS_URI + "nameCorporateRole";
    String NAME_CORPORATE_ALTERNATIVE = NS_URI + "nameCorporateAlt";

    String NAME_CONFERENCE_IDENTIFIER = NS_URI + "nameConferenceID";
    String NAME_CONFERENCE_ROLE = NS_URI + "nameConferenceRole";
    String NAME_CONFERENCE_ALTERNATIVE = NS_URI + "nameConferenceAlt";

    /**
     * RDF style extension
     */
    String XBIB_NAME = NS_URI + "name";
    String XBIB_VALUE = NS_URI + "value";
    String XBIB_TYPE = NS_URI + "type";
    
    /**
     * Dublin Core- style extension
     */
    String COVERAGE_LOCATION_PLACE_NAME = NS_URI + "coverageLocationPlaceName";
    String COVERAGE_LOCATION_PLACE_IDENTIFIER = NS_URI + "coverageLocationPlaceID";

    String DESCRIPTION = NS_URI + "description";
    String DESCRIPTION_FOOTNOTE = NS_URI + "descriptionFootNote";
    String DESCRIPTION_THESIS = NS_URI + "descriptionThesis";
    String DESCRIPTION_CLOB_ISODIS25577 = NS_URI + "descriptionISODIS25577";
    String DESCRIPTION_CREATOR = NS_URI + "creatorDescription";
    String DESCRIPTION_COLLECTION = NS_URI + "collectionDescription";

    String IDENTIFIER_AUTHORITY_SYSID = NS_URI + "sysID";
    String IDENTIFIER_AUTHORITY_MAB = NS_URI + "identifierAuthorityMAB";
    String IDENTIFIER_AUTHORITY_MAB_WHOLE = NS_URI + "identifierAuthorityMABWhole";
    String IDENTIFIER_AUTHORITY_EKI = NS_URI + "identifierAuthorityEKI";
    String IDENTIFIER_AUTHORITY_NWBIB = NS_URI + "identifierAuthorityNWBIB";
    String IDENTIFIER_AUTHORITY_NLZ = NS_URI + "identifierAuthorityNLZ";
    String IDENTIFIER_AUTHORITY_ZDB = NS_URI + "identifierAuthorityZDB";
    String IDENTIFIER_AUTHORITY_LOC = NS_URI + "identifierAuthorityLOC";
    String IDENTIFIER_AUTHORITY_OCLC = NS_URI + "identifierAuthorityOCLC";
    String IDENTIFIER_AUTHORITY_DNB = NS_URI + "identifierAuthorityDNB";
    String IDENTIFIER_AUTHORITY_ISIL = NS_URI + "identifierAuthorityISIL";
    String IDENTIFIER_AUTHORITY_EAN = NS_URI + "identifierAuthorityEAN"; // ISBN-13
    String IDENTIFIER_AUTHORITY_ISBN = NS_URI + "identifierAuthorityISBN";
    String IDENTIFIER_AUTHORITY_ISBN_ORIGINAL = NS_URI + "identifierAuthorityOriginISBN";
    String IDENTIFIER_AUTHORITY_ISMN = NS_URI + "identifierAuthorityISMN";
    String IDENTIFIER_AUTHORITY_ISMN_ORIGINAL = NS_URI + "identifierAuthorityOriginISMN";
    String IDENTIFIER_AUTHORITY_ISSN = NS_URI + "identifierAuthorityISSN";
    String IDENTIFIER_AUTHORITY_ISSN_ORIGINAL = NS_URI + "identifierAuthorityOriginISSN";
    String IDENTIFIER_AUTHORITY_DOI = NS_URI + "identifierAuthorityDOI";
    String IDENTIFIER_AUTHORITY_ISRN = NS_URI + "identifierAuthorityISRN";

    String LANGUAGE_639_1 = NS_URI + "languageISO6391";
    String LANGUAGE_639_2B = NS_URI + "languageISO6392b";

    String PUBLISHER_NAME = NS_URI + "publisherName";
    String PUBLISHER_NAME_ALTERNATIVE = NS_URI + "publisherNameAlt";
    String PUBLISHER_PLACE = NS_URI + "publisherPlace";

    String RELATION_NAME = NS_URI + "relationName";
    String RELATION_TYPE = NS_URI + "relationType";
    String RELATION_VALUE = NS_URI + "relationValue";

    String RELATION_VALUE_IDENTIFIER = NS_URI + "relationValueID";
    String RELATION_LABEL = NS_URI + "relationLabel";
    String RELATION_UNIFORM_RESOURCE_LOCATOR = NS_URI + "relationURL";
    String RELATION_UNIFORM_RESOURCE_LOCATOR_LABEL = NS_URI + "relationURLLabel";
    
    String SOURCE = NS_URI + "source";
    String SOURCE_TITLE = NS_URI + "sourceTitle";
    String SOURCE_TITLE_SUB = NS_URI + "sourceTitleSub";
    String SOURCE_TITLE_WHOLE = NS_URI + "sourceTitleWhole";
    String SOURCE_CREATOR = NS_URI + "sourceCreator";
    String SOURCE_DESCRIPTION = NS_URI + "sourceDescription";
    String SOURCE_DESCRIPTION_VOLUME = NS_URI + "sourceDescriptionVolume";
    String SOURCE_EDITION = NS_URI + "sourceEdition";
    String SOURCE_DATE_ISSUED = NS_URI + "sourceDateIssued";
    String SOURCE_IDENTIFIER = NS_URI + "sourceID";
    String SOURCE_PUBLISHER_PLACE = NS_URI + "sourcePublisherPlace";
    
    String SUBJECT_AUTHORITY_ASB = NS_URI + "subjectAuthorityASB";
    String SUBJECT_AUTHORITY_BAY = NS_URI + "subjectAuthorityBAY";
    String SUBJECT_AUTHORITY_BK = NS_URI + "subjectAuthorityBK";
    String SUBJECT_AUTHORITY_BNB = NS_URI + "subjectAuthorityBNB";
    String SUBJECT_AUTHORITY_DDC = NS_URI + "subjectAuthorityDDC";
    String SUBJECT_AUTHORITY_DOPAED = NS_URI + "subjectAuthorityDOPAED";
    String SUBJECT_AUTHORITY_DNB = NS_URI + "subjectAuthorityDNB";
    String SUBJECT_AUTHORITY_EKZ = NS_URI + "subjectAuthorityEKZ";
    String SUBJECT_AUTHORITY_EPP = NS_URI + "subjectAuthorityEPP";
    String SUBJECT_AUTHORITY_GHB = NS_URI + "subjectAuthorityGHB";
    String SUBJECT_AUTHORITY_IFZ = NS_URI + "subjectAuthorityIFZ";
    String SUBJECT_AUTHORITY_KAB = NS_URI + "subjectAuthorityKAB";
    String SUBJECT_AUTHORITY_LCC = NS_URI + "subjectAuthorityLCC";
    String SUBJECT_AUTHORITY_MSC = NS_URI + "subjectAuthorityMSC";
    String SUBJECT_AUTHORITY_NDC = NS_URI + "subjectAuthorityNDC";
    String SUBJECT_AUTHORITY_NDLC = NS_URI + "subjectAuthorityNDLC";
    String SUBJECT_AUTHORITY_NLZ = NS_URI + "subjectAuthorityNLZ";
    String SUBJECT_AUTHORITY_NWBIB = NS_URI + "subjectAuthorityNWBIB";
    String SUBJECT_AUTHORITY_RVK = NS_URI + "subjectAuthorityRVK";
    String SUBJECT_AUTHORITY_RPB = NS_URI + "subjectAuthorityRPB";
    String SUBJECT_AUTHORITY_SSD = NS_URI + "subjectAuthoritySSD";
    String SUBJECT_AUTHORITY_SFB = NS_URI + "subjectAuthoritySFB";
    String SUBJECT_AUTHORITY_RSWK = NS_URI + "subjectAuthorityRSWK";
    String SUBJECT_AUTHORITY_TUM = NS_URI + "subjectAuthorityTUM";
    String SUBJECT_AUTHORITY_UDC = NS_URI + "subjectAuthorityUDC";
    String SUBJECT_AUTHORITY_ZDB = NS_URI + "subjectAuthorityZDB";
    String SUBJECT_RSWK = NS_URI +"rswk";
    String SUBJECT_RSWK_ID = NS_URI + "subjectID";
    String SUBJECT_RSWK_TOPIC = NS_URI +"subjectTopic";
    String SUBJECT_RSWK_GENRE = NS_URI +"subjectGenre";
    String SUBJECT_RSWK_PERSON = NS_URI +"subjectPerson";
    String SUBJECT_RSWK_CORPORATE = NS_URI +"subjectCorporate";
    String SUBJECT_RSWK_SPATIAL = NS_URI +"subjectSpatial";
    String SUBJECT_RSWK_TEMPORAL = NS_URI +"subjectTemporal";
    String SUBJECT_RSWK_TITLE = NS_URI +"subjectTitle";
    String SUBJECT_RSWK_SUB = NS_URI +"subjectSub";
    String HAS_GND = NS_URI +"hasGND";    

    String TITLE = NS_URI + "title";
    String TITLE_SUB = NS_URI + "titleSub";
    String TITLE_PART = NS_URI + "titlePart";
    String TITLE_WHOLE = NS_URI + "titleWhole";
    
    String TYPE_RECORD = NS_URI + "recordType";
    String TYPE_DESCRIPTION = NS_URI + "typeDescription";    
    
    String BOOST = NS_URI + "boost";
    
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
