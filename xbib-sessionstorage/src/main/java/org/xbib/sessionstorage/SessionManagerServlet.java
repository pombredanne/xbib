package org.xbib.sessionstorage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class SessionManagerServlet extends HttpServlet {

    private Logger log;

   @Override
   public void init(ServletConfig config) throws ServletException {
       super.init(config);
       String loggerName = config.getInitParameter("loggerName");
       if (loggerName == null) {
           throw new ServletException("loggerName must not be null");
       }
       this.log = LoggerFactory.getLogger(loggerName);
       log.info("SessioNManagerServlet started");
   }    
    
    @Override
    public void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(request.isRequestedSessionIdValid() ? 200 : 404);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        response.setStatus(200);
        response.setContentType("application/x-java-serialized-object");
        ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());
        output.writeObject(getAttributes(session));
        output.close();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            HttpSession session = request.getSession();
            ObjectInputStream input = new ObjectInputStream(request.getInputStream());
            SessionAttributes newAttributes = (SessionAttributes) input.readObject();
            for (Map.Entry<String,Object> me : newAttributes.entrySet()) {
                session.setAttribute(me.getKey(), me.getValue());
            }
            response.setStatus(200);
            response.setContentType("application/x-java-serialized-object");
            ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());
            output.writeObject(getAttributes(session));
            output.close();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            response.setStatus(500);
        }
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            HttpSession session = request.getSession();
            ObjectInputStream input = new ObjectInputStream(request.getInputStream());
            SessionAttributes newAttributes = (SessionAttributes) input.readObject();
            for (Map.Entry<String,Object> me : newAttributes.entrySet()) {
                session.removeAttribute(me.getKey());
            }
            response.setStatus(200);
            response.setContentType("application/x-java-serialized-object");
            ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());
            output.writeObject(getAttributes(session));
            output.close();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            response.setStatus(500);
        }
    }

    private SessionAttributes getAttributes(HttpSession session) {
        SessionAttributes attributes = new SessionAttributes();
        Enumeration<String> en = session.getAttributeNames();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            Object value = session.getAttribute(key);
            if (value instanceof Serializable) {
                attributes.put(key, value);
            }
        }
        return attributes;
    }
    
}
