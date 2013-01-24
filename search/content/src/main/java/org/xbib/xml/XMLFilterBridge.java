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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public class XMLFilterBridge extends DefaultHandler {

    private final XMLFilterImpl filter;

    XMLFilterBridge(XMLFilterImpl filter) {
        this.filter = filter;
    }

    @Override
    public void setDocumentLocator(Locator lctr) {
        filter.setDocumentLocator(lctr);
    }

    @Override
    public void startDocument() throws SAXException {
        filter.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        filter.endDocument();
    }

    @Override
    public void startPrefixMapping(String string, String string1) throws SAXException {
        filter.startPrefixMapping(string, string1);
    }

    @Override
    public void endPrefixMapping(String string) throws SAXException {
        filter.endPrefixMapping(string);
    }

    @Override
    public void startElement(String string, String string1, String string2, Attributes atrbts) throws SAXException {
        filter.startElement(string, string1, string2, atrbts);
    }

    @Override
    public void endElement(String string, String string1, String string2) throws SAXException {
        filter.endElement(string, string1, string2);
    }

    @Override
    public void characters(char[] chars, int i, int i1) throws SAXException {
        filter.characters(chars, i, i1);
    }

    @Override
    public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
        filter.ignorableWhitespace(chars, i, i1);
    }

    @Override
    public void processingInstruction(String string, String string1) throws SAXException {
        filter.processingInstruction(string, string1);
    }

    @Override
    public void skippedEntity(String string) throws SAXException {
        filter.skippedEntity(string);
    }
}