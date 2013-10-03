package org.xbib.io.json;

/**
 * The JSONException is thrown by the JSON.org classes
 */
public class JSONException extends Exception {

    /**
     * Constructs a JSONException with an explanatory message.
     *
     * @param message Detail about the reason for the exception.
     */
    public JSONException(String message) {
        super(message);
    }

    public JSONException(Throwable t) {
        super(t.getMessage());
    }

}
