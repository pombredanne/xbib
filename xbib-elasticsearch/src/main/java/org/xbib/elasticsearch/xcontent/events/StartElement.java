package org.xbib.elasticsearch.xcontent.events;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public final class StartElement extends SAXEvent {

    private final String uri;
    private final String localName;
    private final String qName;
    private final Attributes atts;

    public StartElement(String uri, String localName, String qName, Attributes atts) {
        super(EventType.START_ELEMENT);
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
        this.atts = new AttributesImpl(atts);
    }

    @Override
    public void send(ContentHandler contentHandler) throws SAXException {
        contentHandler.startElement(uri, localName, qName, atts);
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

    public Attributes getAtts() {
        return atts;
    }

}
