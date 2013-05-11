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
package org.xbib.objectstorage;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.objectstorage.adapter.container.rows.ContainerRow;
import org.xbib.objectstorage.adapter.container.rows.ItemRow;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ObjectStorageResponse implements StreamingOutput {

    private final static Logger logger = LoggerFactory.getLogger(ObjectStorageResponse.class.getName());
    private Response response;
    private ResponseBuilder builder;
    private String textResponse;
    private GenericEntity<List<ContainerRow>> containerResponse;
    private GenericEntity<List<ItemRow>> itemResponse;
    private long t0;
    private long t1;

    public ObjectStorageResponse(ResponseBuilder builder) {
        this.builder = builder;
        t0 = System.currentTimeMillis();
    }

    public ObjectStorageResponse(Response response) {
        this.response = response;

    }

    @Override
    public void write(OutputStream out) throws IOException, WebApplicationException {
    }

    public ResponseBuilder builder() {
        return builder;
    }

    public Response getResponse() {
        if (response != null) {
            return response;
        }
        t1 = System.currentTimeMillis();
        builder.header("X-millis", t1 - t0);
        if (containerResponse != null) {
            logger.info("entity is container response ");
            builder.entity(containerResponse);
        } else if (itemResponse != null) {
            logger.info("entity is item response ");
            builder.entity(itemResponse);
        } else if (textResponse != null) {
            logger.info("entity is text response ");
            builder.entity(textResponse);
        }
        Response r = builder.build();
        containerResponse = null;
        itemResponse = null;
        textResponse = null;
        return r;
    }

    public void setTextResponse(String response) {
        this.textResponse = response;
    }

    public void setContainerResponse(List<ContainerRow> response) {
        this.containerResponse = new GenericEntity<List<ContainerRow>>(response) {

        };
    }

    public void setItemResponse(List<ItemRow> response) {
        this.itemResponse = new GenericEntity<List<ItemRow>>(response) {
        };
    }
}
