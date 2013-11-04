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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.container.ContainerRequest;
import org.xbib.date.DateUtil;
import org.xbib.jersey.filter.PasswordSecurityContext;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.util.ExceptionFormatter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.concurrent.TimeUnit;

@Path("/v1")
public class API implements Parameter {

    private final static Logger logger = LoggerFactory.getLogger(API.class.getName());

    public final static String VERSION = "v1";

    private final static Adapter adapter =
            AdapterService.getInstance()
                    .getAdapter(URI.create("http://xbib.org/objectstorage/remotefilesystem"));

    @Context HttpServletRequest servletRequest;
    @Context HttpServletResponse servletResponse;
    @Context SecurityContext securityContext;
    @Context UriInfo uriInfo;

    /**
     * Create root
     *
     * @return
     * @throws Exception
     */
    @PUT
    public javax.ws.rs.core.Response putRoot(
            @PathParam(CONTAINER_PARAMETER) String container) throws Exception {
        return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
    }

    public javax.ws.rs.core.Response headRoot() throws Exception {
        return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
    }

    /**
     * Post root
     *
     * @return
     * @throws Exception
     */
    @POST
    public javax.ws.rs.core.Response postRoot() throws Exception {
        return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
    }

    /**
     * Get root
     *
     * @return
     * @throws Exception
     */
    @GET
    public javax.ws.rs.core.Response getRoot() throws Exception {
        return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
    }

    /**
     * Delete root
     *
     * @return
     * @throws Exception
     */
    @DELETE
    public javax.ws.rs.core.Response deleteRoot() throws Exception {
        return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
    }

    /**
     * Put container
     *
     * @return
     * @throws Exception
     */
    @PUT
    @Path("/{container}")
    public javax.ws.rs.core.Response putContainer(@PathParam(CONTAINER_PARAMETER) String container) throws Exception {
        return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
    }

    /**
     * Post container
     *
     * @return
     * @throws Exception
     */
    @POST
    @Path("/{container}")
    public javax.ws.rs.core.Response postContainer(
            @PathParam(CONTAINER_PARAMETER) String container) throws Exception {
        return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
    }

    /**
     * Get container items
     *
     * @return
     * @throws Exception
     */
    @GET
    @Path("/{container}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public javax.ws.rs.core.Response getContainer(
            @PathParam(CONTAINER_PARAMETER) String container,
            @QueryParam(STATE_PARAMETER) String status,
            @QueryParam(FROM_DATE_PARAMETER) String fromDate,
            @QueryParam(TO_DATE_PARAMETER) String toDate,
            @QueryParam(FROM_PARAMETER) Long from,
            @QueryParam(SIZE_PARAMETER) Long size) {
        if ("head".equalsIgnoreCase(servletRequest.getMethod())) {
            return headContainer(servletResponse, securityContext, uriInfo, container);
        }
        logger.debug("getContainer(): {} 1={} 2={}", uriInfo.getAbsolutePath(), securityContext);
        ResponseBuilder builder = new ResponseBuilderImpl();
        Response response = new Response(builder);
        try {
            Container cnt = adapter.connect(container, getPasswordSecurityContext(securityContext),
                    uriInfo.getAbsolutePath());
            Principal principal = cnt.getPrincipal(securityContext);
            Request request = cnt.newRequest()
                    .setUser(principal.getName())
                    .addStringParameter(CONTAINER_PARAMETER, container)
                    .addStringParameter(STATE_PARAMETER, status)
                    .addDateParameter(FROM_DATE_PARAMETER, DateUtil.parseDateISO(fromDate, DateUtil.min()))
                    .addDateParameter(TO_DATE_PARAMETER, DateUtil.parseDateISO(toDate, DateUtil.now()))
                    .addLongParameter(FROM_PARAMETER, from)
                    .addLongParameter(SIZE_PARAMETER, size);
            logger.debug("getContainer(): principal={} request={}",
                    principal.getName(),
                    request.toString());
            Action action = cnt.getContainerGetAction();
            action.execute(request, response);
            action.waitFor(0, TimeUnit.SECONDS); // no-op
            adapter.disconnect(cnt);
            return response.getResponse();
        } catch (Exception e) {
            return javax.ws.rs.core.Response.serverError()
                    .type(MediaType.APPLICATION_XML)
                    .entity(ExceptionFormatter.toPlainText(e))
                    .build();
        }
    }

    private javax.ws.rs.core.Response headContainer(
            final HttpServletResponse servletResponse,
            final SecurityContext securityContext,
            final UriInfo uriInfo,
            final String container) {
        logger.debug("headContainer() " + uriInfo.getAbsolutePath());
        ResponseBuilder builder = new ResponseBuilderImpl();
        Response response = new Response(builder);
        try {
            Container cnt = adapter.connect(container,
                    getPasswordSecurityContext(securityContext), uriInfo.getAbsolutePath());
            Principal principal = cnt.getPrincipal(securityContext);
            Request request = cnt.newRequest()
                    .setUser(principal.getName())
                    .addStringParameter(CONTAINER_PARAMETER, container);
            logger.debug("headContainer(): principal={} request={}",
                    principal.getName(),
                    request.toString());
            Action action = cnt.getContainerHeadAction();
            action.execute(request, response);
            action.waitFor(0, TimeUnit.SECONDS); // no-op
            adapter.disconnect(cnt);
            return response.getResponse(); // headers, no body
        } catch (Exception e) {
            return javax.ws.rs.core.Response.serverError()
                    .type(MediaType.APPLICATION_XML)
                    .entity(ExceptionFormatter.toPlainText(e))
                    .build();
        }
    }

    /**
     * Delete container
     *
     * @return
     * @throws Exception
     */
    @DELETE
    @Path("/{container}")
    public javax.ws.rs.core.Response deleteContainer(
            @PathParam(CONTAINER_PARAMETER) String container) throws Exception {
        return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
    }

    @GET
    @Path("/{container}/{item}/{state}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public javax.ws.rs.core.Response getItem(
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item,
            @PathParam(STATE_PARAMETER) String state) throws Exception {
        return getItem(container, item);
    }

    @GET
    @Path("/{container}/{item}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public javax.ws.rs.core.Response getItem(
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item) {
        logger.debug("getItem() {}", uriInfo.getAbsolutePath());
        ResponseBuilder builder = new ResponseBuilderImpl();
        Response response = new Response(builder);
        try {
            if (servletRequest.getMethod().equalsIgnoreCase("head")) {
                return headItem(servletResponse, securityContext, uriInfo, container, item);
            } else {
                Container cnt = adapter.connect(container,
                        getPasswordSecurityContext(securityContext), uriInfo.getAbsolutePath());
                Principal principal = cnt.getPrincipal(securityContext);
                Request request = cnt.newRequest()
                        .setUser(principal.getName())
                        .setContainer(container)
                        .setItem(item);
                logger.debug("getItem(): principal={} request={}",
                        principal.getName(),
                        request.toString());
                Action action = cnt.getItemGetAction();
                action.execute(request, response);
                action.waitFor(0, TimeUnit.SECONDS); // no-op
                adapter.disconnect(cnt);
                return response.getResponse(); // headers, no body
            }
        } catch (Exception e) {
            return javax.ws.rs.core.Response.serverError()
                    .type(MediaType.APPLICATION_XML)
                    .entity(ExceptionFormatter.toPlainText(e))
                    .build();
        }
    }

    private javax.ws.rs.core.Response headItem(
            final HttpServletResponse servletResponse,
            final SecurityContext securityContext,
            final UriInfo uriInfo,
            String container,
            String item) {
        logger.debug("headItem() {}", uriInfo.getAbsolutePath());
        ResponseBuilder builder = new ResponseBuilderImpl();
        Response response = new Response(builder);
        try {
            Container cnt = adapter.connect(container,
                    getPasswordSecurityContext(securityContext), uriInfo.getAbsolutePath());
            Principal principal = cnt.getPrincipal(securityContext);
            Request request = cnt.newRequest()
                    .setUser(principal.getName())
                    .setContainer(container)
                    .setItem(item);
            logger.debug("headItem(): principal={} request={}",
                    principal.getName(),
                    request.toString());
            Action action = cnt.getItemHeadAction();
            action.execute(request, response);
            action.waitFor(0, TimeUnit.SECONDS); //no-op
            adapter.disconnect(cnt);
            return response.getResponse(); // headers, no body
        } catch (Exception e) {
            return javax.ws.rs.core.Response.serverError()
                    .type(MediaType.APPLICATION_XML)
                    .entity(ExceptionFormatter.toPlainText(e))
                    .build();
        }
    }

    @PUT
    @Path("/{container}/{item}/{state}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({"application/pdf"})
    public javax.ws.rs.core.Response putItem(
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item,
            @PathParam(STATE_PARAMETER) String state,
            @HeaderParam("Content-Type") String mimeType,
            InputStream in) throws IOException {
        logger.debug("putItem(): {}", uriInfo.getAbsolutePath());
        if (mimeType == null) {
            return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
        }
        return processUpload(container, item, state, mimeType, in);
    }

    @PUT
    @Path("/{container}/{item}/{state}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({"text/plain"})
    public javax.ws.rs.core.Response putItemState(
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item,
            @PathParam(STATE_PARAMETER) String state,
            @HeaderParam("Content-Type") String mimeType,
            InputStream in) throws IOException {
        logger.debug("putItemStatus() {}", uriInfo.getAbsolutePath());
        if (mimeType == null) {
            return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
        }
        return processUpload(container, item, state, mimeType, in);
    }

    @POST
    @Path("/{container}/{item}/{status}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response postItem(
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item,
            @PathParam(STATE_PARAMETER) String status,
            @FormDataParam("file") InputStream in,
            @FormDataParam("file") FormDataContentDisposition disp) throws IOException {
        logger.debug("postItem {}", uriInfo.getAbsolutePath());
        if (disp == null) {
            return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
        }
        String mimeType = disp.getType();
        item = disp.getFileName();
        return processUpload(container, item, status, mimeType, in);
    }

    @DELETE
    @Path("/{container}/{item}")
    public javax.ws.rs.core.Response deleteItem(
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item) {
        logger.debug("deleteItem {}", uriInfo.getAbsolutePath());
        return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
    }

    private javax.ws.rs.core.Response processUpload(String container,
            String item,
            String status,
            String mimeType,
            InputStream in) throws IOException {
        long t0 = System.currentTimeMillis();
        ResponseBuilder builder = new ResponseBuilderImpl();
        Response response = new Response(builder);
        try {
            Container cnt = adapter.connect(container,
                    getPasswordSecurityContext(securityContext), uriInfo.getAbsolutePath());
            if (!cnt.canUpload(mimeType)) {
                return javax.ws.rs.core.Response.serverError().status(Status.BAD_REQUEST).build();
            }
            ItemInfo itemInfo = new ItemInfo(cnt, item).setInputStream(in);
            if (mimeType != null) {
                itemInfo.setMimeType(mimeType);
            }

            cnt.upload(itemInfo);

            Principal principal = cnt.getPrincipal(securityContext);

            Request request = cnt.newRequest()
                    .setUser(principal.getName())
                    .setContainer(container)
                    .setItem(item)
                    .addStringParameter(STATE_PARAMETER, status);
            Action action = cnt.getItemUpdateAction();
            action.execute(request, response);
            action.waitFor(0, TimeUnit.SECONDS); // no-op
            action = cnt.getItemJournalAction(itemInfo);
            action.execute(request, response);
            action.waitFor(0, TimeUnit.SECONDS); // no-op
            long t1 = System.currentTimeMillis();
            adapter.disconnect(cnt);
            javax.ws.rs.core.Response r = builder.status(Status.OK)
                    .entity(itemInfo.entity())
                    .header("X-checksum-sha1", itemInfo.getChecksum())
                    .header("X-octets", itemInfo.getOctets())
                    .header("X-mime-type", itemInfo.getMimeType())
                    .header("X-millis", t1 - t0).build();
            logger.debug("response = {}", r.toString());
            return r;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return javax.ws.rs.core.Response.serverError()
                    .type(MediaType.APPLICATION_XML)
                    .entity(ExceptionFormatter.toPlainText(e))
                    .build();
        }
    }

    private PasswordSecurityContext getPasswordSecurityContext(SecurityContext securityContext) {
        // I hate jersey
        if (securityContext instanceof ContainerRequest) {
            ContainerRequest cr = (ContainerRequest)securityContext;
            SecurityContext sc = cr.getSecurityContext();
            if (sc instanceof PasswordSecurityContext) {
                return (PasswordSecurityContext)sc;
            }
        }
        return null;
    }
}
