package org.xbib.io.http.netty;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpRequest extends HttpPacket {

    private String method;
    private URI uri;
    private AsyncHttpClientConfig.Builder config;
    private RequestBuilder builder;
    private Realm.RealmBuilder realmBuilder;

    public HttpRequest(String method) {
        this.method = method;
        this.config = new AsyncHttpClientConfig.Builder().setRequestTimeoutInMs(30000).setAllowPoolingConnection(true).setFollowRedirects(true).setMaxRequestRetry(1).setCompressionEnabled(true);       
        this.builder = new RequestBuilder(method);
        this.realmBuilder = new Realm.RealmBuilder();
    }
    
    public HttpRequest setURI(URI fullUri) {
        if (fullUri.getUserInfo() != null) {
            String[] userInfo = fullUri.getUserInfo().split(":");
            realmBuilder = realmBuilder.setPrincipal(userInfo[0]).setPassword(userInfo[1]).setUsePreemptiveAuth(true).setScheme(AuthScheme.BASIC);
        }
        String authority = fullUri.getHost() + (fullUri.getPort() > 0 ? ":" + fullUri.getPort() : "");
        try {
            this.uri = new URI(fullUri.getScheme(), authority, fullUri.getPath(), fullUri.getQuery(), fullUri.getFragment());
        } catch (URISyntaxException ex) {
            // ignore
        }
        return this;
    }
    
    public String getMethod() {
        return method;
    }
    
    public URI getURI() {
        return uri;
    }

    public HttpRequest addParameter(String name, String value) {
        if (value != null && value.length() > 0) {
            builder.addQueryParameter(name, value);
        }
        return this;
    }

    public HttpRequest addHeader(String name, String value) {
        if (value != null && value.length() > 0) {
            builder.addHeader(name, value);
        }
        return this;
    }

    public HttpRequest setBody(String body) {
        builder.setBody(body);
        return this;
    }

    public HttpRequest setProxy(String host, int port) {
        if (host != null) {
            config.setProxyServer(new ProxyServer(host, port));
        }
        return this;
    }

    public HttpRequest setTimeout(int millis) {
        if (millis > 0) {
            config.setRequestTimeoutInMs(millis);
        }
        return this;
    }
    
    public HttpRequest setUser(String user) {
        realmBuilder = realmBuilder.setPrincipal(user);
        return this;
    }
    
    public HttpRequest setPassword(String password) {
        realmBuilder = realmBuilder.setPassword(password);
        return this;
    }
    
    public AsyncHttpClient buildClient() {        
        AsyncHttpClient client = new AsyncHttpClient(new NettyAsyncHttpProvider(config.build()));
        return client;
    }
    
    public Request buildRequest() {
        return builder.setUrl(uri.toASCIIString()).setRealm(realmBuilder.build()).build();
    }

}
