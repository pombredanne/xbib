package org.xbib.elasticsearch.xcontent.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class StartDocument extends SAXEvent {

    public static final StartDocument SINGLETON = new StartDocument();

    public StartDocument() {
        super(EventType.START_DOCUMENT);
    }

    @Override
    public void send(ContentHandler contentHandler) throws SAXException {
        contentHandler.startDocument();
    }

}
