package org.xbib.sru.client;

import org.xbib.io.http.netty.HttpResponse;
import org.xbib.io.http.netty.HttpResponseListener;

public interface SRUHttpResponseListener extends HttpResponseListener {

    HttpResponse getResponse();

    void close();
}
