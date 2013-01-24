package org.xbib.oai.exceptions;

public class BadArgumentException extends OAIException {
     
    public BadArgumentException() {
        this(null);
    }

    public BadArgumentException(String message) {
        super(message);
    }   
}
