package org.xbib.elasticsearch.xcontent.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

public final class EndDocument extends SAXEvent {

    public static final EndDocument SINGLETON = new EndDocument();

    public EndDocument() {
        super(EventType.END_DOCUMENT);
    }

    @Override
    public void send(ContentHandler contentHandler) throws SAXException {
        contentHandler.endDocument();
    }

    public void send(ContentHandler contentHandler, LocatorImpl locator) throws SAXException {
        send(contentHandler);
    }
}
