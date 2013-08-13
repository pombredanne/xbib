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
package org.xbib.oai.service;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Date;

import org.xbib.oai.OAISession;
import org.xbib.oai.identify.ListIdentifiersResponse;
import org.xbib.oai.metadata.ListMetadataFormatsResponse;
import org.xbib.oai.record.GetRecordRequest;
import org.xbib.oai.identify.IdentifyResponse;
import org.xbib.oai.identify.ListIdentifiersRequest;
import org.xbib.oai.metadata.ListMetadataFormatsRequest;
import org.xbib.oai.record.GetRecordResponse;
import org.xbib.oai.record.ListRecordsResponse;
import org.xbib.oai.identify.IdentifyServerRequest;
import org.xbib.oai.record.ListRecordsServerRequest;
import org.xbib.oai.set.ListSetsRequest;
import org.xbib.oai.exceptions.OAIException;
import org.xbib.oai.set.ListSetsResponse;

/**
 *  OAI service
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public interface OAIService {

    URI getURI();

    OAISession newSession();

    /**
     * This verb is used to retrieve information about a repository. 
     * Some of the information returned is required as part of the OAI-PMH.
     * Repositories may also employ the Identify verb to return additional 
     * descriptive information.
     * @param request
     * @param response
     * @throws OAIException 
     */
    void identify(IdentifyServerRequest request, IdentifyResponse response) throws OAIException;
    
    /**
     * This verb is an abbreviated form of ListRecords, retrieving only 
     * headers rather than records. Optional arguments permit selective 
     * harvesting of headers based on set membership and/or datestamp. 
     * Depending on the repository's support for deletions, a returned 
     * header may have a status attribute of "deleted" if a record 
     * matching the arguments specified in the request has been deleted.
     * @param request
     * @param response
     * @throws OAIException 
     */
    void listIdentifiers(ListIdentifiersRequest request, ListIdentifiersResponse response) throws OAIException;

    /**
     * This verb is used to retrieve the metadata formats available 
     * from a repository. An optional argument restricts the request 
     * to the formats available for a specific item.
     * @param request
     * @param response
     * @throws OAIException 
     */
    void listMetadataFormats(ListMetadataFormatsRequest request, ListMetadataFormatsResponse response) throws OAIException;
    
    /**
     * This verb is used to retrieve the set structure of a repository, 
     * useful for selective harvesting.
     * @param request
     * @param response
     * @throws OAIException 
     */
    void listSets(ListSetsRequest request, ListSetsResponse response) throws OAIException;

    /**
     * This verb is used to harvest records from a repository. 
     * Optional arguments permit selective harvesting of records based on 
     * set membership and/or datestamp. Depending on the repository's 
     * support for deletions, a returned header may have a status 
     * attribute of "deleted" if a record matching the arguments 
     * specified in the request has been deleted. No metadata 
     * will be present for records with deleted status.
     * @param request
     * @param response
     * @throws OAIException 
     */
    void listRecords(ListRecordsServerRequest request, ListRecordsResponse response) throws OAIException;
    
    /**
     * This verb is used to retrieve an individual metadata record from 
     * a repository. Required arguments specify the identifier of the item 
     * from which the record is requested and the format of the metadata 
     * that should be included in the record. Depending on the level at 
     * which a repository tracks deletions, a header with a "deleted" value 
     * for the status attribute may be returned, in case the metadata format 
     * specified by the metadataPrefix is no longer available from the 
     * repository or from the specified item.
     * @param request
     * @param response
     * @throws OAIException 
     */
    void getRecord(GetRecordRequest request, GetRecordResponse response) throws OAIException;

    Date getLastModified();
    
    String getRepositoryName();
    
    URL getBaseURL();

    String getProtocolVersion();

    String getAdminEmail();
    
    String getEarliestDatestamp();
    
    String getDeletedRecord();
    
    String getGranularity();
    
}
