package org.xbib.search;

import java.io.IOException;

public class NotFoundError extends IOException {

    public NotFoundError() {
        super();
    }

    public NotFoundError(String message) {
        super(message);
    }
}
