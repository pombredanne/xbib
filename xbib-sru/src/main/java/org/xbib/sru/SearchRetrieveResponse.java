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
package org.xbib.sru;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.events.XMLEvent;
import org.xbib.io.AbstractResponse;
import org.xbib.io.Request;

public class SearchRetrieveResponse extends AbstractResponse
        implements SRUResponseListener {

    private HttpServletResponse response;
    private SRUResponseListener listener;
    private URI origin;
    private Collection<XMLEvent> events;

    public SearchRetrieveResponse(Writer writer) {
        super(writer);
    }

    public SearchRetrieveResponse(HttpServletResponse response, String encoding)
            throws UnsupportedEncodingException, IOException {
        this(response.getOutputStream(), encoding);
        this.response = response;
    }

    public SearchRetrieveResponse(OutputStream out, String encoding)
            throws UnsupportedEncodingException {
        super(out, encoding);
    }
    
    @Override
    public void write() throws IOException {
    }

    public SearchRetrieveResponse addResponseParameter(String key, String value) {
        if (response != null) {
            response.addHeader(key, value);
        }
        return this;
    }

    public void setListener(SRUResponseListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onConnect(Request request) {
        if (listener != null) {
            listener.onConnect(request);
        }
    }

    @Override
    public void onDisconnect(Request request) {
        if (listener != null) {
            listener.onDisconnect(request);
        }
    }

    @Override
    public void onError(Request request, int count, byte[] errorMessage) {
        if (listener != null) {
            listener.onError(request, count, errorMessage);
        }
    }

    @Override
    public void onReceive(Request request, int count, byte[] message) {
        // ignore
    }


    @Override
    public void version(String version) {
        if (listener != null) {
            listener.version(version);
        }
    }

    @Override
    public void numberOfRecords(long numberOfRecords) {
        if (listener != null) {
            listener.numberOfRecords(numberOfRecords);
        }
    }
    
    @Override
    public void beginRecord() {
        if (listener != null) {
            listener.beginRecord();
        }
    }

    @Override
    public void recordMetadata(String recordSchema, String recordPacking,
            String recordIdentifier, int recordPosition) {
        if (listener != null) {
            listener.recordMetadata(recordSchema, recordPacking,
                    recordIdentifier, recordPosition);
        }
    }

    @Override
    public void recordData(Collection<XMLEvent> record) {
        if (listener != null && record != null) {
            listener.recordData(record);
        }
    }

    @Override
    public void extraRecordData(Collection<XMLEvent> record) {
        if (listener != null && record != null) {
            listener.extraRecordData(record);
        }
    }

    @Override
    public void endRecord() {
        if (listener != null) {
            listener.endRecord();
        }
    }
    
    public SearchRetrieveResponse setOrigin(URI origin) {
        this.origin = origin;
        return this;
    }

    public URI getOrigin() {
        return origin;
    }
    
    public void setEvents(Collection<XMLEvent> events) {
        this.events = events;
    }
    
    public Collection<XMLEvent> getEvents() {
        return events;
    }    
}

