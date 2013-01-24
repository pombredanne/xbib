package org.xbib.oai.exceptions;

public class NoSetHierarchyException extends OAIException {
    
    public NoSetHierarchyException() {
        this(null);
    }

    public NoSetHierarchyException(String message) {
        super(message);
    }

}
