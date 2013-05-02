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
package org.xbib.oai.client;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.xbib.io.http.netty.HttpResponse;
import org.xbib.io.http.netty.HttpResponseListener;
import org.xbib.oai.GetRecordRequest;
import org.xbib.oai.IdentifyRequest;
import org.xbib.oai.IdentifyResponse;
import org.xbib.oai.ListIdentifiersRequest;
import org.xbib.oai.ListMetadataFormatsRequest;
import org.xbib.oai.ListRecordsRequest;
import org.xbib.oai.ListRecordsResponse;
import org.xbib.oai.ListSetsRequest;
import org.xbib.oai.ListSetsResponse;
import org.xbib.oai.MetadataReader;
import org.xbib.oai.OAIResponse;
import org.xbib.oai.exceptions.OAIException;
import org.xbib.xml.transform.StylesheetTransformer;

public interface OAIClient {
    
    String USER_AGENT = "OAI/20130502";
    
    OAIClient setURI(URI uri);
    
    URI getURI();
    
    OAIClient setProxy(String host, int port);

    OAIClient setTimeout(long timeout);

    OAIClient setStylesheetTransformer(StylesheetTransformer transformer);
    
    OAIClient setMetadataReader(MetadataReader reader);

    /**
     * This verb is used to retrieve information about a repository. 
     * Some of the information returned is required as part of the OAI-PMH.
     * Repositories may also employ the Identify verb to return additional 
     * descriptive information.
     * @param request
     * @throws OAIException 
     */
    OAIClient prepareIdentify(IdentifyRequest request, IdentifyResponse response)
            throws IOException;
    
    /**
     * This verb is an abbreviated form of ListRecords, retrieving only 
     * headers rather than records. Optional arguments permit selective 
     * harvesting of headers based on set membership and/or datestamp. 
     * Depending on the repository's support for deletions, a returned 
     * header may have a status attribute of "deleted" if a record 
     * matching the arguments specified in the request has been deleted.
     * 
     * @param response
     * @throws OAIException 
     */
    OAIClient prepareListIdentifiers(ListIdentifiersRequest request, OAIResponse response) 
            throws IOException;

    /**
     * This verb is used to retrieve the metadata formats available 
     * from a repository. An optional argument restricts the request 
     * to the formats available for a specific item.
     * @param response
     * @throws OAIException 
     */
    OAIClient prepareListMetadataFormats(ListMetadataFormatsRequest request, OAIResponse response) 
            throws IOException;
    
    /**
     * This verb is used to retrieve the set structure of a repository, 
     * useful for selective harvesting.
     * @param response
     * @throws OAIException 
     */
    OAIClient prepareListSets(ListSetsRequest request, ListSetsResponse response) 
            throws IOException;

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
    OAIClient prepareListRecords(ListRecordsRequest request, ListRecordsResponse response) 
            throws IOException;
    
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
    OAIClient prepareGetRecord(GetRecordRequest request, OAIResponse response) 
            throws IOException;

    void execute() throws IOException;

    void execute(long l, TimeUnit tu) throws IOException;

    HttpResponse getResponse();
}
