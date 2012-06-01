package org.xbib.sru;

import java.net.URI;
import org.xbib.io.Request;

public class AbstractSRURequest implements Request {

    private URI uri;
    private String username;
    private String password;
    
    public AbstractSRURequest setURI(URI uri) {
        this.uri = uri;
        return this;
    }
    
    public URI getURI() {
        return uri;
    }
   
    public AbstractSRURequest setUsername(String username) {
        this.username = username;
        return this;
    }
    
    public String getUsername() {
        return username;
    }

    public AbstractSRURequest setPassword(String password) {
        this.password = password;
        return this;
    }
    
    public String getPassword() {
        return password;
    }    
}


