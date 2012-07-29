/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.io.http.netty;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;
import org.xbib.io.Mode;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class HttpOperationTest {

    private static final Logger logger = LoggerFactory.getLogger(HttpOperationTest.class.getName());
    
    @Test
    public void testGet() throws Exception {
        HttpSession session = new HttpSession();
        session.open(Mode.READ);
        HttpRequest request = new HttpRequest("GET").setURI(URI.create("http://www.google.com/search"))
                .addParameter("q", "köln");
        session.addRequest(request);
        final HttpOperation op = new HttpOperation();
        op.addProcessor(new HttpResultProcessor() {

            @Override
            public void process(HttpResult result) throws IOException {
                logger.info("result = " + result);
            }
            @Override
            public void processError(HttpResult result) throws IOException {
                logger.info("error status = " + result.getStatusCode());
            }

        });
        op.prepareExecution(session).execute(15, TimeUnit.SECONDS);
        session.close();
    }
    
    @Test
    public void testPost() throws Exception {
        HttpSession session = new HttpSession();
        session.open(Mode.READ);
        HttpRequest request = new HttpRequest("POST").setURI(URI.create("http://www.google.com/search"))
                .addHeader("Content-Length", "0")
                .addParameter("q", "köln");        
        session.addRequest(request);
        final HttpOperation op = new HttpOperation();
        op.addProcessor(new HttpResultProcessor() {

            @Override
            public void process(HttpResult result) throws IOException {
                logger.info("result = " + result);
            }
            @Override
            public void processError(HttpResult result) throws IOException {
                logger.info("error status = " + result.getStatusCode());
            }
        });
        op.prepareExecution(session).execute(15, TimeUnit.SECONDS);
        session.close();
    }     
}
