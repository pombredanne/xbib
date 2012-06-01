package org.xbib.oai;

import org.xbib.xml.transform.XMLFilterReader;

public class MetadataReader extends XMLFilterReader {
    
    RecordHeader header;
    
    public void setHeader(RecordHeader header) {
        this.header = header;
    }
    
    public RecordHeader getHeader() {
        return header;
    }
    
}
