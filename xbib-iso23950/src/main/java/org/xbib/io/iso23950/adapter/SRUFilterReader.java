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
import javax.xml.stream.events.XMLEvent;
import org.xbib.marc.FieldDesignator;
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

    public SRUFilterReader(SearchRetrieveResponse response, String encoding) {
        this.response = response;
        this.recordPosition = 1;
        this.encoding = encoding;
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        new MarcXchangeSaxAdapter(input)
                .setContentHandler(getContentHandler())
                .setListener(this)
                .setSchema((String) getProperty(SCHEMA))
                .setFormat((String) getProperty(FORMAT))
                .setType((String) getProperty(TYPE)).parse();
    }

    @Override
    public void beginRecord(String format, String type) {
        response.beginRecord();
        response.recordMetadata(recordSchema, recordPacking, "", recordPosition);
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            events.add(eventFactory.createStartDocument());
            // emit additional parameter values for federating disguised as "namespace"
            events.add(eventFactory.createNamespace("format", format));
            events.add(eventFactory.createNamespace("type", type));
            events.add(eventFactory.createNamespace("id", recordPosition + "_" + response.getOrigin().getHost()));
            // disguised namespaces for SRU
            events.add(eventFactory.createNamespace("recordSchema", recordSchema));
            events.add(eventFactory.createNamespace("recordPacking", recordPacking));
            // no recordIdentifier possible here
            events.add(eventFactory.createNamespace("recordPosition", Integer.toString(recordPosition)));
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
    public void beginControlField(FieldDesignator designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            events.add(eventFactory.createStartElement(recordSchema, nsURI, "controlfield"));
            if (designator != null && designator.getTag() != null) {
                events.add(eventFactory.createAttribute("tag", designator.getTag()));
            }
        }
    }

    @Override
    public void endControlField(FieldDesignator designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            if (designator != null && designator.getData() != null) {
                String s = decode(designator.getData());
                // plugin recordIdentifier to the disguised namespaces
                if ("001".equals(designator.getTag())) {
                    plugRecordIdentifier(events, s);
                }
                events.add(eventFactory.createCharacters(s));
            }
            events.add(eventFactory.createEndElement(recordSchema, nsURI, "controlfield"));
        }
    }

    @Override
    public void beginDataField(FieldDesignator designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            events.add(eventFactory.createStartElement(recordSchema, nsURI, "datafield"));
            if (designator != null && designator.getTag() != null) {
                events.add(eventFactory.createAttribute("tag", designator.getTag()));
                if (designator.getIndicator() != null) {
                    for (int i = 0; i < designator.getIndicator().length(); i++) {
                        events.add(eventFactory.createAttribute("ind" + (i + 1),
                                designator.getIndicator().substring(i, i + 1)));
                    }
                }
            }
        }
    }

    @Override
    public void endDataField(FieldDesignator designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            if (designator != null && designator.getData() != null) {
                events.add(eventFactory.createCharacters(decode(designator.getData())));
            }
            events.add(eventFactory.createEndElement(recordSchema, nsURI, "datafield"));
        }
    }

    @Override
    public void beginSubField(FieldDesignator designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            events.add(eventFactory.createStartElement(recordSchema, nsURI, "subfield"));
            if (designator != null && designator.getSubfieldId() != null) {
                events.add(eventFactory.createAttribute("code", designator.getSubfieldId()));
            }
        }
    }

    @Override
    public void endSubField(FieldDesignator designator) {
        Collection<XMLEvent> events = response.getEvents();
        if (events != null) {
            if (designator != null && designator.getData() != null) {
                events.add(eventFactory.createCharacters(decode(designator.getData())));
            }
            events.add(eventFactory.createEndElement(recordSchema, nsURI, "subfield"));
        }
    }

    private String decode(String value) {
        String s = value;
        try {
            // read from octet stream and map to encoding
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
                if (e.isNamespace()) {
                    Namespace ns = (Namespace) e;
                    if ("recordPosition".equals(ns.getPrefix())) {
                        it.add(eventFactory.createNamespace("recordIdentifier", recordIdentifier));
                        return;
                    }
                }
            }
        }
    }
}
