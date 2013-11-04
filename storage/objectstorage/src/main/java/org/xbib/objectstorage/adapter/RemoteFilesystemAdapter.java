package org.xbib.objectstorage.adapter;

import org.xbib.jersey.filter.PasswordSecurityContext;
import org.xbib.objectstorage.API;
import org.xbib.objectstorage.Adapter;
import org.xbib.objectstorage.Container;
import org.xbib.objectstorage.container.RemoteFileTransferContainer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

public class RemoteFilesystemAdapter extends PropertiesAdapter {

    private ResourceBundle bundle;

    @Override
    public Adapter init() {
        return this;
    }

    @Override
    public Container connect(String containerName, PasswordSecurityContext securityContext, URI baseURI) throws IOException {
        if (baseURI == null) {
            throw new IOException("base URI is null");
        }
        this.bundle = ResourceBundle.getBundle(containerName);
        try {
            baseURI = baseURI.resolve(API.VERSION);
            URI uri = new URI(baseURI.getScheme(),
                securityContext.getUser() + ":" + securityContext.getPassword(),
                baseURI.getHost(),
                baseURI.getPort(),
                containerName + "/" + baseURI.getPath(),
                baseURI.getQuery(),
                baseURI.getFragment()
            );
            return new RemoteFileTransferContainer(uri, bundle);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public void disconnect(Container container) throws IOException {
    }

    @Override
    public URI getAdapterURI() {
        return URI.create("http://xbib.org/objectstorage/remotefilesystem");
    }

}
