package org.xbib.jersey.filter;

import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class PasswordSecurityFilter implements ContainerRequestFilter {

    private final static Logger logger = LoggerFactory.getLogger(PasswordSecurityFilter.class.getName());

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        logger.debug("filter {}", request);
        String authentication = request.getHeaderValue(ContainerRequest.AUTHORIZATION);
        logger.debug("filter auth {}", authentication);
        if (authentication == null) {
            return request;
        }
        if (!authentication.startsWith("Basic ")) {
            return request;
        }
        authentication = authentication.substring("Basic ".length());
        String auth = Base64.base64Decode(authentication);
        logger.debug("filter auth decoded {}", auth);
        String[] values = auth.split(":");
        if (values.length < 2) {
            return request;
        }
        PasswordSecurityContext sc = new PasswordSecurityContext(request.getSecurityContext());
        sc.setUser(values[0]);
        sc.setPassword(values[1]);
        request.setSecurityContext(sc);
        logger.debug("request = {} security context = {}", request, sc);
        return request;
    }

}
