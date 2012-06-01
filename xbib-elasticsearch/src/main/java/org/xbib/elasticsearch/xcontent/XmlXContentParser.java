package org.xbib.elasticsearch.xcontent;

import java.io.IOException;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.support.AbstractXContentParser;

public class XmlXContentParser extends AbstractXContentParser implements XContentParser {

    final XmlParser parser;
    
    public XmlXContentParser(XmlParser parser) {
        this.parser = parser;
    }
    
    @Override
    public XContentType contentType() {
        return XContentType.JSON;
    }

    @Override
    public Token nextToken() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean doBooleanValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected short doShortValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected int doIntValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected long doLongValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected float doFloatValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected double doDoubleValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void skipChildren() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Token currentToken() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String currentName() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String text() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasTextCharacters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public char[] textCharacters() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int textLength() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int textOffset() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Number numberValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NumberType numberType() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean estimatedNumberType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] binaryValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
