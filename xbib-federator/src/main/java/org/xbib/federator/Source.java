package org.xbib.federator;

import java.net.URI;

public interface Source {
    
    URI getURI();

    String getUser();
    
    String getPassword();
    
    String getQueryType();
    
    String getQuery();
    
    long getTimeoutMillis();

    boolean ping();
    
    long lastActive();
    
}
