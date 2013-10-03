
package org.xbib.io.http.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.Charset;


/**
 * Abstract base class of the content being sent to a server.
 * Check Resty.content(...) methods to create objects
 */
public abstract class AbstractContent {
    public static final byte[] CRLF = {'\r', '\n'};

    public abstract void writeHeader(OutputStream os) throws IOException;

    public abstract void writeContent(OutputStream os) throws IOException;

    protected abstract void addContent(URLConnection con) throws IOException;

    protected byte[] ascii(String string) {
        return string.getBytes(Charset.forName("us-ascii"));
    }

    protected String enc(String aString) {
        return EncoderUtil.encodeIfNecessary(aString, EncoderUtil.Usage.TEXT_TOKEN, 0);
    }
}
