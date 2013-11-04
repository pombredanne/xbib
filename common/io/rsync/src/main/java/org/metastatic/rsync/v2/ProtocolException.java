
package org.metastatic.rsync.v2;

/**
 * Signals an error in the rsync protocol.
 */
public class ProtocolException extends java.io.IOException {

    public ProtocolException() {
        super();
    }

    public ProtocolException(String msg) {
        super(msg);
    }
}
