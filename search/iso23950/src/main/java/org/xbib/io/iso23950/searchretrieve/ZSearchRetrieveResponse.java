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
package org.xbib.io.iso23950.searchretrieve;

import org.xbib.io.iso23950.RecordIdentifierSetter;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.util.SRUFilterReader;
import org.xbib.io.iso23950.ZResponse;
import org.xbib.io.iso23950.ZSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Iso2709Reader;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Arrays;

public class ZSearchRetrieveResponse extends SearchRetrieveResponse
        implements ZResponse, RecordIdentifierSetter {

    private final Logger logger = LoggerFactory.getLogger(ZSearchRetrieveResponse.class.getName());

    private SearchRetrieveRequest request;

    private ZSession session;

    private byte[] records;

    private byte[] errors;

    private String format;

    private String type;

    private long resultCount;

    public ZSearchRetrieveResponse(SearchRetrieveRequest request) {
        super(request);
        this.request = request;
    }

    public ZSearchRetrieveResponse setSession(ZSession session) {
        this.session = session;
        return this;
    }

    public ZSearchRetrieveResponse setRecords(byte[] records) {
        this.records = records;
        return this;
    }

    public ZSearchRetrieveResponse setErrors(byte[] errors) {
        this.errors = errors;
        return this;
    }

    public ZSearchRetrieveResponse setFormat(String format) {
        this.format = format;
        return this;
    }

    public ZSearchRetrieveResponse setType(String type) {
        this.type = type;
        return this;
    }

    public ZSearchRetrieveResponse setResultCount(long count) {
        this.resultCount = count;
        return this;
    }

    @Override
    public ZSearchRetrieveResponse to(HttpServletResponse servletResponse) throws IOException {
        return to(servletResponse.getWriter());
    }

    @Override
    public ZSearchRetrieveResponse to(Writer writer) throws IOException {
        setOrigin(session.getConnection().getURI());
        // get result count for caller and for stylesheet
        numberOfRecords(resultCount);
        getTransformer().addParameter("numberOfRecords", resultCount);
        // push out results
        ByteArrayInputStream in = new ByteArrayInputStream(records);
        // stream encoding, must always be octet!
        InputSource source = new InputSource(new InputStreamReader(in, "ISO-8859-1"));
        SRUFilterReader reader = new SRUFilterReader(this, "UTF-8");
        try {
            reader.setRecordIdentifierSetter(this);
            reader.setProperty(Iso2709Reader.FORMAT, format);
            reader.setProperty(Iso2709Reader.TYPE, type);
            StreamResult streamResult = new StreamResult(writer);
            getTransformer().setSource(new SAXSource(reader, source)).setResult(streamResult);
            if (getStylesheets() != null) {
                getTransformer().transform(Arrays.asList(getStylesheets()));
            } else {
                getTransformer().transform();
            }
        } catch (SAXNotRecognizedException | SAXNotSupportedException | TransformerException e) {
            throw new IOException(e);
        }
        return this;
    }

    @Override
    public String setRecordIdentifier(String identifier) {
        return null;
    }
}


