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

import java.util.ListIterator;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.XMLEvent;

public final class XMLEventIterator implements XMLEventReader, ListIterator {

    private final ListIterator<XMLEvent> iterator;

    public XMLEventIterator(ListIterator<XMLEvent> iterator) {
        this.iterator = iterator;
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public String getElementText() {
        StringBuilder sb = new StringBuilder();
        XMLEvent event;
        do {
            event = nextEvent();
            if (event.isCharacters()) {
                sb.append(event.asCharacters().getData());
            }
        } while (event instanceof Comment || event.isCharacters());
        previous();
        return sb.toString();
    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public XMLEvent peek() {
        if (hasNext()) {
            try {
                return nextEvent();
            } finally {
                previous();
            }
        }
        return null;
    }

    @Override
    public XMLEvent nextEvent() {
        return iterator.next();
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        XMLEvent event;
        do {
            event = nextEvent();
            if (event.isStartElement() || event.isEndElement()) {
                return event;
            }
        } while (event instanceof Comment || event.isCharacters()
                && event.asCharacters().isIgnorableWhiteSpace());
        throw new XMLStreamException("no tag found: " + event);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return iterator.hasPrevious();
    }

    @Override
    public int nextIndex() {
        return iterator.nextIndex();
    }

    @Override
    public int previousIndex() {
        return iterator.previousIndex();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public XMLEvent next() {
        return nextEvent();
    }

    @Override
    public XMLEvent previous() {
         return previousEvent();
   }

    @Override
    public void set(Object e) {
        iterator.set((XMLEvent)e);
    }

    @Override
    public void add(Object e) {
        iterator.add((XMLEvent)e);
    }

    public XMLEvent previousEvent() {
        return iterator.previous();
    }
    
}