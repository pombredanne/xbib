package org.xbib.oai.exceptions;


public class IdDoesNotExistException extends OAIException {
    
    public IdDoesNotExistException() {
        this(null);
    }

    public IdDoesNotExistException(String message) {
        super(message);
    }

}
