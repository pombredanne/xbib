package org.xbib.applet.client;

import org.xbib.applet.client.AbstractClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.xbib.sessionstorage.SessionAttributes;

public class SessionClient extends AbstractClient {

    public SessionClient(URL documentBase, String path, String sessionID) {
        super(documentBase, path, sessionID);
    }

    /**
     * Checks if the session ID is valid.
     */
    public int head() throws IOException {
        try {
            URL url = new URL(documentBase, path + ";jessionid=" + sessionID);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("HEAD");
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Accept", ACCEPT);
            conn.connect();
            return conn.getResponseCode();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public SessionAttributes get() throws IOException {
        try {
            URL url = new URL(documentBase, path + ";jessionid=" + sessionID);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Accept", ACCEPT);
            conn.connect();
            int status = conn.getResponseCode();
            SessionAttributes newSession = null;
            if (status == 200) {
                ObjectInputStream input = new ObjectInputStream(conn.getInputStream());
                newSession = (SessionAttributes) input.readObject();
                input.close();
            } else {
                throw new IOException("Session error: " + url + " : " + status);
            }
            conn.disconnect();
            newSession.put("id", sessionID);
            return newSession;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Update session.
     *
     * @param session
     * @return a new session
     * @throws IOException
     */
    public SessionAttributes post(SessionAttributes session) throws IOException {
        URL url = new URL(documentBase, path + ";jessionid=" + sessionID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        OutputStream out = null;
        InputStream in = null;
        try {
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept", ACCEPT);
            conn.setRequestProperty("Content-Type", "application/x-java-serialized-object");
            out = conn.getOutputStream();
            if (out != null) {
                ObjectOutputStream output = new ObjectOutputStream(out);
                output.writeObject(session);
            }
            in = conn.getInputStream();
            int status = conn.getResponseCode();
            SessionAttributes newSession;
            if (in != null) {
                ObjectInputStream input = new ObjectInputStream(in);
                newSession = (SessionAttributes) input.readObject();
                input.close();
                return newSession;
            } else {
                throw new IOException("Session error: " + url + " : status " + status);
            }
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            conn.disconnect();
        }
    }

    /**
     * Deletes a session.
     *
     * @return
     * @throws IOException
     */
    public boolean delete() throws IOException {
        try {
            URL url = new URL(documentBase, path + ";jessionid=" + sessionID);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("DELETE");
            conn.setInstanceFollowRedirects(true);
            conn.connect();
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
