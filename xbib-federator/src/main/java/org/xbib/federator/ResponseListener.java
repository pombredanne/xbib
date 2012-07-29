package org.xbib.federator;

public interface ResponseListener<T> {

    void onResponse(T response);
}
