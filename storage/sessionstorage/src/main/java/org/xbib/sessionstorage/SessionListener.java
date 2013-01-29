package org.xbib.sessionstorage;

import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Simple http session listener implements HttpSessionListener interface.
 */
public class SessionListener implements HttpSessionListener {

    private static final Logger logger = Logger.getLogger(SessionListener.class.getName());
    private int sessionCount;

    public SessionListener() {
        this.sessionCount = 0;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        synchronized (this) {
            sessionCount++;
        }
        String id = session.getId();
        String message = new StringBuilder("new session for ")
                .append(se.getSource().toString())
                .append(", ID ").append(id)
                .append(", total of ").append(sessionCount)
                .append(" live sessions").toString();
        logger.info(message);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        String id = session.getId();
        synchronized (this) {
            sessionCount--;
        }
        String message = new StringBuilder("destroyed session for ")
                .append(se.getSource().toString())
                .append(", ID ").append(id)
                .append(", total of ").append(sessionCount)
                .append(" live sessions").toString();
        logger.info(message);
    }
}