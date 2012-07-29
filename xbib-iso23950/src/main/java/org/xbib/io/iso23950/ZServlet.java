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
package org.xbib.io.iso23950;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xbib.io.negotiate.ContentTypeNegotiator;
import org.xbib.io.negotiate.MediaRangeSpec;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;

public class ZServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ZServlet.class.getName());
    private final Map<String, String> mediaTypes = new HashMap<>();
    private final ContentTypeNegotiator ctn = new ZContentTypeNegotiator();
    private final StylesheetTransformer transformer = new StylesheetTransformer("/xsl");
    private final String responseEncoding = "UTF-8";
    private ZAdapter adapter;
    private Logger zLogger;
    private ZRequestDumper requestDumper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String adapterName = config.getInitParameter("name");
        this.adapter = ZAdapterFactory.getAdapter(adapterName);
        this.requestDumper = new ZRequestDumper();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaType = getMediaType(request);
        response.setContentType(mediaType);
        response.setHeader("Server", "Java");
        response.setHeader("X-Powered-By", getClass().getName());
        final OutputStream out = response.getOutputStream();
        if (adapter == null) {
            adapter = createAdapter(request);
        }
        // zLogger is present after adapter creation
        if (zLogger != null && requestDumper != null) {
            zLogger.info(requestDumper.toString(request));
        }
        try {
            adapter.connect();
            adapter.setStylesheetTransformer(transformer);
            String resultSetName = request.getParameter("resultSetName") != null
                    ? request.getParameter("resultSetName") : "default";
            String query = request.getParameter("query");
            String elementSetName = request.getParameter("elementSetName") != null
                    ? request.getParameter("elementSetName") : "F";
            int from = Integer.parseInt(
                    request.getParameter("from") != null
                    ? request.getParameter("from") : "1");
            int size = Integer.parseInt(
                    request.getParameter("size") != null
                    ? request.getParameter("size") : "10");
            AbstractSearchRetrieve op = new CQLSearchRetrieve();
            op.setDatabase(adapter.getDatabases()).setQuery(query).setResultSetName(resultSetName).setElementSetName(elementSetName).setPreferredRecordSyntax(adapter.getPreferredRecordSyntax()).setFrom(from).setSize(size);
            adapter.searchRetrieve(op, new SearchRetrieveResponse(response, responseEncoding));
        } finally {
            out.flush();
            adapter.disconnect();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void destroy() {
        super.destroy();
        adapter.disconnect();
    }

    private String getMediaType(HttpServletRequest req) {
        String useragent = req.getHeader("User-Agent");
        String mediaType, mimeType = req.getParameter("http:accept");
        if (mimeType == null) {
            mimeType = req.getParameter("httpAccept");
        }
        if (mimeType == null) {
            mimeType = req.getHeader("accept");
        }
        mediaType = mediaTypes.get(mimeType);
        if (mediaType == null) {
            MediaRangeSpec mrs = useragent != null
                    ? ctn.getBestMatch(mimeType, useragent) : ctn.getBestMatch(mimeType);
            if (mrs != null) {
                mediaType = mrs.getMediaType();
            } else {
                mediaType = "";
            }
            mediaTypes.put(mimeType, mediaType);
        }
        logger.trace("mimeType = {} -> mediaType = {}", mimeType, mediaType);
        return mediaType;
    }

    private ZAdapter createAdapter(HttpServletRequest request) throws ServletException, IOException {
        String[] reqPath = request.getRequestURI().split("/");
        String name = reqPath[reqPath.length - 1];
        this.adapter = ZAdapterFactory.getAdapter(name);
        if (adapter == null) {
            throw new ServletException("can't get adapter from " + request.getRequestURI());
        }
        this.zLogger = createLogger(name);
        return adapter;
    }

    private Logger createLogger(String name) throws IOException {
        Logger cLogger = LoggerFactory.getLogger("org.xbib.io.z3950.logger");
        /*String directory = System.getProperty("org.xbib.io.z3950.logging.directory", "logs");
        String prefix = System.getProperty("org.xbib.io.z3950.logging.prefix", "z3950-request-" + name + ".");
        String suffix = System.getProperty("org.xbib.io.z3950.logging.suffix", ".log");
        CustomFileHandler handler = new CustomFileHandler(directory, prefix, suffix);
        handler.setFormatter(new CustomFormatter());
        cLogger.addHandler(handler);*/
        return cLogger;
    }
}
