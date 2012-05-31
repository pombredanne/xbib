/*
 * Copyright 2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xbib.json;

import java.io.IOException;
import javax.xml.namespace.QName;
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
 * Helper class that can be used for JSON -> XML transformation.
 * <pre>
 *	Transformer transformer = TransformerFactory.newInstance().newTransformer();
 *	InputSource source = new InputSource(...);
 *	Result result = ...;
 *	transformer.transform(new SAXSource(new JsonXmlReader(namespace),source), result);
 * </pre>
 */
public class JsonXmlReader implements XMLReader {

    private final QName root;

    private ContentHandler contentHandler;
    
    public JsonXmlReader(QName root) {
        this.root = root;
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        //ignore
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

    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        new JsonSaxAdapter(input, contentHandler, root).parse();
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException {
        throw new UnsupportedOperationException();
    }

}