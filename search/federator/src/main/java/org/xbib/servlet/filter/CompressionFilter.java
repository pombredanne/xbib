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
package org.xbib.servlet.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.servlet.filter.common.AbstractFilter;
import org.xbib.servlet.filter.common.Constants;
import static org.xbib.servlet.filter.common.Constants.CONTENT_ENCODING_IDENTITY;
import static org.xbib.servlet.filter.common.Constants.DEFAULT_COMPRESSION_SIZE_THRESHOLD;
import static org.xbib.servlet.filter.common.Constants.HTTP_ACCEPT_ENCODING_HEADER;
import static org.xbib.servlet.filter.common.Constants.HTTP_CONTENT_ENCODING_HEADER;
import org.xbib.servlet.filter.compression.CompressedHttpServletRequestWrapper;
import org.xbib.servlet.filter.compression.CompressedHttpServletResponseWrapper;
import org.xbib.servlet.filter.compression.EncodedStreamsFactory;


/**
 * Servlet Filter implementation class CompressionFilter to handle compressed requests
 * and also respond with compressed contents supporting gzip, compress or
 * deflate compression encoding.
 *
 */
public class CompressionFilter extends AbstractFilter {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CompressionFilter.class.getName());

    /**
     * The threshold number of bytes) to compress
     */
    private int compressionThreshold = DEFAULT_COMPRESSION_SIZE_THRESHOLD;

    /**
     * To mark the request that it is processed
     */
    private static final String PROCESSED_ATTR = CompressionFilter.class.getName() + ".PROCESSED";

    /**
     * To mark the request that response compressed
     */
    private static final String COMPRESSED_ATTR = CompressionFilter.class.getName() + ".COMPRESSED";

    /**
     * Threshold
     */
    private static final String INIT_PARAM_COMPRESSION_THRESHOLD = "compressionThreshold";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        int compressionMinSize;
        try {
            compressionMinSize = Integer.parseInt(filterConfig.getInitParameter(INIT_PARAM_COMPRESSION_THRESHOLD));
        } catch (Exception e) {
            compressionMinSize = this.compressionThreshold;
        }
        if (compressionMinSize > 0) { // priority given to configured value
            this.compressionThreshold = compressionMinSize;
        }
        logger.trace("Filter initialized with: {}:{}", new Object[]{
            INIT_PARAM_COMPRESSION_THRESHOLD, String.valueOf(this.compressionThreshold)});
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        ServletRequest req = getRequest(request);
        ServletResponse resp = getResponse(request, response);
        request.setAttribute(PROCESSED_ATTR, Boolean.TRUE);
        chain.doFilter(req, resp);
        if (resp instanceof CompressedHttpServletResponseWrapper) {
            CompressedHttpServletResponseWrapper compressedResponseWrapper = (CompressedHttpServletResponseWrapper) resp;
            try {
                compressedResponseWrapper.close();  //so that stream is finished and closed.
            } catch (IOException ex) {
                logger.error("Response was already closed: ", ex.toString());
            }
            if (compressedResponseWrapper.isCompressed()) {
                req.setAttribute(COMPRESSED_ATTR, Boolean.TRUE);
            }
        }
    }

    private ServletRequest getRequest(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            logger.trace("No Compression: non http request");
            return request;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentEncoding = httpRequest.getHeader(HTTP_CONTENT_ENCODING_HEADER);
        if (contentEncoding == null) {
            logger.trace("No Compression: Request content encoding is: {}", contentEncoding);
            return request;
        }
        if (!EncodedStreamsFactory.isRequestContentEncodingSupported(contentEncoding)) {
            logger.trace("No Compression: unsupported request content encoding: {}", contentEncoding);
            return request;
        }
        logger.debug("Decompressing request: content encoding : {}", contentEncoding);
        return new CompressedHttpServletRequestWrapper(httpRequest, EncodedStreamsFactory.getFactoryForContentEncoding(contentEncoding));
    }

    private String getAppropriateContentEncoding(String acceptEncoding) {
        if (acceptEncoding == null) return null;
        String contentEncoding = null;
        if (CONTENT_ENCODING_IDENTITY.equals(acceptEncoding.trim())) {
            return contentEncoding; //no encoding to be applied
        }
        String[] clientAccepts = acceptEncoding.split(",");
        //!TODO select best encoding (based on q) when multiple encoding are accepted by client
        //@see http://stackoverflow.com/questions/3225136/http-what-is-the-preferred-accept-encoding-for-gzip-deflate
        for (String accepts : clientAccepts) {
            if (CONTENT_ENCODING_IDENTITY.equals(accepts.trim())) {
                return contentEncoding;
            } else if (EncodedStreamsFactory.SUPPORTED_ENCODINGS.containsKey(accepts.trim())) {
                contentEncoding = accepts; //get first matching encoding
                break;
            }
        }
        return contentEncoding;
    }

    private ServletResponse getResponse(ServletRequest request, ServletResponse response) {
        if (response.isCommitted() || request.getAttribute(PROCESSED_ATTR) != null) {
            logger.trace("No Compression: Response committed or filter has already been applied");
            return response;
        }
        if (!(response instanceof HttpServletResponse) || !(request instanceof HttpServletRequest)) {
            logger.trace("No Compression: non http request/response");
            return response;
        }
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String acceptEncoding = httpRequest.getHeader(HTTP_ACCEPT_ENCODING_HEADER);
        String contentEncoding = getAppropriateContentEncoding(acceptEncoding);
        if (contentEncoding == null) {
            logger.trace("No Compression: Accept encoding is : {}", acceptEncoding);
            return response;
        }
        String requestURI = httpRequest.getRequestURI();
        if (!isURLAccepted(requestURI)) {
            logger.trace("No Compression: For path: ", requestURI);
            return response;
        }
        String userAgent = httpRequest.getHeader(Constants.HTTP_USER_AGENT_HEADER);
        if (!isUserAgentAccepted(userAgent)) {
            logger.trace("No Compression: For User-Agent: {}", userAgent);
            return response;
        }
        EncodedStreamsFactory encodedStreamsFactory = EncodedStreamsFactory.getFactoryForContentEncoding(contentEncoding);
        logger.debug("Compressing response: content encoding : {}", contentEncoding);
        return new CompressedHttpServletResponseWrapper(httpResponse, encodedStreamsFactory, contentEncoding, compressionThreshold, this);
    }

}
