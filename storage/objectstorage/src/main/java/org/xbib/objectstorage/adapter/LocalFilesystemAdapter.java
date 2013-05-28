package org.xbib.objectstorage.adapter;

import org.xbib.objectstorage.Adapter;
import org.xbib.objectstorage.ObjectStorageRequest;
import org.xbib.objectstorage.adapter.container.LocalFileTransferContainer;
import org.xbib.objectstorage.adapter.container.request.FileRequest;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.io.IOException;
import java.net.URI;

public class LocalFilesystemAdapter implements Adapter {

    public LocalFilesystemAdapter() {
        init();
        addContainer(new LocalFileTransferContainer(getDefaultContainerName(),
                URI.create(null),
                "Default container",
                null));
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
        return URI.create("http://xbib.org/objectstorage/localfilesystem");
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
