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
package org.xbib.io.iso23950.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.xbib.io.ConnectionManager;
import org.xbib.io.ErrorResultProcessor;
import org.xbib.io.ResultProcessor;
import org.xbib.io.iso23950.AbstractSearchRetrieve;
import org.xbib.io.iso23950.Diagnostics;
import org.xbib.io.iso23950.InitOperation;
import org.xbib.io.iso23950.Record;
import org.xbib.io.iso23950.ZAdapter;
import org.xbib.io.iso23950.ZConnection;
import org.xbib.io.iso23950.ZSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Iso2709Reader;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public abstract class ZBaseAdapter implements ZAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ZBaseAdapter.class.getName());
    private ZConnection connection;
    private ZSession session;
    private boolean connected;
    private boolean authenticated;
    private StylesheetTransformer transformer;

    @Override
    public void connect() {
        try {
            connectInternal();
        } catch (IOException ex) {
            logger.error(getURI().getHost() + ": " + ex.getMessage(), ex);
            disconnectInternal();
        }
    }

    @Override
    public void disconnect() {
        disconnectInternal();
    }

    @Override
    public void setStylesheetTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Search/retrieve and return the raw records in bytes. No response is
     * generated. Useful for chaining several retrievals into one for a single
     * response.
     *
     * @param request
     * @return raw record byte array
     * @throws Diagnostics
     * @throws IOException
     */
    @Override
    public void searchRetrieve(AbstractSearchRetrieve request, OutputStream records, OutputStream errors)
            throws Diagnostics, IOException {
        try {
            connectInternal();
            if (!authenticated) {
                throw new Diagnostics(2, "authentication failed, check adapter URI for username/password");
            }
            final ResponseHelper helper = new ResponseHelper(records, errors);
            request.setHandler(helper);
            request.setErrorHandler(helper);
            request.setTimeout(getTimeout());
            // execute search
            request.execute(session);
        } finally {
            disconnectInternal();
        }
    }

    /**
     * Search/retrieve with output to a response
     *
     * @param request
     * @param response
     * @throws Diagnostics
     * @throws IOException
     */
    @Override
    public void searchRetrieve(AbstractSearchRetrieve request, SearchRetrieveResponse response)
            throws Diagnostics, IOException {
        searchRetrieve(request, response, null);
    }

    @Override
    public void searchRetrieve(AbstractSearchRetrieve request, SearchRetrieveResponse response, List<XMLEvent> list)
            throws Diagnostics, IOException {
        if (transformer == null) {
            throw new Diagnostics(1, "no stylesheet transformer installed");
        }
        try {
            connectInternal();
            if (!authenticated) {
                throw new Diagnostics(2, "authentication failed, check adapter URI for username/password");
            }
            ByteArrayOutputStream records = new ByteArrayOutputStream();
            ByteArrayOutputStream errors = new ByteArrayOutputStream();
            final ResponseHelper r = new ResponseHelper(records, errors);
            request.setHandler(r);
            request.setErrorHandler(r);
            request.setTimeout(getTimeout());
            // execute search
            request.execute(session);
            // get result count for caller and for stylesheet
            response.numberOfRecords(request.getResultCount());
            transformer.addParameter("numberOfRecords", request.getResultCount());
            // push out results
            ByteArrayInputStream in = new ByteArrayInputStream(records.toByteArray());
            InputSource source = new InputSource(new InputStreamReader(in, getEncoding()));
            SRUFilterReader reader = new SRUFilterReader(response, list);
            reader.setProperty(Iso2709Reader.FORMAT, getFormat());
            reader.setProperty(Iso2709Reader.TYPE, getType());
            transformer.setSource(new SAXSource(reader, source)).setTarget(
                    response.getOutput() != null
                    ? new StreamResult(response.getOutput())
                    : new StreamResult(response.getWriter())).apply();
        } catch (SAXNotSupportedException | SAXNotRecognizedException | TransformerException ex) {
            logger.error(getURI().getHost() + ": " + ex.getMessage(), ex);
        } finally {
            disconnectInternal();
        }
    }

    protected void connectInternal() throws IOException {
        if (connected) {
            return;
        }
        this.connection = (ZConnection) ConnectionManager.getConnection(getURI());
        this.session = connection.createSession();
        this.connected = true;
        this.authenticated = authenticate(session);
    }

    protected void disconnectInternal() {
        if (!connected) {
            return;
        }
        authenticated = false;
        connected = false;
        if (session != null) {
            session.close();
        }
        session = null;
        if (connection != null) {
            connection.close();
        }
        connection = null;
    }

    protected boolean authenticate(ZSession session) throws IOException {
        if (session == null) {
            throw new IOException("no session");
        }
        String username = null;
        String password = null;
        String userinfo = getURI().getUserInfo();
        if (userinfo != null) {
            String[] userpass = userinfo.split(":");
            if (userpass.length > 0) {
                username = userpass[0];
                password = "";
                if (userpass.length > 1) {
                    password = userpass[1];
                }
            }
        }
        InitOperation init = new InitOperation(username, password, null);
        init.execute(session);
        session.setAuthenticated(!init.rejected());
        return session.authenticated();
    }

    protected abstract String getEncoding();

    protected abstract String getStylesheet();

    protected abstract String getFormat();

    protected abstract String getType();

    protected abstract int getTimeout();

    class ResponseHelper implements ResultProcessor<Record>, ErrorResultProcessor<Record> {

        private OutputStream records;
        private OutputStream errors;

        ResponseHelper(OutputStream records, OutputStream errors) {
            this.records = records;
            this.errors = errors;
        }

        @Override
        public void process(Record result) throws IOException {
            if (result.getContent() == null) {
                throw new IOException("no result");
            }
            records.write(result.getContent());
        }

        @Override
        public void processError(Record result) throws IOException {
            if (result.getContent() == null) {
                throw new IOException("no result");
            }
            errors.write(result.getContent());
        }
    }
}
