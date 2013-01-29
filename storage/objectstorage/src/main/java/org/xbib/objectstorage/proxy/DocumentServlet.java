package org.xbib.objectstorage.proxy;

import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 * Get print documents.
 *
 */
@WebServlet(name = "DocumentServlet", urlPatterns = {"/print/doc"})
@ServletSecurity(
@HttpConstraint(transportGuarantee = TransportGuarantee.NONE, rolesAllowed = {"doc"}))
public class DocumentServlet extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(DocumentServlet.class.getName());
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * Check if document is available. Returns 204 if true, 404 otherwise,
     * or 500 if an error occured.
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    public void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            
        } catch (Exception e) {
            logger.error(null, e);
            response.setStatus(500);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        response.setStatus(200);
        response.setContentType("application/pdf");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }
}
