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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.XMLFilterImpl;

public class XMLFilterReader extends XMLFilterImpl {

    private static final Logger logger = Logger.getLogger(XMLFilterReader.class.getName());
    private static final SAXParserFactory parserFactory =
            SAXParserFactory.newInstance("org.apache.xerces.jaxp.SAXParserFactoryImpl", null);
    private SAXParser parser;
    private XMLFilterBridge bridge;

    public XMLFilterReader() {
        try {
            parserFactory.setFeature("http://xml.org/sax/features/namespaces", true);
            parserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            parser = parserFactory.newSAXParser();
        } catch (ParserConfigurationException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } catch (SAXNotSupportedException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } catch (SAXNotRecognizedException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } catch (SAXException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
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
