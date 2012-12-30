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
package org.xbib.marc.xml;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchange;
import org.xbib.marc.MarcXchangeListener;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MarcXmlReader
        extends DefaultHandler implements MarcXchange, MarcXchangeListener {

    private static final SAXParserFactory factory = SAXParserFactory.newInstance();
    private ContentHandler contentHandler;
    private MarcXchangeListener listener;
    private InputSource source;
    private List<Field> fields = new LinkedList();
    private StringBuilder content = new StringBuilder();

    public MarcXmlReader(final InputSource source) {
        this.source = source;
        factory.setNamespaceAware(true);
    }

    public MarcXmlReader setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
        return this;
    }

    public MarcXmlReader setListener(MarcXchangeListener listener) {
        this.listener = listener;
        return this;
    }

    public void parse() throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser = factory.newSAXParser();
        parser.parse(source, this);
    }

    @Override
    public void leader(String label) {
        if (listener != null) {
            listener.leader(label);
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        if (listener != null) {
            listener.beginRecord(format, type);
        }
    }

    @Override
    public void beginControlField(Field designator) {
        if (listener != null) {
            listener.beginControlField(designator);
        }
    }

    @Override
    public void beginDataField(Field designator) {
        if (listener != null) {
            listener.beginDataField(designator);
        }
    }

    @Override
    public void beginSubField(Field designator) {
        if (listener != null) {
            listener.beginSubField(designator);
        }
    }

    @Override
    public void endRecord() {
        if (listener != null) {
            listener.endRecord();
        }
    }

    @Override
    public void endControlField(Field designator) {
        if (listener != null) {
            listener.endControlField(designator);
        }
    }

    @Override
    public void endDataField(Field designator) {
        if (listener != null) {
            listener.endDataField(designator);
        }
    }

    @Override
    public void endSubField(Field designator) {
        if (listener != null) {
            listener.endSubField(designator);
        }
    }

    @Override
    public void trailer(String trailer) {
        if (listener != null) {
            listener.trailer(trailer);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // not needed
    }

    @Override
    public void startDocument() throws SAXException {
        fields.clear();
        content.setLength(0);
    }

    @Override
    public void endDocument() throws SAXException {
        trailer(null);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // ignore all mappings
        if (contentHandler != null) {
            contentHandler.startPrefixMapping(prefix, uri);
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // ignore all mappings
        if (contentHandler != null) {
            contentHandler.endPrefixMapping(prefix);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        content.setLength(0);
        if (!checkNamespace(uri)) {
            return;
        }
        switch (localName) {
            case RECORD: {
                // get format and type
                String format = "MARC21";
                String type = " Bibliographic";
                for (int i = 0; i < atts.getLength(); i++) {
                    switch (atts.getLocalName(i)) {
                        case FORMAT:
                            format = atts.getValue(i);
                            break;
                        case TYPE:
                            type = atts.getValue(i);
                            break;
                    }
                }
                beginRecord(format, type);
                break;
            }
            case LEADER: {
                // do nothing
                break;
            }
            case CONTROLFIELD: {
                String tag = "";
                for (int i = 0; i < atts.getLength(); i++) {
                    String name = atts.getLocalName(i);
                    if (TAG.equals(name)) {
                        tag = atts.getValue(i);
                    }
                }
                Field field = new Field().setTag(tag);
                beginControlField(field);
                fields.add(field);
                break;
            }
            case DATAFIELD: {
                String tag = "";
                char[] indicators = new char[10];
                for (int i = 0; i < atts.getLength(); i++) {
                    String name = atts.getLocalName(i);
                    if (TAG.equals(name)) {
                        tag = atts.getValue(i);
                    }
                    if (name.startsWith("ind")) {
                        int pos = Integer.parseInt(name.substring(3));
                        indicators[pos] = atts.getValue(i).charAt(0);
                    }
                }
                Field field = new Field().setTag(tag).setIndicator(new String(indicators));
                beginDataField(field);
                fields.add(field);
                break;
            }
            case SUBFIELD: {
                Field field = fields.get(fields.size() - 1);
                field.setSubfieldId(null); // reset sub field ID
                field.setData(null); // reset data
                for (int i = 0; i < atts.getLength(); i++) {
                    if (CODE.equals(atts.getLocalName(i))) {
                        field.setSubfieldId(atts.getValue(i));
                    }
                }
                beginSubField(field);
                fields.add(field);
                break;
            }
        }
        if (contentHandler != null) {
            contentHandler.startElement(uri, localName, qName, atts);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!checkNamespace(uri)) {
            content.setLength(0);
            return;
        }
        // ignore namespaces, just check local names
        switch (localName) {
            case RECORD: {
                endRecord();
                break;
            }
            case LEADER: {
                leader(content.toString());
                break;
            }
            case CONTROLFIELD: {
                endControlField(fields.remove(0).setData(content.toString()));
                break;
            }
            case DATAFIELD: {
                endDataField(fields.remove(0).setSubfieldId(null).setData(content.toString()));
                break;
            }
            case SUBFIELD: {
                endSubField(fields.remove(fields.size() - 1).setData(content.toString()));
                break;
            }
        }
        content.setLength(0);
        if (contentHandler != null) {
            contentHandler.endElement(uri, localName, qName);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String s = new String(ch, start, length);
        if (!s.trim().isEmpty()) {
            content.append(s);
        }
        if (contentHandler != null) {
            contentHandler.characters(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (contentHandler != null) {
            contentHandler.ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        if (contentHandler != null) {
            contentHandler.processingInstruction(target, data);
        }
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        if (contentHandler != null) {
            contentHandler.skippedEntity(name);
        }
    }

    private boolean checkNamespace(String uri) {
        return NS_PREFIX.equals(uri) || MARC21_NS_URI.equals(uri);
    }
}
