package org.xbib.elasticsearch.xcontent.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract class SAXEvent {

    private final EventType type;
    private SAXEvent next;

    public enum EventType {

        CHARACTERS,
        END_DOCUMENT,
        END_ELEMENT,
        END_PREFIX_MAPPING,
        START_DOCUMENT,
        START_ELEMENT,
        START_PREFIX_MAPPING
    }

    public SAXEvent(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    public SAXEvent next() {
        return next;
    }

    public void setNext(SAXEvent next) {
        this.next = next;
    }

    public abstract void send(ContentHandler contentHandler) throws SAXException;
}
