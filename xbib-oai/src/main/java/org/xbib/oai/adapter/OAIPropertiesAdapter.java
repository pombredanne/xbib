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
package org.xbib.oai.adapter;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xbib.oai.GetRecordRequest;
import org.xbib.oai.IdentifyRequest;
import org.xbib.oai.IdentifyResponse;
import org.xbib.oai.ListIdentifiersRequest;
import org.xbib.oai.ListMetadataFormatsRequest;
import org.xbib.oai.ListRecordsRequest;
import org.xbib.oai.ListSetsRequest;
import org.xbib.oai.OAIResponse;
import org.xbib.oai.OAIServerRequest;
import org.xbib.oai.exceptions.OAIException;
import org.xbib.xml.transform.StylesheetTransformer;

public class OAIPropertiesAdapter implements OAIAdapter {
    private static final Logger logger = Logger.getLogger(OAIPropertiesAdapter.class.getName());
    public final static String ADAPTER_URI = "uri";
    public final static String STYLESHEET = "stylesheet";
    public final static String REPOSITORY_NAME = "identify.repositoryName";
    public final static String BASE_URL = "identify.baseURL";
    public final static String PROTOCOL_VERSION = "identify.protocolVersion";
    public final static String ADMIN_EMAIL = "identify.adminEmail";
    public final static String EARLIEST_DATESTAMP = "identify.earliestDatestamp";
    public final static String DELETED_RECORD = "identify.deletedRecord";
    public final static String GRANULARITY = "identify.granularity";
    
    private Properties properties;

    public OAIPropertiesAdapter(Properties properties) {
        this.properties = properties;
    }
    
    public Properties getProperties() {
        return properties;
    }

    @Override
    public URI getURI() {
        return URI.create(properties.getProperty(ADAPTER_URI).trim());
    }
    
    @Override
    public String getStylesheet() {
        return properties.getProperty(STYLESHEET);
    }
    
    @Override
    public String getRepositoryName() {
        return properties.getProperty(REPOSITORY_NAME);
    }
    
    @Override
    public URL getBaseURL() {
        try {
            return new URL(properties.getProperty(BASE_URL));
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getProtocolVersion() {
        return properties.getProperty(PROTOCOL_VERSION);
    }

    @Override
    public String getAdminEmail() {
        return properties.getProperty(ADMIN_EMAIL);
    }
    
    @Override
    public String getEarliestDatestamp() {
        return properties.getProperty(EARLIEST_DATESTAMP);
    }
    
    @Override
    public String getDeletedRecord() {
        return properties.getProperty(DELETED_RECORD);
    }
    
    @Override
    public String getGranularity() {
        return properties.getProperty(GRANULARITY);
    }
    
    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public Date getLastModified() {
        return null;
    }

    @Override
    public void setStylesheetTransformer(StylesheetTransformer transformer) {
    }

    @Override
    public void identify(IdentifyRequest request, IdentifyResponse response) 
            throws OAIException {
    }

    @Override
    public void listMetadataFormats(ListMetadataFormatsRequest request, OAIResponse response) 
            throws OAIException {
    }

    @Override
    public void listSets(ListSetsRequest request, OAIResponse response) 
            throws OAIException {
    }

    @Override
    public void listIdentifiers(ListIdentifiersRequest request, OAIResponse response) 
            throws OAIException {
    }

    @Override
    public void listRecords(ListRecordsRequest request, OAIResponse response) 
            throws OAIException {
    }

    @Override
    public void getRecord(GetRecordRequest request, OAIResponse response) 
            throws OAIException {
    }
    
}
