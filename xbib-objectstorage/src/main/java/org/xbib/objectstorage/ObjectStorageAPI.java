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
import java.io.InputStream;
import java.security.Principal;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.xbib.io.util.DateUtil;
import org.xbib.util.ExceptionFormatter;

@Path("/v1")
public class ObjectStorageAPI implements ObjectStorageParameter {

    private final static Logger logger = Logger.getLogger(ObjectStorageAPI.class.getName());
    // @todo injection of adapter
    private final static ObjectStorageAdapter adapter = ObjectStorageAdapterService.getInstance().getDefaultAdapter();
    public final static String VERSION = "v1";

    /**
     * Create root
     *
     * @return
     * @throws Exception
     */
    @PUT
    /*
     * @RolesAllowed("UploadGroup")
     */
    public Response putRoot(
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @PathParam(CONTAINER_PARAMETER) String container) throws Exception {
        return Response.serverError().status(Status.BAD_REQUEST).build();
    }

    public Response headRoot() throws Exception {
        return Response.serverError().status(Status.BAD_REQUEST).build();
    }

    /**
     * Post root
     *
     * @return
     * @throws Exception
     */
    @POST
    /*
     * @RolesAllowed("UploadGroup")
     */
    public Response postRoot(
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo) throws Exception {
        return Response.serverError().status(Status.BAD_REQUEST).build();
    }

    /**
     * Get root
     *
     * @return
     * @throws Exception
     */
    @GET
    /*
     * @RolesAllowed({"UploadGroup","DownloadGroup"})
     */
    public Response getRoot(
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo) throws Exception {
        return Response.serverError().status(Status.BAD_REQUEST).build();
    }

    /**
     * Get root
     *
     * @return
     * @throws Exception
     */
    @DELETE
    /*
     * @RolesAllowed("UploadGroup")
     */
    public Response deleteRoot(
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo) throws Exception {
        return Response.serverError().status(Status.BAD_REQUEST).build();
    }

    /**
     * Put container
     *
     * @return
     * @throws Exception
     */
    @PUT
    @Path("/{container}")
    /*
     * @RolesAllowed("UploadGroup")
     */
    public Response putContainer(
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @PathParam(CONTAINER_PARAMETER) String container) throws Exception {
        return Response.serverError().status(Status.BAD_REQUEST).build();
    }

    /**
     * Post container
     *
     * @return
     * @throws Exception
     */
    @POST
    @Path("/{container}")
    /*
     * @RolesAllowed("UploadGroup")
     */
    public Response postContainer(
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @PathParam(CONTAINER_PARAMETER) String container) throws Exception {
        return Response.serverError().status(Status.BAD_REQUEST).build();
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
    /*
     * @RolesAllowed({"UploadGroup","DownloadGroup"})
     */
    public Response getContainer(
            @Context final HttpServletRequest servletRequest,
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @PathParam(CONTAINER_PARAMETER) String container,
            @QueryParam(STATE_PARAMETER) String status,
            @QueryParam(FROM_DATE_PARAMETER) String fromDate,
            @QueryParam(TO_DATE_PARAMETER) String toDate,
            @QueryParam(FROM_PARAMETER) Long from,
            @QueryParam(SIZE_PARAMETER) Long size) {
        if (servletRequest.getMethod().equalsIgnoreCase("head")) {
            return headContainer(servletResponse, securityContext, uriInfo, container);
        }
        ResponseBuilder builder = new ResponseBuilderImpl();
        ObjectStorageResponse response = new ObjectStorageResponse(builder);
        try {
            adapter.connect(uriInfo.getAbsolutePath());
            Principal principal = adapter.getPrincipal(securityContext);
            ObjectStorageRequest request = adapter.newRequest().setUser(principal.getName())
                    .addStringParameter(CONTAINER_PARAMETER, container)
                    .addStringParameter(STATE_PARAMETER, status)
                    .addDateParameter(FROM_DATE_PARAMETER, DateUtil.parseDateISO(fromDate))
                    .addDateParameter(TO_DATE_PARAMETER, DateUtil.parseDateISO(toDate)).addLongParameter(FROM_PARAMETER, from).addLongParameter(SIZE_PARAMETER, size);
            Action action = fromDate != null
                    ? adapter.getContainerGetByDateAction(container)
                    : adapter.getContainerGetAction(container);
            action.execute(request, response);
            action.waitFor(0, TimeUnit.SECONDS); // no-op
            adapter.disconnect();
            return response.getResponse();
        } catch (Exception e) {
            return Response.serverError().entity(ExceptionFormatter.format(e)).build();
        }
    }

    private Response headContainer(
            final HttpServletResponse servletResponse,
            final SecurityContext securityContext,
            final UriInfo uriInfo,
            final String container) {
        logger.log(Level.INFO, "headContainer");
        ResponseBuilder builder = new ResponseBuilderImpl();
        ObjectStorageResponse response = new ObjectStorageResponse(builder);
        try {
            adapter.connect(uriInfo.getAbsolutePath());
            Principal principal = adapter.getPrincipal(securityContext);
            ObjectStorageRequest request = adapter.newRequest().setUser(principal.getName()).addStringParameter(CONTAINER_PARAMETER, container);
            Action action = adapter.getContainerHeadAction(container);
            action.execute(request, response);
            action.waitFor(0, TimeUnit.SECONDS); // no-op
            adapter.disconnect();
            return response.getResponse(); // headers, no body
        } catch (Exception e) {
            return Response.serverError().entity(ExceptionFormatter.format(e)).build();
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
    /*
     * @RolesAllowed("UploadGroup")
     */
    public Response deleteContainer(
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @PathParam(CONTAINER_PARAMETER) String container) throws Exception {
        return Response.serverError().status(Status.BAD_REQUEST).build();
    }

    @GET
    @Path("/{container}/{item}/{state}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    /*
     * @RolesAllowed({"UploadGroup", "DownloadGroup"})
     */
    public Response getItem(
            @Context final HttpServletRequest servletRequest,
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item,
            @PathParam(STATE_PARAMETER) String state) throws Exception {
        return getItem(servletRequest, servletResponse, securityContext,
                uriInfo, container, item);
    }

    @GET
    @Path("/{container}/{item}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    /*
     * @RolesAllowed({"UploadGroup", "DownloadGroup"})
     */
    public Response getItem(
            @Context final HttpServletRequest servletRequest,
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item) {
        logger.log(Level.INFO, "getItem " + uriInfo.getAbsolutePath());
        ResponseBuilder builder = new ResponseBuilderImpl();
        ObjectStorageResponse response = new ObjectStorageResponse(builder);
        try {
            if (servletRequest.getMethod().equalsIgnoreCase("head")) {
                return headItem(servletResponse, securityContext, uriInfo, container, item);
            } else {
                adapter.connect(uriInfo.getAbsolutePath());
                Principal principal = adapter.getPrincipal(securityContext);
                ObjectStorageRequest request = adapter.newRequest().setUser(principal.getName()).setContainer(container).setItem(item);
                Action action = adapter.getItemGetAction(container);
                action.execute(request, response);
                action.waitFor(0, TimeUnit.SECONDS); // no-op
                adapter.disconnect();
                return response.getResponse(); // headers, no body
            }
        } catch (Exception e) {
            return Response.serverError().entity(ExceptionFormatter.format(e)).build();
        }
    }

    private Response headItem(
            final HttpServletResponse servletResponse,
            final SecurityContext securityContext,
            final UriInfo uriInfo,
            String container,
            String item) {
        logger.log(Level.INFO, "headItem " + uriInfo.getAbsolutePath());
        ResponseBuilder builder = new ResponseBuilderImpl();
        ObjectStorageResponse response = new ObjectStorageResponse(builder);
        try {
            adapter.connect(uriInfo.getBaseUri());
            Principal principal = adapter.getPrincipal(securityContext);
            ObjectStorageRequest request = adapter.newRequest().setUser(principal.getName()).setContainer(container).setItem(item);
            Action action = adapter.getItemHeadAction(container);
            action.execute(request, response);
            action.waitFor(0, TimeUnit.SECONDS); //no-op
                adapter.disconnect();
                return response.getResponse(); // headers, no body
        } catch (Exception e) {
            return Response.serverError().entity(ExceptionFormatter.format(e)).build();
        }
    }

    @PUT
    @Path("/{container}/{item}/{state}")
    @Consumes({"application/pdf"})
    /*
     * @RolesAllowed("UploadGroup")
     */
    public Response putItem(
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item,
            @PathParam(STATE_PARAMETER) String state,
            @HeaderParam("Content-Type") String mimeType,
            InputStream in) {
        logger.log(Level.INFO, "putItem " + uriInfo.getAbsolutePath());
        if (mimeType == null) {
            return Response.serverError().status(Status.BAD_REQUEST).build();
        }
        if (!adapter.canUploadTo(mimeType, container)) {
            return Response.serverError().status(Status.BAD_REQUEST).build();
        } else {
            return processUpload(servletResponse, securityContext, uriInfo, container, item, state, mimeType, in);
        }
    }

    @PUT
    @Path("/{container}/{item}/{state}")
    @Consumes({"text/plain"})
    /*
     * @RolesAllowed("UploadGroup")
     */
    public Response putItemState(
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item,
            @PathParam(STATE_PARAMETER) String state,
            @HeaderParam("Content-Type") String mimeType,
            InputStream in) {
        logger.log(Level.INFO, "putItemStatus " + uriInfo.getAbsolutePath());
        if (mimeType == null) {
            return Response.serverError().status(Status.BAD_REQUEST).build();
        }
        if (!adapter.canUploadTo(mimeType, container)) {
            return Response.serverError().status(Status.BAD_REQUEST).build();
        } else {

            return processUpload(servletResponse, securityContext, uriInfo, container, item, state, mimeType, in);
        }
    }

    @POST
    @Path("/{container}/{item}/{status}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postItem(
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item,
            @PathParam(STATE_PARAMETER) String status,
            @FormDataParam("files[]") InputStream in,
            @FormDataParam("files[]") FormDataContentDisposition disp) {
        logger.log(Level.INFO, "postItem " + uriInfo.getAbsolutePath());
        if (disp == null) {
            return Response.serverError().status(Status.BAD_REQUEST).build();
        }
        String mimeType = disp.getType();
        item = disp.getFileName();
        return processUpload(servletResponse, securityContext, uriInfo, container, item, status, mimeType, in);
    }

    @DELETE
    @Path("/{container}/{item}")
    /*
     * @RolesAllowed("UploadGroup")
     */
    public Response deleteItem(
            @Context final HttpServletResponse servletResponse,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @PathParam(CONTAINER_PARAMETER) String container,
            @PathParam(ITEM_PARAMETER) String item) {
        logger.log(Level.INFO, "deleteItem " + uriInfo.getAbsolutePath());
        return Response.serverError().status(Status.BAD_REQUEST).build();
    }

    private Response processUpload(
            final HttpServletResponse servletResponse,
            final SecurityContext securityContext,
            final UriInfo uriInfo,
            String container,
            String item,
            String status,
            String mimeType,
            InputStream in) {
        long t0 = System.currentTimeMillis();
        ResponseBuilder builder = new ResponseBuilderImpl();
        ObjectStorageResponse response = new ObjectStorageResponse(builder);
        try {
            adapter.connect(uriInfo.getBaseUri());
            ItemInfo itemInfo = adapter.newItemInfo(container, item).setInputStream(in);
            if (mimeType != null) {
                itemInfo.setMimeType(mimeType);
            }
            itemInfo.writeToFile(adapter);
            Principal principal = adapter.getPrincipal(securityContext);
            ObjectStorageRequest request = adapter.newRequest().setUser(principal.getName()).setContainer(container).setItem(item).addStringParameter(STATE_PARAMETER, status);
            Action action = adapter.getItemUpdateAction(container);
            action.execute(request, response);
            action.waitFor(0, TimeUnit.SECONDS); // no-op
            action = adapter.getItemJournalAction(container, itemInfo);
            action.execute(request, response);
            action.waitFor(0, TimeUnit.SECONDS); // no-op
            long t1 = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            sb.append("[{\"name\":\"").append(item).append("\"").append(",\"type\":\"").append(itemInfo.getMimeType()).append("\"").append(",\"size\":\"").append(itemInfo.getOctets()).append("\"").append(",\"url\":\"").append(itemInfo.getURL()).append("\"").append(",\"delete_url\":\"").append(itemInfo.getDeleteURL()).append("\"").append(",\"delete_type\":\"DELETE\",\"checksum\":\"").append(itemInfo.getChecksum()).append("\"}]");

            logger.log(Level.INFO, sb.toString());
            adapter.disconnect();
            return builder.status(Status.OK).entity(sb.toString()).header("X-checksum-sha1", itemInfo.getChecksum()).header("X-octets", itemInfo.getOctets()).header("X-mime-type", itemInfo.getMimeType()).header("X-millis", t1 - t0).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return Response.serverError().entity(ExceptionFormatter.format(e)).build();
        }
    }
}
