package org.xbib.io.http.rest;

import java.nio.charset.Charset;

/**
 * Encapsulates form-data sent to web services.
 * Currently only application/x-www-form-urlencoded is supported.
 */
public class FormContent extends Content {
    protected String rawQuery;

    public FormContent(String query) {
        super("application/x-www-form-urlencoded", getBytes(query));
        // strictly speaking US ASCII should be used
    }

    private static byte[] getBytes(String query) {
        return query.getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public String toString() {
        return rawQuery;
    }
}
