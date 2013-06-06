package org.xbib.io.ftp.apache;

import org.apache.commons.net.ftp.FTPClient;
import org.xbib.io.Session;
import org.xbib.io.StringPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

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
        client.connect(uri.getHost());
        String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            String[] auth = userInfo.split(":");
            client.login(auth[0], auth[1]);
        }
        this.open = true;
    }

    @Override
    public void close() throws IOException {
        if (client != null) {
            client.disconnect();
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
        if (client != null) {
            return client.listNames(path);
        } else {
            return null;
        }
    }

    public boolean exists(String path) throws IOException {
        if (client != null) {
            return client.listFiles(path).length > 0;
        } else {
            return false;
        }
    }

    public void mkdir(String path) throws IOException {
        if (client == null) {
            return;
        } else {
            String[] s = path.split("/");
            if (s.length == 1) {
                if (s[0].length() > 0) {
                    client.makeDirectory(s[0]);
                    client.changeWorkingDirectory(s[0]);
                }
            } else {
                if (s[0].length() > 0) {
                    client.makeDirectory(s[0]);
                    client.changeWorkingDirectory(s[0]);
                }
                mkdir(path.substring(path.indexOf('/')+1));
            }
        }
    }

    public void upload(InputStream in, String fileName) throws IOException {
        if (client != null) {
            client.storeFile(fileName, in);
        }
    }

    public void download(String fileName, OutputStream out) throws IOException {
        if (client != null) {
            client.retrieveFile(fileName, out);
        }
    }
}
