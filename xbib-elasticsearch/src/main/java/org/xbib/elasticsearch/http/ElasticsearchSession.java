package org.xbib.elasticsearch.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Properties;
import org.xbib.io.http.netty.HttpSession;
import org.xbib.io.util.URIUtil;

/**
 *  Elasticsearch HTTP session
 *
 */
public class ElasticsearchSession extends HttpSession {

    private URI uri;
    private String host;
    private int port;
    private String index;
    private String type;

    public ElasticsearchSession(URI baseURI) throws UnsupportedEncodingException {
        Properties p = URIUtil.getPropertiesFromURI(baseURI);        
        // get this from URI
        this.host = p.getProperty("host");
        this.port = Integer.parseInt(p.getProperty("port", "9200"));
        this.index = p.getProperty("index");
        this.type = p.getProperty("type");
        String view = p.getProperty("fragment");
        if (view == null) {
            view = p.getProperty("view");
        }
        if (view != null && view.length() > 0) {
            String[] s = view.split("/");
            switch (s.length) {
                case 1:
                    this.index = s[0];
                    break;
                case 2:
                    this.index = s[0];
                    this.type = s[1];
                    break;
            }
        }
        // build the Elasticsearch query HTTP URL
        StringBuilder urlStr = new StringBuilder();
        urlStr.append("http://").append(host);
        if (port > 0) {
            urlStr.append(':').append(port);
        }
        if (index.length() > 0) {
            urlStr.append('/').append(index);
        }
        if (type.length() > 0) {
            urlStr.append('/').append(type);
        }
        this.uri = URI.create(urlStr.toString());
    }
    
    public URI getURI() {
        return uri;
    }

}
