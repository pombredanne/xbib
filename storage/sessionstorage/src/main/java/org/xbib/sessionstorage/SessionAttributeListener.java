package org.xbib.sessionstorage;

import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

public class SessionAttributeListener implements HttpSessionAttributeListener {

    private static final Logger logger = Logger.getLogger(SessionAttributeListener.class.getName());

    @Override
    public void attributeAdded(HttpSessionBindingEvent se) {
        HttpSession session = se.getSession();
        String id = session.getId();
        String name = se.getName();
        String value = (String) se.getValue();
        String source = se.getSource().getClass().getName();
        String message = new StringBuilder("Attribute bound to session in ")
                .append(source).append("\nThe attribute name: ").append(name)
                .append("\n").append("The attribute value:").append(value)
                .append("\n").append("The session ID: ").append(id).toString();
        logger.info(message);
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent se) {
        HttpSession session = se.getSession();
        String id = session.getId();
        String name = se.getName();
        if (name == null) {
            name = "Unknown";
        }
        String value = (String) se.getValue();
        String source = se.getSource().getClass().getName();
        String message = new StringBuilder("Attribute unbound from session in ")
                .append(source).append("\nThe attribute name: ").append(name)
                .append("\n").append("The attribute value: ").append(value)
                .append("\n").append("The session ID: ").append(id).toString();
        logger.info(message);
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent se) {
        String source = se.getSource().getClass().getName();
        String message = new StringBuilder("Attribute replaced in session  ")
                .append(source).toString();
        logger.info(message);
    }
}