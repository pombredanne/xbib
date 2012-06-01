/*
 * This file is part of citygml4j.
 * Copyright (c) 2007 - 2010
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.igg.tu-berlin.de/
 *
 * The citygml4j library is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see 
 * <http://www.gnu.org/licenses/>.
 */
package org.xbib.elasticsearch.xcontent.events;

import org.xbib.elasticsearch.xcontent.events.SAXEvent.EventType;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.NamespaceSupport;

public class SAXEventBuffer implements ContentHandler {

    final NamespaceSupport namespaces;
    final boolean trackLocation;
    private LocatorImpl locator;
    private SAXEvent head;
    private SAXEvent tail;
    private EventType lastElementEvent = EventType.END_ELEMENT;
    private Stack<StartElement> parentStartElements;
    private StartElement lastStartElement;

    public SAXEventBuffer() {
        this(false);
    }

    public SAXEventBuffer(boolean trackLocation) {
        this.trackLocation = trackLocation;
        namespaces = new NamespaceSupport();
        locator = trackLocation ? new LocatorImpl() : null;
        parentStartElements = new Stack<>();
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        //
    }

    public void updateLocation(int lineNumber, int columnNumber, String systemId, String publicId) {
        locator.setLineNumber(lineNumber);
        locator.setColumnNumber(columnNumber);
        locator.setSystemId(systemId);
        locator.setPublicId(publicId);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        // we do not record this event
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (lastElementEvent == EventType.START_ELEMENT) {
            addEvent(new Characters(ch, start, length));
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // we do not record this event
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        // we do not record this event
    }

    @Override
    public void startDocument() throws SAXException {
        addEvent(new StartDocument());
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        StartElement element = new StartElement(uri, localName, qName, atts);
        if (lastElementEvent == EventType.START_ELEMENT) {
            parentStartElements.push(lastStartElement);
            tail = lastStartElement;
        }

        addEvent(element);
        lastStartElement = element;
        lastElementEvent = EventType.START_ELEMENT;
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        namespaces.pushContext();
        namespaces.declarePrefix(prefix, uri);
        addEvent(new StartPrefixMapping(prefix, uri));
    }

    @Override
    public void endDocument() throws SAXException {
        addEvent(new EndDocument());
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (lastElementEvent == EventType.END_ELEMENT) {
            parentStartElements.pop();
        }

        addEvent(new EndElement(uri, localName, qName));
        lastElementEvent = EventType.END_ELEMENT;
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        namespaces.popContext();
        addEvent(new EndPrefixMapping(prefix));
    }

    public boolean isEmpty() {
        return head == null;
    }

    public void clear() {
        head = tail = null;
        namespaces.reset();
        locator = trackLocation ? new LocatorImpl() : null;
        lastElementEvent = EventType.END_ELEMENT;
        parentStartElements = new Stack<>();
        lastStartElement = null;
    }

    public void addEvent(SAXEvent event) {
        if (!isEmpty()) {
            tail.setNext(event);
            tail = event;
        } else {
            head = tail = event;
        }
    }

    public void append(SAXEventBuffer other) {
        if (other.isEmpty()) {
            return;
        }

        if (!isEmpty()) {
            addEvent(other.head);
            tail = other.tail;
        } else {
            head = other.head;
            tail = other.tail;
        }
    }

    public SAXEvent getFirstEvent() {
        return head;
    }

    public SAXEvent getFirstEvent(EventType type) {
        if (!isEmpty()) {
            SAXEvent event = head;
            do {
                if (event.getType() == type) {
                    return event;
                }
            } while ((event = event.next()) != null);
        }

        return null;
    }

    public void removeFirstEvent() {
        head = head.next();
    }

    public SAXEvent getLastEvent() {
        return tail;
    }

    public StartElement getFirstStartElement() {
        return (StartElement) getFirstEvent(EventType.START_ELEMENT);
    }

    public StartElement getLastStartElement() {
        return lastStartElement;
    }

    public StartElement getParentStartElement() {
        return parentStartElements.peek();
    }

    private static final class StackItem<T> {

        private final T value;
        private StackItem<T> next;

        StackItem(T value) {
            this.value = value;
        }
    }

    private static final class Stack<T> {

        private StackItem<T> head;

        void push(T item) {
            StackItem<T> tmp = new StackItem<T>(item);
            if (!isEmpty()) {
                tmp.next = head;
                head = tmp;
            } else {
                head = tmp;
            }
        }

        T pop() {
            if (!isEmpty()) {
                StackItem<T> tmp = head;
                head = head.next;
                return tmp.value;
            }

            return null;
        }

        T peek() {
            return !isEmpty() ? head.value : null;
        }

        boolean isEmpty() {
            return head == null;
        }
    }
}
