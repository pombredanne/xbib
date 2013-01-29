package org.xbib.applet.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.xbib.applet.util.ExceptionFormatter;

public class DocumentClient extends AbstractClient {

    private static final int READ_BLOCK = 8192;

    public DocumentClient(URL documentBase, String path, String sessionID) {
        super(documentBase, path, sessionID);
    }
    
    public long numberOfErrorDocuments() throws Exception {
        URL url = new URL(documentBase, path+";jessionid=" + sessionID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("HEAD");
        conn.setInstanceFollowRedirects(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Authorization", getAuthorization());
        conn.connect();
        String result = conn.getResponseCode() == 200 ? conn.getHeaderField("Number-of-error-documents") : null;
        conn.disconnect();
        return result == null? 0 :Long.parseLong(result);        
    }
    
    public long numberOfNextDocuments() throws Exception {
        URL url = new URL(documentBase, path + ";jessionid=" + sessionID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("HEAD");
        conn.setInstanceFollowRedirects(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Authorization", getAuthorization());
        conn.connect();
        String result = conn.getResponseCode() == 200 ? conn.getHeaderField("Number-of-documents") : null;
        conn.disconnect();
        return result == null? 0 : Long.parseLong(result);        
    }

    public String nextDocument() throws IOException {
        URL url = new URL(documentBase, path + ";jessionid=" + sessionID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("HEAD");
        conn.setInstanceFollowRedirects(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Authorization", getAuthorization());
        conn.connect();
        String result = conn.getResponseCode() == 200 ? conn.getHeaderField("Next-document") : null;
        conn.disconnect();
        return result;
    }

    public String nextErrorDocument() throws IOException {
        URL url = new URL(documentBase, path + ";jessionid=" + sessionID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("HEAD");
        conn.setInstanceFollowRedirects(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Authorization", getAuthorization());
        conn.connect();
        String result = conn.getResponseCode() == 200 ? conn.getHeaderField("Next-error-document") : null;
        conn.disconnect();
        return result;
    }
    public void ok(String document) throws IOException {
        URL url = new URL(documentBase, path + "/" + document + ";jessionid=" + sessionID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("POST");
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("Authorization", getAuthorization());
        conn.setRequestProperty("Content-Length", "0");
        conn.setUseCaches(false);
        conn.connect();
        conn.disconnect();
    }

    public void error(String document, Throwable t) {
        try {
            String message = ExceptionFormatter.format(t);
            URL url = new URL(documentBase, document + ";jessionid=" + sessionID);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Authorization", getAuthorization());
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
            conn.setRequestProperty("Content-Length", "" + Integer.toString(message.getBytes("US-ASCII").length));
            conn.getOutputStream().write(message.getBytes("UTF-8"));
            conn.getOutputStream().close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ByteBuffer getDocument(String document) throws IOException {
        URL url = new URL(documentBase, path + "/" + document + ";jessionid=" + sessionID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Accept", ACCEPT);
        conn.setRequestProperty("Authorization", getAuthorization());
        ByteBuffer b = readToEnd(conn.getInputStream());
        conn.disconnect();
        return b;
    }

    private ByteBuffer readToEnd(InputStream in) throws IOException {
        if (in == null) {
            throw new IOException("no input stream");
        }
        ReadableByteChannel bc = Channels.newChannel(in);
        ByteBuffer bb = ByteBuffer.allocate(READ_BLOCK);
        while (bc.read(bb) != -1) {
            bb = resizeBuffer(bb);
        }
        bb.position(0);
        in.close();
        return bb;
    }

    private ByteBuffer resizeBuffer(ByteBuffer in) {
        ByteBuffer result = in;
        if (in.remaining() < READ_BLOCK) {
            result = ByteBuffer.allocate(in.capacity() * 2);
            in.flip();
            result.put(in);
        }
        return result;
    }
}
