package org.xbib.federator;

import java.io.IOException;
import java.io.OutputStream;

public interface Federator {

    Federator setMimeType(String mimetype);    
        
    Federator setTarget(OutputStream target);

    Federator setQuery(String query);
    
    Federator search() throws IOException;    
    
    OutputStream getTarget();
     
    
}
