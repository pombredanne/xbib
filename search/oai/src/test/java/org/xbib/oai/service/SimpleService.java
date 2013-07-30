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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;

import org.xbib.oai.client.DefaultOAIClient;
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
import org.xbib.oai.service.verb.Identify;
import org.xbib.oai.set.ListSetsResponse;

public class SimpleService implements OAIService {

    private DefaultOAIClient client;

    @Override
    public void identify(IdentifyServerRequest request, IdentifyResponse response)
            throws OAIException {
        new Identify(request, response).execute(this);        
    }

    @Override
    public void listMetadataFormats(ListMetadataFormatsRequest request, ListMetadataFormatsResponse response)
            throws OAIException {
    }

    @Override
    public void listSets(ListSetsRequest request, ListSetsResponse response)
            throws OAIException {
    }

    @Override
    public void listIdentifiers(ListIdentifiersRequest request, ListIdentifiersResponse response)
            throws OAIException {
    }

    @Override
    public void listRecords(ListRecordsServerRequest request, ListRecordsResponse response)
            throws OAIException {
    }

    @Override
    public void getRecord(GetRecordRequest request, GetRecordResponse response)
            throws OAIException {
    }

    @Override
    public URI getServiceIdentifier() {
        return URI.create("http://localhost:8080/oai");
    }

    @Override
    public OAISession newSession() {
        this.client = new DefaultOAIClient()
                .setURL(getServiceIdentifier());
        return client;
    }

    @Override
    public void disposeSession(OAISession session) throws IOException {
        client.close();
    }

    @Override
    public Date getLastModified() {
        return new Date();
    }

    @Override
    public String getRepositoryName() {
        return "Test Repository Name";
    }

    @Override
    public URL getBaseURL() {
        try {
            return getServiceIdentifier().toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    @Override
    public String getProtocolVersion() {
        return "2.0";
    }

    @Override
    public String getAdminEmail() {
        return "joergprante@gmail.com";
    }

    @Override
    public String getEarliestDatestamp() {
        return "2012-01-01T00:00:00Z";
    }

    @Override
    public String getDeletedRecord() {
        return "no";
    }

    @Override
    public String getGranularity() {
        return "YYYY-MM-DDThh:mm:ssZ";
    }
        
}
