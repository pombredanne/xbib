package org.xbib.oai;

import java.net.URI;
import org.xbib.io.http.netty.HttpRequest;
import org.xbib.oai.client.OAIClient;

public class OAIRequest extends HttpRequest {

    public OAIRequest(URI uri) {
        super("GET");
        setURI(uri);
        addHeader("User-Agent", OAIClient.USER_AGENT);
    }

    @Override
    public OAIRequest addParameter(String name, String value) {
        if (value != null && value.length() > 0) {
            super.addParameter(name, value);
        }
        return this;
    }
    
}
