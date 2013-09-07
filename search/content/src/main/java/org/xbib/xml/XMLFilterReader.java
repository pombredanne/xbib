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
package org.xbib.xml;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A XML reader which is also a filter.
 *
 * Does evaluate namespaces and does not validate or import external entities or document type definitions.
 *
 */
public class XMLFilterReader extends XMLFilterImpl {

    private final Logger logger = LoggerFactory.getLogger(XMLFilterReader.class.getName());

    private final static String PARSERFACTORY = "org.apache.xerces.jaxp.SAXParserFactoryImpl";

    private static final SAXParserFactory parserFactory =
            SAXParserFactory.newInstance(PARSERFACTORY, null);

    private SAXParser parser;

    private XMLFilterBridge bridge;

    public XMLFilterReader() {
        try {
            parserFactory.setFeature("http://xml.org/sax/features/namespaces", true);
            parserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            parserFactory.setFeature("http://xml.org/sax/features/validation", false);
            parserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            parserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            parser = parserFactory.newSAXParser();
        } catch (ParserConfigurationException | SAXException  e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * Saxon uses setFeature, so we override it here, otherwise XmlFilterImpl will bark.
     *
     * @param name
     * @param value
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     */
    @Override
    public void setFeature (String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        // accept all setFeature calls, but do nothing
    }

    @Override
    public void parse(InputSource input) throws SAXException, IOException {
        this.bridge = getBridge();
        parser.parse(input, bridge);
    }

    protected XMLFilterBridge getBridge() {
        return new XMLFilterBridge(this);
    }
}
