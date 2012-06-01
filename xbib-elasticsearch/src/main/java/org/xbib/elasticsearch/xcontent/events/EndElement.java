package org.xbib.elasticsearch.xcontent.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class EndElement extends SAXEvent {

    private final String uri;
    private final String localName;
    private final String qName;

    public EndElement(String uri, String localName, String qName) {
        super(EventType.END_ELEMENT);
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
    }

    @Override
    public void send(ContentHandler contentHandler) throws SAXException {
        contentHandler.endElement(uri, localName, qName);
    }

    public String getURI() {
        return uri;
    }

    public String getLocalName() {
        return localName;
    }

    public String getQName() {
        return qName;
    }
}
