package org.xbib.applet.client;


import java.net.URL;
import org.xbib.applet.util.Base64;

public abstract class AbstractClient {

    protected static int CONNECT_TIMEOUT = 15000;
    protected static int READ_TIMEOUT = 15000;
    protected static String ACCEPT = "*/*";
    protected URL documentBase;
    protected String path;
    protected String sessionID;
    protected String authorization;

    public AbstractClient(URL documentBase, String path, String sessionID) {
        this.documentBase = documentBase;
        this.path = path;
        this.sessionID = sessionID;
        this.authorization = null;
    }

    public String getID() {
        return sessionID;
    }
    
    public void setAuthorization(String username, String password) {
        String userPassword = username + ":" + password;
        char[] encoding = Base64.encode(userPassword.getBytes());
        this.authorization = "Basic " + new String(encoding);
    }
    
    public String getAuthorization() {
        return authorization;
    }
}