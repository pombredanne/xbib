package org.xbib.oai.util;

import java.util.Date;

public class RecordHeader {

    private String identifier;
    
    private Date datestamp;
    
    private String set;
    
    public RecordHeader setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public RecordHeader setDatestamp(Date datestamp) {
        this.datestamp = datestamp;
        return this;
    }
    
    public Date getDatestamp() {
        return datestamp;
    }
    
    public RecordHeader setSetspec(String setSpec) {
        this.set = setSpec;
        return this;
    }
    
    public String getSetSpec() {
        return set;
    }
}
