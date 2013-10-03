package org.xbib.io.http.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * Used for deleting contents on a server with the DELETE method.
 * Use Resty.delete() to create an instance of this.
 * Not strictly a content, but an instruction to delete a URL.
 */
public class Deletion extends AbstractContent {

    @Override
    public void writeHeader(OutputStream os) throws IOException {
    }

    @Override
    public void writeContent(OutputStream os) throws IOException {
    }

    @Override
    protected void addContent(URLConnection con) throws IOException {
        con.setDoOutput(true);
        ((HttpURLConnection) con).setRequestMethod("DELETE");
    }

}
