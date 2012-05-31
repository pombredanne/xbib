package org.xbib.io.http.netty;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import java.net.URI;

public class HttpResult {
    
    private URI uri;
    private int statusCode;
    private FluentCaseInsensitiveStringsMap headers;
    private String body;
    private Throwable throwable;

    public HttpResult() {
    }
    
    public void setURI(URI uri) {
        this.uri = uri;
    }
    
    public URI getURI() {
        return uri;
    }
    
    public HttpResult setStatusCode(int code) {
        this.statusCode = code;
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }
    
    public HttpResult setHeaders(FluentCaseInsensitiveStringsMap headers) {
        this.headers = headers;
        return this;
    }
    
    public FluentCaseInsensitiveStringsMap getHeaders() {
        return headers;
    }
    
    public HttpResult setBody(String body) {
        this.body = body;
        return this;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
    
    public Throwable getThrowable() {
        return throwable;
    }
    
    public boolean ok() {
       return getStatusCode() == 200 && getThrowable() == null;
    }
}
