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
package org.xbib.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.xbib.common.xcontent.xml.XmlNamespaceContext;

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
 * Read JSON like SaX.
 *
 * Helper class that can be used for JSON to XML transformation.
 * <pre>
 *	Transformer transformer = TransformerFactory.newInstance().newTransformer();
 *	InputSource source = new InputSource(...);
 *	Result result = ...;
 *	transformer.transform(new SAXSource(new JsonXmlReader(namespace),source), result);
 * </pre>
 *
 */
public class JsonXmlReader implements XMLReader {

    private QName root = new QName("root");

    private XmlNamespaceContext context = XmlNamespaceContext.getDefaultInstance();

    private Map<String,Boolean> map = new HashMap();

    private ContentHandler contentHandler;

    private EntityResolver entityResolver;

    private DTDHandler dtdHandler;

    private ErrorHandler errorHandler;

    public JsonXmlReader() {
    }

    public JsonXmlReader root(QName root) {
        this.root = root;
        return this;
    }

    public JsonXmlReader context(XmlNamespaceContext context) {
        this.context = context;
        return this;
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        map.put(name, value);
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return map.get(name);
    }


    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        //ignore
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

    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        if (input.getCharacterStream() != null) {
            new JsonSaxAdapter(input.getCharacterStream(), contentHandler)
                    .root(root)
                    .context(context)
                    .parse();
        } else if (input.getByteStream() != null) {
            String encoding = input.getEncoding() != null? input.getEncoding() : System.getProperty("file.encoding");
            new JsonSaxAdapter(new InputStreamReader(input.getByteStream(), encoding), contentHandler)
                    .root(root)
                    .context(context)
                    .parse();
        }
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException {
        throw new UnsupportedOperationException();
    }

}