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
package org.xbib.marc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * ISO 2709 reader behaving like a SaX XMLReader
 *
 */
public class Iso2709Reader implements XMLReader {

    /**
     * The format property. Default value is "MARC21"
     */
    public static String FORMAT = "format";
    /**
     * The type property. Defaylt value is "Bibliographic"
     */
    public static String TYPE = "type";

    /**
     * Should errors abort the reader.
     */
    public static String FATAL_ERRORS = "fatal_errors";

    /**
     * Should errors be silenced
     */
    public static String SILENT_ERRORS = "silent_errors";

    /**
     * Buffer size for input stream
     */
    public static String BUFFER_SIZE = "buffer_size";

    /**
     * The schema property
     */
    public static String SCHEMA = "schema";
    /**
     * The SaX service
     */
    private MarcXchangeSaxAdapter adapter;
    /**
     * XML content handler
     */
    private ContentHandler contentHandler;

    private EntityResolver entityResolver;

    private DTDHandler dtdHandler;

    private ErrorHandler errorHandler;

    private Map<String, Boolean> features = new HashMap();
    /**
     * MarcXchange listener
     */
    private MarcXchangeListener listener;
    /**
     * Properties for this reader
     */
    private Map<String, Object> properties = new HashMap() {

        {
            put(FORMAT, "MARC21");
            put(TYPE, "Bibliographic");
            put(FATAL_ERRORS, Boolean.FALSE);
            put(SILENT_ERRORS, Boolean.FALSE);
            put(BUFFER_SIZE, 65536);
        }
    };

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
         return features.get(name);
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        this.features.put(name, value);
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return properties.get(name);
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        properties.put(name, value);
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
       this.entityResolver = resolver;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }

    @Override
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    @Override
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Set MarcXchange listener for this reader.
     * @param listener the MarcXchange listener
     * @return 
     */
    public Iso2709Reader setMarcXchangeListener(MarcXchangeListener listener) {
        this.listener = listener;
        return this;
    }

    public MarcXchangeListener getMarcXchangeListener() {
        return listener;
    }
    
    /**
     * Get the MarcXchange Sax service. Useful for inserting MarcXchange data
     * to the MarcXchange listener.
     * @return the MarcXchange Sax service
     */
    public MarcXchangeSaxAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        this.adapter = new MarcXchangeSaxAdapter()
                .buffersize((Integer)properties.get(BUFFER_SIZE))
                .inputSource(input)
                .setContentHandler(contentHandler)
                .setListener(listener)
                .setSchema((String) properties.get(SCHEMA))
                .setFormat((String) properties.get(FORMAT))
                .setType((String) properties.get(TYPE))
                .setFatalErrors((Boolean)properties.get(FATAL_ERRORS))
                .setSilentErrors((Boolean)properties.get(SILENT_ERRORS));
        
        adapter.parse();
    }

    /**
     * We do not support system ID based parsing.
     * @param systemId
     * @throws IOException
     * @throws SAXException 
     */
    @Override
    public void parse(String systemId) throws IOException, SAXException {
        throw new UnsupportedOperationException();
    }
}
