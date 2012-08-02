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
package org.xbib.federator;

import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.XMLEvent;
import org.xbib.sru.SRU;

public class Result {

    private final static XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private final static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private long count;
    private LinkedList<XMLEvent> events;

    Result(long count, LinkedList<XMLEvent> events) {
        this.count = count;
        this.events = events;
    }

    public long getCount() {
        return count;
    }

    public LinkedList<XMLEvent> getEvents() {
        return events;
    }

    public void toSRUResponse(String version, Writer writer) throws XMLStreamException {
        toSRUResponse(version, outputFactory.createXMLEventWriter(writer));
    }
    
    public void toSRUResponse(String version, XMLEventWriter ew) throws XMLStreamException {
        ew.add(eventFactory.createStartDocument());
        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "searchRetrieveResponse"));
        ew.add(eventFactory.createNamespace(SRU.NS_PREFIX, SRU.NS_URI));
        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "version"));
        ew.add(eventFactory.createCharacters(version));
        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "version"));
        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "numberOfRecords"));
        ew.add(eventFactory.createCharacters(Long.toString(count)));
        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "numberOfRecords"));
        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "records"));
        int pos = 1;
        boolean inElement = false;
        Iterator<XMLEvent> it = events.iterator();
        while (it.hasNext()) {
            XMLEvent e = it.next();
            if (e.isProcessingInstruction()) {
                // drop all processing instructions
                ProcessingInstruction pi = (ProcessingInstruction)e;
                String prefix = pi.getTarget();
                String nsURI = pi.getData();
                switch (prefix) {
                    case "recordSchema":
                        // declare SRU record schema only if registered in SRU class
                        if (SRU.RECORD_SCHEMAS.containsKey(nsURI)) {
                            // add XML namespace
                            if (SRU.RECORD_SCHEMA_NAMESPACES.containsKey(nsURI)) {
                                ew.add(eventFactory.createNamespace(nsURI, SRU.RECORD_SCHEMA_NAMESPACES.get(nsURI).toASCIIString()));
                            }
                            ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "recordSchema"));
                            ew.add(eventFactory.createCharacters(SRU.RECORD_SCHEMAS.get(nsURI).toASCIIString()));
                            ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "recordSchema"));
                        }
                        break;
                    case "recordPacking":
                        // SRU record packing (always "xml")
                        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "recordPacking"));
                        ew.add(eventFactory.createCharacters(nsURI));
                        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "recordPacking"));
                        break;
                    case "recordIdentifier":
                        // SRU record identifier
                        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "recordIdentifier"));
                        ew.add(eventFactory.createCharacters(nsURI));
                        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "recordIdentifier"));
                        break;
                    case "recordPosition":
                        // SRU record position is the global position (NOT the local record position)
                        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "recordPosition"));
                        ew.add(eventFactory.createCharacters(Integer.toString(pos++)));
                        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "recordPosition"));
                        // now, after recordPosition, start with recordData
                        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "recordData"));
                        break;
                    case "id":
                        // let us identify the origin of the record by an XML ID
                        ew.add(eventFactory.createAttribute(prefix, nsURI));
                        break;
                    case "format":
                    case "type":
                        // skip format, type
                        break;
                }
                
            } else if (e.isStartDocument()) {
                ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "record"));
            } else if (e.isEndDocument()) {
                ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "recordData"));
                ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "record"));            
            } else {
                if (e.isStartElement()) {
                    inElement = true;
                } else if (e.isEndElement()) {
                    inElement = false;
                }
                if (!e.isCharacters() || inElement) {
                    ew.add(e);
                }
            }
        }
        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "records"));
        ew.add(eventFactory.createEndDocument());
    }
}