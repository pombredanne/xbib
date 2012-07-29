package org.xbib.objectstorage.adapter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.xbib.objectstorage.Container;
import org.xbib.objectstorage.ObjectStorageRequest;

public class FileRequest extends AbstractRequest {

    @Override
    public UserAttributes getUserAttributes() throws IOException {
        return new UserAttributes() {

            @Override
            public String getName() {
                return getUser();
            }
            
        };
    }


}
