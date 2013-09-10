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
package org.xbib.standardnumber;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class ISBNRangeMessageConfigurator {

    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private static final String ISBN_RANGE_MESSAGE_RESOURCE = "/org/xbib/standardnumber/RangeMessage.xml";
    private Stack<StringBuilder> content = new Stack();
    private List<String> ranges = new ArrayList();
    private String messageDate;
    private String prefix = null;
    private String rangeBegin = null;
    private String rangeEnd = null;
    private int length = 0;
    private boolean valid = false;

    static {
        // name space is aware by default, but we set it to true anyway
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
    }

    public ISBNRangeMessageConfigurator() throws IOException {
        InputStream in = getClass().getResourceAsStream(ISBN_RANGE_MESSAGE_RESOURCE);
        try {
            XMLEventReader xmlReader = xmlInputFactory.createXMLEventReader(in);
            while (xmlReader.hasNext()) {
                processEvent(xmlReader.peek());
                xmlReader.nextEvent();
            }
        } catch (XMLStreamException e) {
            throw new IOException(e.getMessage());
        }
    }

    public List<String> getRanges() {
        return ranges;
    }

    public String getMessageDate() {
        return messageDate;
    }

    private void processEvent(XMLEvent e) {
        switch (e.getEventType()) {
            case XMLEvent.START_ELEMENT: {
                StartElement element = e.asStartElement();
                String name = element.getName().getLocalPart();
                if ("RegistrationGroups".equals(name)) {
                    valid = true;
                }
                content.push(new StringBuilder());
                break;
            }
            case XMLEvent.END_ELEMENT: {
                EndElement element = e.asEndElement();
                String name = element.getName().getLocalPart();
                String v = content.pop().toString();
                if ("MessageDate".equals(name)) {
                    this.messageDate = v;
                }
                if ("Prefix".equals(name)) {
                    prefix = v;
                }
                if ("Range".equals(name)) {
                    int pos = v.indexOf('-');
                    if (pos > 0) {
                        rangeBegin = v.substring(0, pos);
                        rangeEnd = v.substring(pos + 1);
                    }
                }
                if ("Length".equals(name)) {
                    length = Integer.parseInt(v);
                }
                if ("Rule".equals(name)) {
                    if (valid && rangeBegin != null && rangeEnd != null) {
                        if (length > 0) {
                            ranges.add(prefix + "-" + rangeBegin.substring(0, length));
                            ranges.add(prefix + "-" + rangeEnd.substring(0, length));
                        }
                    }
                }
                break;
            }
            case XMLEvent.CHARACTERS: {
                Characters c = (Characters) e;
                if (!c.isIgnorableWhiteSpace()) {
                    String text = c.getData().trim();
                    if (text.length() > 0 && !content.empty()) {
                        content.peek().append(text);
                    }
                }
                break;
            }
        }
    }
}
