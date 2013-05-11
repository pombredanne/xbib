package org.xbib.io.ftp.sauron;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.xbib.io.Session;
import org.xbib.io.StringPacket;

public class FTPSession implements Session<StringPacket> {

    private URI uri;
    private FTPClient client;
    private boolean open;

    public FTPSession(URI uri) {
        this.uri = uri;
    }

    @Override
    public void open(Mode mode) throws IOException {
        this.client = new FTPClient();
        try {
            client.connect(uri.getHost());
            String userInfo = uri.getUserInfo();
            String[] auth = userInfo.split(":");
            client.login(auth[0], auth[1]);
        } catch (IllegalStateException | IOException | FTPIllegalReplyException | FTPException ex) {
            throw new IOException(ex);
        }
        this.open = true;
    }

    @Override
    public void close() throws IOException {
        try {
            if (client != null) {
                client.disconnect(true);
            }
        } catch (IllegalStateException | IOException | FTPIllegalReplyException | FTPException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public StringPacket newPacket() {
        return null;
    }

    @Override
    public StringPacket read() throws IOException {
        return null;
    }

    @Override
    public void write(StringPacket packet) throws IOException {
    }

    public String[] list(String path) throws IOException {
        try {
            if (client != null) {
                client.changeDirectory(path);
                return client.listNames();
            } else {
                return null;
            }
        } catch (IllegalStateException | IOException | FTPIllegalReplyException |
                FTPDataTransferException | FTPAbortedException |
                FTPListParseException | FTPException ex) {
            throw new IOException(ex);
        }      
    }

    public void download(String name, OutputStream out) throws IOException {
        try {
            if (client != null) {
                client.download(name, out, 0, null);
            }
        } catch (IllegalStateException | IOException | FTPIllegalReplyException |
                FTPDataTransferException | FTPAbortedException | FTPException ex) {
            throw new IOException(ex);
        }
    }
}
