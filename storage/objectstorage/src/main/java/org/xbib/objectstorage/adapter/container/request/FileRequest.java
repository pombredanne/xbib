package org.xbib.objectstorage.adapter.container.request;

import org.xbib.objectstorage.adapter.UserAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileRequest extends AbstractRequest {

    @Override
    public UserAttributes getUserAttributes() throws IOException {
        return new UserAttributes() {
            Map<String, String> attributes = new HashMap();

            @Override
            public String getName() {
                return getUser();
            }

            @Override
            public Map<String, String> getAttributes() {
                return attributes;
            }
        };
    }
}
