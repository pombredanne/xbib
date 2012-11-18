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
package org.xbib.xml.transform;

import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

public class StylesheetTransformer {

    /**
     * We really depend on Xalan 2.7.1 for now. With JDK 6 internal Xalan, there
     * are NPEs while processing attribute patterns
     *
     * @*
     * Do not use static SAXTransformerFactory. It is not thread safe.
     */
    private final SAXTransformerFactory transformerFactory;
    private final TransformerURIResolver resolver;
    private final static StylesheetPool pool = new StylesheetPool();
    private final Map<String, Object> parameters = new HashMap();
    private Source source;
    private Source xslSource;
    private Source xslSource2;
    private StreamResult target;

    public StylesheetTransformer() {
        this.resolver = new TransformerURIResolver();
        transformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl", null);
        transformerFactory.setErrorListener(new StylesheetErrorListener());
        transformerFactory.setURIResolver(resolver);
    }

    public StylesheetTransformer(String... path) {
        this.resolver = new TransformerURIResolver(path);
        transformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl", null);
        transformerFactory.setErrorListener(new StylesheetErrorListener());
        transformerFactory.setURIResolver(resolver);
    }

    public StylesheetTransformer addParameter(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public StylesheetTransformer setSource(Reader xmlReader) {
        this.source = new StreamSource(xmlReader);
        return this;
    }

    public StylesheetTransformer setSource(Source source) {
        this.source = source;
        return this;
    }

    public StylesheetTransformer setSource(InputSource source) {
        this.source = new SAXSource(source);
        return this;
    }

    public StylesheetTransformer setSource(XMLReader reader, InputSource in) {
        this.source = new SAXSource(reader, in);
        return this;
    }

    public StylesheetTransformer setXsl(String xslPath) throws TransformerException {
        this.xslSource = resolver.resolve(xslPath, null);
        return this;
    }

    public StylesheetTransformer setXsl2(String xslPath) throws TransformerException {
        this.xslSource2 = resolver.resolve(xslPath, null);
        return this;
    }

    public StylesheetTransformer setFilter(XMLReader reader, InputSource in, String xsl, ContentHandler handler)
            throws TransformerConfigurationException, TransformerException {
        XMLFilter filter = transformerFactory.newXMLFilter(resolver.resolve(xsl, null));
        filter.setParent(reader);
        filter.setContentHandler(handler);
        this.source = new SAXSource(filter, in);
        return this;
    }

    public StylesheetTransformer setTarget(Writer result) {
        this.target = new StreamResult(result);
        return this;
    }

    public StylesheetTransformer setTarget(OutputStream result) {
        this.target = new StreamResult(result);
        return this;
    }

    public StylesheetTransformer setTarget(StreamResult result) {
        this.target = result;
        return this;
    }

    public void apply() throws TransformerException {
        Transformer transformer = transformerFactory.newTransformer();
        try {
            addParameters(transformer);
            if (xslSource == null) {
                transformer.transform(source, target);
                return;
            }
            if (xslSource2 == null) {
                Templates templates = pool.newTemplates(transformerFactory, xslSource);
                TransformerHandler thandler = pool.newTransformerHandler(transformerFactory, templates);
                //Templates templates = transformerFactory.newTemplates(xslSource);
                //TransformerHandler thandler = transformerFactory.newTransformerHandler(templates);
                thandler.setResult(target);
                addParameters(thandler.getTransformer());
                transformer.transform(source, new SAXResult(thandler));
                return;
            }
            Templates templates1 = pool.newTemplates(transformerFactory, xslSource);
            Templates templates2 = pool.newTemplates(transformerFactory, xslSource2);
            TransformerHandler handler1 = pool.newTransformerHandler(transformerFactory, templates1);
            TransformerHandler handler2 = pool.newTransformerHandler(transformerFactory, templates2);
            handler1.setResult(new SAXResult(handler2));
            handler2.setResult(target);
            addParameters(handler1.getTransformer());
            addParameters(handler2.getTransformer());
            transformer.transform(source, new SAXResult(handler1));
        } finally {
            resolver.close();
        }
    }

    private void addParameters(Transformer transformer) {
        for (Map.Entry<String, Object> me : parameters.entrySet()) {
            transformer.setParameter(me.getKey(), me.getValue());
        }
    }
}
