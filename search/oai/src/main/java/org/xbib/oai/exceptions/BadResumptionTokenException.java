package org.xbib.oai.exceptions;

import org.xbib.oai.ResumptionToken;

public class BadResumptionTokenException extends OAIException {
     
    public BadResumptionTokenException() {
        this(null);
    }

    public BadResumptionTokenException(ResumptionToken token) {
        super(token != null? token.toString() : null);
    }   
}
