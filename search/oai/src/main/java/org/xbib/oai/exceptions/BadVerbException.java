package org.xbib.oai.exceptions;

public class BadVerbException extends OAIException {
     
    public BadVerbException() {
        this(null);
    }

    public BadVerbException(String message) {
        super(message);
    }   
}
