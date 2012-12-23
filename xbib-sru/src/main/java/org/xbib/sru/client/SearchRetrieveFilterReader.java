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
package org.xbib.sru.client;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;
import org.xbib.sru.SRU;
import org.xbib.sru.SearchRetrieve;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.xml.XMLNamespaceContext;
import org.xbib.xml.XMLFilterReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SearchRetrieveFilterReader extends XMLFilterReader {

    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private final XMLNamespaceContext namespaceContext = XMLNamespaceContext.newInstance();
    private final SearchRetrieve request;
    private final SearchRetrieveResponse response;
    private String recordPacking;
    private String recordSchema;
    private int recordPosition;
    private String recordIdentifier;
    private String content;
    private LinkedList<XMLEvent> recordData;
    private LinkedList<XMLEvent> extraRecordData;
    private boolean echo;
    private boolean isRecordIdentifier;

    public SearchRetrieveFilterReader(SearchRetrieve request, SearchRetrieveResponse response) {
        this.request = request;
        this.response = response;
        this.recordData = new LinkedList();
        this.extraRecordData = new LinkedList();
    }

    @Override
    public void startDocument() throws SAXException {
        echo = false;
        response.onConnect(request);
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        response.onDisconnect(request);
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localname, String qname, Attributes atts)
            throws SAXException {
        if (SRU.NS_URI.equals(uri)) {
            switch (localname) {
                case "record":
                    recordPacking = null;
                    recordSchema = null;
                    recordIdentifier = null;
                    recordPosition = 0;
                    response.beginRecord();
                    break;
                case "recordData": {
                    recordData.add(eventFactory.createStartDocument());
                    ListIterator<Namespace> namespaces = getNamespaces(namespaceContext);
                    while (namespaces.hasNext()) {
                        Namespace ns = namespaces.next();
                        recordData.add(eventFactory.createNamespace(ns.getPrefix(), ns.getNamespaceURI()));
                    }
                    break;
                }
                case "extraRecordData": {
                    extraRecordData.add(eventFactory.createStartDocument());
                    ListIterator<Namespace> namespaces = getNamespaces(namespaceContext);
                    while (namespaces.hasNext()) {
                        Namespace ns = namespaces.next();
                        extraRecordData.add(eventFactory.createNamespace(ns.getPrefix(), ns.getNamespaceURI()));
                    }
                    break;
                }
                case "echoedSearchRetrieveRequest":
                    echo = true;
                    break;
            }
        } else {
            isRecordIdentifier = false;
            if (!recordData.isEmpty()) {
                QName q = toQName(uri, qname);
                recordData.add(eventFactory.createStartElement(q.getPrefix(), q.getNamespaceURI(), q.getLocalPart()));
                ListIterator<Attribute> attributes = getAttributes(atts);
                while (attributes.hasNext()) {
                    Attribute a = attributes.next();
                    recordData.add(eventFactory.createAttribute(a.getName(), a.getValue()));
                    isRecordIdentifier = "controlfield".equals(q.getLocalPart()) && "tag".equals(a.getName().getLocalPart()) && "001".equals(a.getValue());
                }
            }
            if (!extraRecordData.isEmpty()) {
                QName q = toQName(uri, qname);
                extraRecordData.add(eventFactory.createStartElement(q.getPrefix(), q.getNamespaceURI(), q.getLocalPart()));
                ListIterator<Attribute> attributes = getAttributes(atts);
                while (attributes.hasNext()) {
                    Attribute a = attributes.next();
                    extraRecordData.add(eventFactory.createAttribute(a.getName(), a.getValue()));
                }
            }
        }
        super.startElement(uri, localname, qname, atts);
    }

    @Override
    public void endElement(String uri, String localname, String qname) throws SAXException {
        if (SRU.NS_URI.equals(uri)) {
            if ("recordPacking".equals(localname)) {
                recordPacking = content;
            } else if ("recordSchema".equals(localname)) {
                recordSchema = content;
            } else if ("recordData".equals(localname)) {
                recordData.add(eventFactory.createEndDocument());
                response.recordData(recordData);
                recordData = new LinkedList();
            } else if ("extraRecordData".equals(localname)) {
                extraRecordData.add(eventFactory.createEndDocument());
                response.extraRecordData(extraRecordData);
                extraRecordData = new LinkedList();
            } else if ("recordPosition".equals(localname)) {
                recordPosition = Integer.parseInt(content);
            } else if ("recordIdentifier".equals(localname)) {
                recordIdentifier = content;
            } else if ("record".equals(localname)) {
                response.recordMetadata(recordSchema, recordPacking, recordIdentifier, recordPosition);
                response.endRecord();
            } else if ("echoedSearchRetrieveRequest".equals(localname)) {
                echo = false;
            } else if ("version".equals(localname) && !echo) {
                response.version(content);
            } else if ("numberOfRecords".equals(localname)) {
                int n = -1;
                try {
                    n = Integer.parseInt(content);
                } catch (NumberFormatException e) {
                    // drop                    
                }
                response.numberOfRecords(n);
                response.addResponseParameter("X-SRU-numberOfRecords", content);
            }
        } else {
            if (!recordData.isEmpty()) {
                QName q = toQName(uri, qname);
                recordData.add(eventFactory.createEndElement(q.getPrefix(), q.getNamespaceURI(), q.getLocalPart()));
                if (isRecordIdentifier && recordIdentifier == null) {
                    recordIdentifier = content;
                }
            }
            if (!extraRecordData.isEmpty()) {
                QName q = toQName(uri, qname);
                recordData.add(eventFactory.createEndElement(q.getPrefix(), q.getNamespaceURI(), q.getLocalPart()));
            }
        }
        super.endElement(uri, localname, qname);
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        this.content = new String(chars, start, length);
        if (!recordData.isEmpty()) {
            recordData.add(eventFactory.createCharacters(content));
        }
        if (!extraRecordData.isEmpty()) {
            extraRecordData.add(eventFactory.createCharacters(content));
        }
        super.characters(chars, start, length);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        namespaceContext.addNamespace(prefix, uri);
        super.startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        namespaceContext.removeNamespace(prefix);
        super.endPrefixMapping(prefix);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        super.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        if (!recordData.isEmpty()) {
            recordData.add(eventFactory.createProcessingInstruction(target, data));
        }
        if (!extraRecordData.isEmpty()) {
            extraRecordData.add(eventFactory.createProcessingInstruction(target, data));
        }
        super.processingInstruction(target, data);
    }

    private QName toQName(String namespaceUri, String qname) {
        int i = qname.indexOf(':');
        if (i == -1) {
            return new QName(namespaceUri, qname);
        } else {
            String prefix = qname.substring(0, i);
            String localPart = qname.substring(i + 1);
            return new QName(namespaceUri, localPart, prefix);
        }
    }

    private ListIterator<Attribute> getAttributes(Attributes attributes) {
        List<Attribute> list = new LinkedList();
        for (int i = 0; i < attributes.getLength(); i++) {
            QName name = toQName(attributes.getURI(i), attributes.getQName(i));
            if (!("xmlns".equals(name.getLocalPart()) || "xmlns".equals(name.getPrefix()))) {
                list.add(eventFactory.createAttribute(name, attributes.getValue(i)));
            }
        }
        return list.listIterator();
    }

    private ListIterator<Namespace> getNamespaces(XMLNamespaceContext namespaceContext) {
        List<Namespace> namespaces = new LinkedList();
        String defaultNamespaceUri = namespaceContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
        if (defaultNamespaceUri != null && defaultNamespaceUri.length() > 0) {
            namespaces.add(eventFactory.createNamespace(defaultNamespaceUri));
        }
        for (String prefix : namespaceContext.getNamespacePrefixes()) {
            String namespaceUri = namespaceContext.getNamespaceURI(prefix);
            namespaces.add(eventFactory.createNamespace(prefix, namespaceUri));
        }
        return namespaces.listIterator();
    }
    
}