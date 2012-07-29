package org.xbib.objectstorage.adapter;

import java.io.IOException;
import java.net.URI;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import org.xbib.objectstorage.ObjectStorageRequest;
import org.xbib.objectstorage.adapter.container.DefaultContainer;

public class DefaultAdapter extends AbstractAdapter {

    public DefaultAdapter() {
        init();
        addContainer(new DefaultContainer(getDefaultContainerName(), "Default container", null));
    }
    
    @Override
    public String getRoot() {
        return "/tmp";
    }

    @Override
    public String getDriverClassName() {
        return null; // no connection
    }

    @Override
    public String getConnectionSpec() {
        return null; // no connection
    }

    @Override
    public String getUser() {
        return null; // no connection
    }
    @Override
    public String getPassword() {
        return null; // no connection
    }
    
    
    @Override
    public String getStatementBundleName() {
        return null;
    }

    @Override
    public String getDefaultContainerName() {
        return "default";
    }

    @Override
    public URI getAdapterURI() {
        return URI.create("http://xbib.org/objectstorage/default");
    }

    @Override
    public DirContext getDirContext() throws NamingException {
        return null;
    }

    @Override
    public ObjectStorageRequest newRequest() throws IOException {
        return new FileRequest();
    }

}
