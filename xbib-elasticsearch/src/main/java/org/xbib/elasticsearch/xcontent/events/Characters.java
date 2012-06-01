package org.xbib.elasticsearch.xcontent.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class Characters extends SAXEvent {

    private final char[] ch;

    public Characters(String string) {
        super(EventType.CHARACTERS);
        this.ch = string.toCharArray();
    }

    public Characters(char[] ch, int start, int length) {
        super(EventType.CHARACTERS);
        this.ch = new char[length];
        System.arraycopy(ch, start, this.ch, 0, length);
    }
    
    @Override
    public void send(ContentHandler contentHandler) throws SAXException {
        contentHandler.characters(ch, 0, ch.length);
    }

    @Override
    public String toString() {
        return new String(ch);
    }

    public void append(StringBuffer buffer) {
        buffer.append(ch);
    }

    public char[] getCh() {
        return ch;
    }
}
