package org.xbib.oai.exceptions;

public class NoRecordsMatchException extends OAIException {
    
    public NoRecordsMatchException() {
        this(null);
    }

    public NoRecordsMatchException(String message) {
        super(message);
    }

}
