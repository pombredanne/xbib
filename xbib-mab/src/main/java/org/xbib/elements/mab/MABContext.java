package org.xbib.elements.mab;

import org.xbib.elements.items.LiaContext;

public class MABContext extends LiaContext {

    private String format;
    private boolean continuing;
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setContinuing(boolean continuing) {
        this.continuing = continuing;
    }
    
    public boolean getContinuing() {
        return continuing;
    }
    
    @Override
    public void clear() {
        super.clear();
    }
    
}
