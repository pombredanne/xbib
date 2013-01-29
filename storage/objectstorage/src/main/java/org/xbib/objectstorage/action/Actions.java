package org.xbib.objectstorage.action;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.xbib.objectstorage.Action;
import org.xbib.objectstorage.ObjectStorageRequest;
import org.xbib.objectstorage.ObjectStorageResponse;

public class Actions implements Action {
    
    Action[] actions;
    
    public Actions(Action... actions) {
        this.actions = actions;
    }
    
    @Override
    public void execute(ObjectStorageRequest request, ObjectStorageResponse response) throws Exception {
        for (Action a : actions) {
            a.execute(request, response);
        }
    }
    
    @Override
    public Action waitFor(long l, TimeUnit tu) throws IOException {
        return this;
    }
}
