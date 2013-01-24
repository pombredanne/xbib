package org.xbib.applet.client;

import java.net.HttpURLConnection;
import java.net.URL;
import org.xbib.applet.util.ExceptionFormatter;
import org.xbib.applet.util.URLUtil;

public class LogClient extends AbstractClient {

    public LogClient(URL documentBase, String path, String sessionID) {
        super(documentBase, path, sessionID);
    }

    public void info(String message) {
        log("info", message);
    }

    public void error(String message) {
        log("error", message);
    }

    public void error(Throwable t) {
        log("error", ExceptionFormatter.format(t));
    }

    public void debug(String message) {
        log("debug", message);
    }

    public void trace(String message) {
        log("trace", message);
    }

    public void warn(String message) {
        log("warn", message);
    }

    public void log(String level, String message) {
        try {
            URL url = new URL(documentBase, path + ";jessionid=" + sessionID);
            String params = URLUtil.renderQueryString("level", level, "message", message);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            conn.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes("US-ASCII").length));
            conn.setUseCaches(false);
            conn.getOutputStream().write(params.getBytes("UTF-8"));
            conn.getOutputStream().close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
