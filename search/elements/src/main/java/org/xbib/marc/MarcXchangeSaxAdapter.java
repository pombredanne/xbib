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
package org.xbib.marc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import org.xbib.io.sequential.CharStream;
import org.xbib.io.sequential.CharStreamFactory;
import org.xbib.io.sequential.CharStreamListener;
import org.xbib.io.sequential.Separable;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.xml.XMLNS;
import org.xbib.xml.XMLUtil;
import org.xbib.xml.XSI;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class MarcXchangeSaxAdapter implements MarcXchange, MarcXchangeListener {

    private static final Logger logger = LoggerFactory.getLogger(MarcXchangeSaxAdapter.class.getName());
    private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
    private static final CharStreamFactory factory = CharStreamFactory.getInstance();
    private final CharStreamListener streamListener = new Iso2709StreamListener();
    private CharStream stream;
    private char mark = '\u0000';
    private int position = 0;
    private FieldDirectory directory;
    private Field designator;
    private RecordLabel label;
    private boolean datafieldOpen;
    private boolean subfieldOpen;
    private boolean recordOpen;
    private String schema;
    private String format;
    private String type;
    private String id;
    private String nsUri;
    private ContentHandler contentHandler;
    private MarcXchangeListener listener;
    private boolean fatalerrors = false;
    private boolean silenterrors = false;
    private int buffersize = 8192;

    public MarcXchangeSaxAdapter() {
        this.subfieldOpen = false;
        this.recordOpen = false;
    }

    public MarcXchangeSaxAdapter buffersize(int buffersize) {
        this.buffersize = buffersize;
        return this;
    }

    public MarcXchangeSaxAdapter inputSource(final InputSource source) throws IOException {
        if (source.getByteStream() != null) {
            String encoding = source.getEncoding() != null ? source.getEncoding() : "ANSEL";
            Reader reader = new InputStreamReader(source.getByteStream(), encoding);
            this.stream = factory.newStream(reader, buffersize, streamListener);
        } else {
            Reader reader = source.getCharacterStream();
            this.stream = factory.newStream(reader, buffersize, streamListener);
        }
        return this;
    }

    public MarcXchangeSaxAdapter setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
        return this;
    }

    public MarcXchangeSaxAdapter setListener(MarcXchangeListener listener) {
        this.listener = listener;
        return this;
    }

    public MarcXchangeSaxAdapter setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public MarcXchangeSaxAdapter setFormat(String format) {
        this.format = format;
        return this;
    }

    public MarcXchangeSaxAdapter setType(String type) {
        this.type = type;
        return this;
    }
    
    public MarcXchangeSaxAdapter setFatalErrors(Boolean fatalerrors) {
        this.fatalerrors = fatalerrors;
        return this;
    }

    public MarcXchangeSaxAdapter setSilentErrors(Boolean silenterrors) {
        this.silenterrors = silenterrors;
        return this;
    }

    public String getIdentifier() {
        return id;
    }

    /**
     * Parse ISO 2709 and emit SAX events.
     */
    public void parse() throws IOException, SAXException {
        beginCollection();
        String chunk;
        do {
            chunk = stream.readData();
        } while (chunk != null);
        stream.close();
        endCollection();
    }

    public void beginCollection() throws SAXException {
        if (contentHandler == null) {
            logger.warn("no content handler set");
            return;
        }
        contentHandler.startDocument();
        // write schema info
        AttributesImpl attrs = new AttributesImpl();
        if ("MARC21".equalsIgnoreCase(schema)) {
            this.nsUri = MARC21_NS_URI;
            attrs.addAttribute(XMLNS.NS_URI, XSI.NS_PREFIX,
                    XMLNS.NS_PREFIX + ":" + XSI.NS_PREFIX, "CDATA", XSI.NS_URI);
            attrs.addAttribute(XSI.NS_URI, "schemaLocation",
                    XSI.NS_PREFIX + ":schemaLocation", "CDATA", MARC21_NS_URI + " " + MARC21_SCHEMA);

        } else {
            this.nsUri = NS_URI;
            attrs.addAttribute(XMLNS.NS_URI, XSI.NS_PREFIX,
                    XMLNS.NS_PREFIX + ":" + XSI.NS_PREFIX, "CDATA", XSI.NS_URI);
            attrs.addAttribute(XSI.NS_URI, "schemaLocation",
                    XSI.NS_PREFIX + ":schemaLocation", "CDATA", NS_URI + " " + MARCXCHANGE_SCHEMA);
        }
        contentHandler.startPrefixMapping("", nsUri);
        contentHandler.startElement(nsUri, COLLECTION, COLLECTION, attrs);
    }

    public void endCollection() throws SAXException {
        if (contentHandler == null) {
            logger.warn("no content handler set");
            return;
        }
        contentHandler.endElement(nsUri, COLLECTION, COLLECTION);
        contentHandler.endDocument();
    }

    @Override
    public void beginRecord(String format, String type) {
        if (recordOpen) {
            return;
        }
        try {
            AttributesImpl attrs = new AttributesImpl();
            if (format != null && !"MARC21".equalsIgnoreCase(schema)) {
                attrs.addAttribute(nsUri, FORMAT, FORMAT, "CDATA", format);
            }
            if (type != null) {
                attrs.addAttribute(nsUri, TYPE, TYPE, "CDATA", type);
            }
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, RECORD, RECORD, attrs);
            }
            if (listener != null) {
                listener.beginRecord(format, type);
            }
            this.recordOpen = true;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endRecord() {
        if (!recordOpen) {
            return;
        }
        try {
            if (listener != null) {
                listener.endRecord();
            }
            if (contentHandler != null) {
                contentHandler.endElement(nsUri, RECORD, RECORD);
            }
            if (listener != null) {
                // emit trailer event, drives record output segmentation
                listener.trailer(null);
            }
            this.recordOpen = false;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void leader(String value) {
        if (value == null) {
            return;
        }
        try {
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, LEADER, LEADER, EMPTY_ATTRIBUTES);
                contentHandler.characters(value.toCharArray(), 0, value.length());
                contentHandler.endElement(nsUri, LEADER, LEADER);
            }
            if (listener != null) {
                listener.leader(value);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }
    
    @Override
    public void trailer(String trailer) {
        // do nothing, MARC reading defines no trailer
    }

    @Override
    public void beginControlField(Field designator) {
        if (designator == null) {
            return;
        }
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute(nsUri, TAG, TAG, "CDATA", designator.tag());
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, CONTROLFIELD, CONTROLFIELD, attrs);
            }
            if (listener != null) {
                listener.beginControlField(designator);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endControlField(Field designator) {
        try {
            if (listener != null) {
                listener.endControlField(designator);
            }
            if (designator != null) {
                String value = designator.data();
                if (!value.isEmpty()) {
                    switch (designator.tag()) {
                        case "001":
                            this.id = value;
                            break;
                        case "006":
                        case "007":
                        case "008":
                            // fix fill characters here
                            value = value.replace('^', '|');
                            break;
                    }
                    if (contentHandler != null) {
                        contentHandler.characters(value.toCharArray(), 0, value.length());
                    }
                }
            }
            if (contentHandler != null) {
                contentHandler.endElement(nsUri, CONTROLFIELD, CONTROLFIELD);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void beginDataField(Field designator) {
        if (designator == null) {
            return;
        }
        try {
            if (designator.isControlField()) {
                beginControlField(designator);
                endControlField(designator);
                return;
            }
            if (datafieldOpen) {
                return;
            }
            AttributesImpl attrs = new AttributesImpl();
            String tag = designator.tag();
            if (tag == null || tag.length() == 0) {
                tag = Field.NULL_TAG; // fallback
                designator.tag(tag);
            }
            attrs.addAttribute(nsUri, TAG, TAG, "CDATA", tag);
            int ind = designator.indicator() != null
                    ? designator.indicator().length() : 0;
            // force at least two default blank indicators if schema is Marc 21
            if ("MARC21".equalsIgnoreCase(schema)) {
                for (int i = (ind == 0 ? 1 : ind); i <= 2; i++) {
                    attrs.addAttribute(null, IND + i, IND + i, "CDATA", " ");
                }
            }
            // set indicators
            for (int i = 1; i <= ind; i++) {
                attrs.addAttribute(null, IND + i,
                        IND + i, "CDATA", designator.indicator().substring(i - 1, i));
            }
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, DATAFIELD, DATAFIELD, attrs);
            }
            if (listener != null) {
                listener.beginDataField(designator);
            }
            datafieldOpen = true;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endDataField(Field designator) {
        try {
            if (!datafieldOpen) {
                return;
            }
            if (listener != null) {
                listener.endDataField(designator);
            }
            if (designator != null) {
                String value = designator.data();
                if (value != null && !value.isEmpty()) {
                    value = normalizeValue(value);
                    // write data field per default into a subfield with code 'a'
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute(nsUri, CODE, CODE, "CDATA", "a");
                    if (contentHandler != null) {
                        contentHandler.startElement(nsUri, SUBFIELD, SUBFIELD, attrs);
                        contentHandler.characters(value.toCharArray(), 0, value.length());
                        contentHandler.endElement(nsUri, SUBFIELD, SUBFIELD);
                    }
                }
            }
            if (contentHandler != null) {
                contentHandler.endElement(NS_URI, DATAFIELD, DATAFIELD);
            }
            datafieldOpen = false;
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void beginSubField(Field designator) {
        if (designator == null) {
            return;
        }
        try {
            AttributesImpl attrs = new AttributesImpl();
            String subfieldId = designator.subfieldId();
            if (subfieldId == null || subfieldId.length() == 0) {
                subfieldId = "a"; // fallback
            }
            attrs.addAttribute(nsUri, CODE, CODE, "CDATA", subfieldId);
            if (contentHandler != null) {
                contentHandler.startElement(nsUri, SUBFIELD, SUBFIELD, attrs);
            }
            if (listener != null) {
                listener.beginSubField(designator);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void endSubField(Field designator) {
        if (designator == null) {
            return;
        }
        try {
            if (listener != null) {
                listener.endSubField(designator);
            }
            if (designator != null) {
                if (contentHandler != null) {
                    String value = designator.data();
                    if (!value.isEmpty()) {
                        value = normalizeValue(value);
                        contentHandler.characters(value.toCharArray(), 0, value.length());
                    }
                }
            }
            if (contentHandler != null) {
                contentHandler.endElement(NS_URI, SUBFIELD, SUBFIELD);
            }
        } catch (Exception ex) {
            if (fatalerrors) {
                throw new RuntimeException(ex);
            } else if (!silenterrors) {
                logger.warn(designator + ": " + ex.getMessage(), ex);
            }
        }
    }

    protected String normalizeValue(String value) {
        return XMLUtil.clean(Normalizer.normalize(value, Form.NFC));
    }

    private class Iso2709StreamListener implements CharStreamListener {

        @Override
        public void data(String data) {
            String fieldContent = data;
            try {
                switch (mark) {
                    case Separable.FS: // start/end file
                        break;
                    case Separable.GS: // start/end of group within a stream
                        if (subfieldOpen) { // close subfield if open
                            subfieldOpen = false;
                            endDataField(null);
                        }
                        endDataField(designator);
                        endRecord(); // close record
                    // fall through is ok
                    case '\u0000': // start of stream
                        position = 0;
                        // skip line-feed (OCLC PICA quirk)
                        if (data.charAt(0) == '\n') {
                            fieldContent = data.substring(1);
                        }
                        if (fieldContent.length() >= RecordLabel.LENGTH) {
                            beginRecord(format, type);
                            String labelStr = fieldContent.substring(0, RecordLabel.LENGTH);
                            label = new RecordLabel(labelStr.toCharArray());
                            // auto-repair label
                            leader(label.getFixed());
                            directory = new FieldDirectory(label, fieldContent);
                            if (directory.isEmpty()) {
                                designator = new Field(label, fieldContent.substring(RecordLabel.LENGTH));
                                if (designator.tag() != null) {
                                    beginDataField(designator);
                                }
                            }
                        } else {
                            directory = new FieldDirectory(label, fieldContent);
                            designator = new Field(label);
                        }
                        break;
                    case Separable.RS:
                        if (subfieldOpen) {
                            subfieldOpen = false;
                            endDataField(null); // force data field close
                        } else if (designator != null && !designator.isEmpty()) {
                            if (datafieldOpen) {
                                endDataField(designator);
                            }
                        }
                        if (directory != null && directory.containsKey(position)) {
                            designator = new Field(label, directory.get(position), fieldContent, false);
                        } else {
                            // repair field content if too short
                            if (fieldContent.length() < 3) {
                                fieldContent = designator.tag() + fieldContent;
                            }
                            designator = new Field(label, fieldContent);
                        }
                        if (designator != null) {
                            beginDataField(designator);
                        }
                        break;
                    case Separable.US:
                        if (!subfieldOpen) {
                            subfieldOpen = true;
                            beginDataField(designator);
                        }
                        if (designator != null) {
                            designator = new Field(label, designator, fieldContent, true);
                            beginSubField(designator);
                        }
                        endSubField(designator);
                        break;
                }
            } catch (FieldDirectoryException ex) {
                logger.warn(ex.getMessage(), ex);
            } finally {
                position += data.length();
            }
        }

        @Override
        public void markUnit() {
            mark = Separable.US;
            position++;
        }

        @Override
        public void markRecord() {
            mark = Separable.RS;
            position++;
        }

        @Override
        public void markGroup() {
            mark = Separable.GS;
            position++;
        }

        @Override
        public void markFile() {
            mark = Separable.FS;
            position++;
            endDataField(null);
            endRecord();
        }
    }
}
