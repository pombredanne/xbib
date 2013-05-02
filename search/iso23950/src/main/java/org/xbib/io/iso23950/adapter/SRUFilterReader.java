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
package org.xbib.io.iso23950.adapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.XMLEvent;
import org.xbib.io.iso23950.RecordIdentifierSetter;
import org.xbib.marc.Field;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange;
import org.xbib.marc.MarcXchangeListener;
import org.xbib.marc.MarcXchangeSaxAdapter;
import org.xbib.sru.SearchRetrieveResponse;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SRUFilterReader extends Iso2709Reader implements MarcXchangeListener {

    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private final String nsURI = MarcXchange.NS_URI;
    private final String recordSchema = MarcXchange.NS_PREFIX;
    private final String recordPacking = "xml";
    private final SearchRetrieveResponse response;
    private final String encoding;
    private int recordPosition;
    private RecordIdentifierSetter setter;

    public SRUFilterReader(SearchRetrieveResponse response, String encoding) {
        this.response = response;
        this.recordPosition = 1;
        this.encoding = encoding;
    }
    
    public SRUFilterReader setRecordIdentifierSetter(RecordIdentifierSetter setter) {
        this.setter = setter;
        return this;
    }    
    
    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        new MarcXchangeSaxAdapter()
                .inputSource(input)
                .setContentHandler(getContentHandler())
                .setListener(this)
                .setSchema((String) getProperty(SCHEMA))
                .setFormat((String) getProperty(FORMAT))
                .setType((String) getProperty(TYPE)).parse();
    }

    @Override
    public void beginRecord(String format, String type) {
        response.beginRecord();
        response.recordSchema(recordSchema);
        response.recordPacking(recordPacking);
        response.recordPosition(recordPosition);
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            events.add(eventFactory.createStartDocument());
            // emit additional parameter values for federating
            events.add(eventFactory.createProcessingInstruction("format", format));
            events.add(eventFactory.createProcessingInstruction("type", type));
            events.add(eventFactory.createProcessingInstruction("id", recordPosition + "_" + response.getOrigin().getHost()));
            // SRU
            events.add(eventFactory.createProcessingInstruction("recordSchema", recordSchema));
            events.add(eventFactory.createProcessingInstruction("recordPacking", recordPacking));
            // no recordIdentifier
            events.add(eventFactory.createProcessingInstruction("recordPosition", Integer.toString(recordPosition)));
        }
        recordPosition++;
    }

    @Override
    public void endRecord() {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            response.recordData(events);
        }
        response.endRecord();
        if (events != null) {
            events.add(eventFactory.createEndDocument());
        }
    }

    @Override
    public void leader(String label) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            events.add(eventFactory.createStartElement(recordSchema, nsURI, "leader"));
            events.add(eventFactory.createCharacters(label));
            events.add(eventFactory.createEndElement(recordSchema, nsURI, "leader"));
        }
    }

    @Override
    public void trailer(String trailer) {
        // ignored
    }

    @Override
    public void beginControlField(Field designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            events.add(eventFactory.createStartElement(recordSchema, nsURI, "controlfield"));
            if (designator != null && designator.tag() != null) {
                events.add(eventFactory.createAttribute("tag", designator.tag()));
            }
        }
    }

    @Override
    public void endControlField(Field designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            if (designator != null && designator.data() != null) {
                String s = decode(designator.data());
                // check for 001 tag and put record identifier
                if ("001".equals(designator.tag())) {
                    plugRecordIdentifier(events, s);
                }
                events.add(eventFactory.createCharacters(s));
            }
            events.add(eventFactory.createEndElement(recordSchema, nsURI, "controlfield"));
        }
    }

    @Override
    public void beginDataField(Field designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            events.add(eventFactory.createStartElement(recordSchema, nsURI, "datafield"));
            if (designator != null && designator.tag() != null) {
                events.add(eventFactory.createAttribute("tag", designator.tag()));
                if (designator.indicator() != null) {
                    for (int i = 0; i < designator.indicator().length(); i++) {
                        events.add(eventFactory.createAttribute("ind" + (i + 1),
                                designator.indicator().substring(i, i + 1)));
                    }
                }
            }
        }
    }

    @Override
    public void endDataField(Field designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            if (designator != null && designator.data() != null) {
                events.add(eventFactory.createCharacters(decode(designator.data())));
            }
            events.add(eventFactory.createEndElement(recordSchema, nsURI, "datafield"));
        }
    }

    @Override
    public void beginSubField(Field designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            events.add(eventFactory.createStartElement(recordSchema, nsURI, "subfield"));
            if (designator != null && designator.subfieldId() != null) {
                events.add(eventFactory.createAttribute("code", designator.subfieldId()));
            }
        }
    }

    @Override
    public void endSubField(Field designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            if (designator != null && designator.data() != null) {
                events.add(eventFactory.createCharacters(decode(designator.data())));
            }
            events.add(eventFactory.createEndElement(recordSchema, nsURI, "subfield"));
        }
    }

    private String decode(String value) {
        String s = value;
        try {
            // read from octet stream (ISO-8859-1 = 8 bit) and map to encoding, then normalize 
            s = Normalizer.normalize(new String(s.getBytes("ISO-8859-1"), encoding), Form.NFKC);
            return s;
        } catch (UnsupportedEncodingException ex) {
            return s;
        }
    }

    private void plugRecordIdentifier(Collection<XMLEvent> events, String recordIdentifier) {
        if (events instanceof List) {
            ListIterator<XMLEvent> it = ((List) events).listIterator(events.size());
            while (it.hasPrevious()) {
                XMLEvent e = it.previous();
                if (e.isProcessingInstruction()) {
                    ProcessingInstruction pi = (ProcessingInstruction) e;
                    if ("recordPosition".equals(pi.getTarget())) {
                        it.add(eventFactory.createProcessingInstruction("recordIdentifier", 
                                setter != null ? setter.setRecordIdentifier(recordIdentifier) : recordIdentifier));
                        return;
                    }
                }
            }
        }
    }
}
