package org.xbib.json;

import com.fasterxml.jackson.core.JsonToken;

public interface JsonConsumer {

    void add(JsonToken token);
}
