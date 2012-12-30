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
     * The schema property
     */
    public static String SCHEMA = "schema";
    private MarcXchangeSaxAdapter adapter;
    private ContentHandler contentHandler;
    private MarcXchangeListener listener;
    private Map<String, Object> properties = new HashMap() {

        {
            put(FORMAT, "MARC21");
            put(TYPE, "Bibliographic");
        }
    };

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
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
        //ignore
    }

    @Override
    public EntityResolver getEntityResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {
        //ignore
    }

    @Override
    public DTDHandler getDTDHandler() {
        throw new UnsupportedOperationException();
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
        //ignore
    }

    @Override
    public ErrorHandler getErrorHandler() {
        throw new UnsupportedOperationException();
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
     * Get the MarcXchange Sax adapter. Useful for inserting MarcXchange data
     * to the MarcXchange listener.
     * @return the MarcXchange Sax adapter
     */
    public MarcXchangeSaxAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        this.adapter = new MarcXchangeSaxAdapter(input).setContentHandler(contentHandler)
                .setListener(listener)
                .setSchema((String) properties.get(SCHEMA))
                .setFormat((String) properties.get(FORMAT))
                .setType((String) properties.get(TYPE));
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
