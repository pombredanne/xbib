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
package org.xbib.federator.action;

import org.xbib.io.iso23950.client.ZClient;
import org.xbib.io.iso23950.searchretrieve.PQFSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveRequest;
import org.xbib.io.iso23950.service.ZService;
import org.xbib.io.iso23950.service.ZServiceFactory;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.iso23950.service.ZSRUService;
import org.xbib.sru.iso23950.service.ZSRUServiceFactory;

import java.io.StringWriter;

public class PQFZAction extends AbstractAction {

    private final Logger logger = LoggerFactory.getLogger(PQFZAction.class.getName());

    @Override
    public Action call() {
        String query = get(params, "query", null);
        if (query == null) {
            logger.warn("query parameter not set, not executing: {}", params);
            return null;
        }
        if (response == null) {
            logger.warn("response not set, not executing: {}", params);
            return null;
        }
        final String name = get(params, "name", "default");
        int from = get(params, "from", 1);
        int size = get(params, "size", 10);
        ZSRUService service = ZSRUServiceFactory.getService(name);
        ZClient client = service.newZClient();
        try {
            PQFSearchRetrieveRequest request = client.newPQFSearchRetrieveRequest();
            //String resultSetName = get(params, "resultSetName", "default");
            //String elementSetName = get(params, "elementSetName", "F");
            //adapter.connect();
            //adapter.setStylesheetTransformer(transformer);
            request.setQuery(query)
                    //.setResultSetName(resultSetName)
                    //.setElementSetName(elementSetName)
                    .setFrom(from)
                    .setSize(size);
            response.setOrigin(service.getURI());
            /*service.setRecordIdentifierSetter(new RecordIdentifierSetter() {

                @Override
                public String setRecordIdentifier(String identifier) {
                    return base + "/" + name + "#" + identifier.trim(); 
                }
            });*/
            //service.searchRetrieve(request, response, from, size, transformer);
            StringWriter sw = new StringWriter();
            service.searchRetrieve(request, sw);
        } catch (Exception e) {
            logger.error(service.getURI().getHost() + " failure: " + e.getMessage(), e);
        }
        this.count = -1; // request.getResultCount();
        return this;
    }
    
}
