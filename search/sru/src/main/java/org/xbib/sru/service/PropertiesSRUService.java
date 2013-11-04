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
package org.xbib.sru.service;

import org.xbib.io.Session;
import org.xbib.io.http.HttpSession;
import org.xbib.io.http.netty.NettyHttpSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.client.SRUClient;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

/**
 *  A properties-based SRU service
 *
 */
public class PropertiesSRUService implements SRUService {

    private final Logger logger = LoggerFactory.getLogger(PropertiesSRUService.class.getName());

    private Properties properties;

    private final HttpSession session;

    public PropertiesSRUService(Properties properties) throws IOException {
        this.properties = properties;
        this.session = new NettyHttpSession();
        session.open(Session.Mode.READ);
    }
    
    @Override
    public String getRecordSchema() {
        return properties.getProperty(RECORDSCHEMA_PROPERTY, "mods");
    }
    
    @Override
    public String getRecordPacking() {
        return properties.getProperty(RECORDPACKING_PROPERTY, "xml");
    }
    
    @Override
    public String getEncoding() {
        return properties.getProperty(ENCODING_PROPERTY, "UTF-8");
    }

    @Override
    public String getVersion() {
        return properties.getProperty(VERSION_PROPERTY, "1.2");
    }
    
    @Override
    public URI getURI() {
        return URI.create(properties.getProperty(ADDRESS_PROPERTY).trim());
    }

    @Override
    public SRUClient newClient() throws IOException {
        return null;
    }

    /*public void searchRetrieve(SearchRetrieveRequest request, SearchRetrieveListener listener)
            throws IOException {
        HttpRequest req = session.newRequest()
                .setMethod("GET")
                .setURL(request.getURI())
                .addParameter(SRUConstants.OPERATION_PARAMETER, "searchRetrieve")
                .addParameter(SRUConstants.VERSION_PARAMETER, request.getVersion())
                .addParameter(SRUConstants.QUERY_PARAMETER, URIUtil.encode(request.getQuery(), Charset.forName("UTF-8")))
                .addParameter(SRUConstants.START_RECORD_PARAMETER, Integer.toString(request.getStartRecord()))
                .addParameter(SRUConstants.MAXIMUM_RECORDS_PARAMETER, Integer.toString(request.getMaximumRecords()));
        if (request.getRecordPacking() != null && !request.getRecordPacking().isEmpty()) {
            req.addParameter(SRUConstants.RECORD_PACKING_PARAMETER, request.getRecordPacking());
        }
        if (request.getRecordSchema() != null && !request.getRecordSchema().isEmpty()) {
            req.addParameter(SRUConstants.RECORD_SCHEMA_PARAMETER, request.getRecordSchema());
        }
        try {
            req.prepare().execute(listener).waitFor();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        } catch (TimeoutException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }*/

    public void close() throws IOException {
        session.close();
    }

}
