package org.xbib.oai.exceptions;

public class CannotDisseminateFormatException extends OAIException {
    
    public CannotDisseminateFormatException() {
        this(null);
    }

    public CannotDisseminateFormatException(String message) {
        super(message);
    }

}
