package org.xbib.elasticsearch.xcontent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.xml.namespace.QName;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentGenerator;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;

public class XmlXContent implements XContent {
    
    public XmlXContent() {
    }
    
    @Override
    public XContentType type() {
        return XContentType.JSON; // wrong
    }

    @Override
    public byte streamSeparator() {
        return '\n';
    }
    
    @Override
    public XContentGenerator createGenerator(OutputStream os) throws IOException {
        return new XmlXContentGenerator(new XmlGenerator(os));
    }

    @Override
    public XContentGenerator createGenerator(Writer writer) throws IOException {
        return new XmlXContentGenerator(new XmlGenerator(writer));
    }

    @Override
    public XContentParser createParser(String content) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public XContentParser createParser(InputStream is) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public XContentParser createParser(byte[] data) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public XContentParser createParser(byte[] data, int offset, int length) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public XContentParser createParser(Reader reader) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
