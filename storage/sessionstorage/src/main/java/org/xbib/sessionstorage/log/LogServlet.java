package org.xbib.sessionstorage.log;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class LogServlet extends HttpServlet {    

   private Logger log;

   @Override
   public void init(ServletConfig config) throws ServletException {
       super.init(config);
       String loggerName = config.getInitParameter("loggerName");
       if (loggerName == null) {
           throw new ServletException("loggerName must not be null");
       }
       this.log = LoggerFactory.getLogger(loggerName);
       log.info("LogServlet started");
   }
    
    @Override
    public void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(request.isRequestedSessionIdValid() ? 204 : 404);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log(request);
        response.setStatus(204);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log(request);
        response.setStatus(204);
    }
    
    private void log(HttpServletRequest request) throws IOException {
        String level = request.getParameter("level");
        String message = request.getSession().getId() + " " +
                request.getParameter("message");
        if ("warn".equals(level)) {
            log.warn(message);
        }
        else if ("error".equals(level)) {
            log.error(message);
        } else if ("debug".equals(level)) {
            log.debug(message);
        }
        else if ("trace".equals(level)) {
            log.trace(message);
        } else {
            log.info(message);
        }
    }
}
