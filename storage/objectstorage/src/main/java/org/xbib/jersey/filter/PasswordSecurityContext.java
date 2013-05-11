package org.xbib.jersey.filter;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;


public class PasswordSecurityContext implements SecurityContext {

    SecurityContext context;
    String user;
    String password;

    PasswordSecurityContext(SecurityContext context) {
        this.context = context;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public Principal getUserPrincipal() {
        return context.getUserPrincipal();
    }

    @Override
    public boolean isUserInRole(String role) {
        return context.isUserInRole(role);
    }

    @Override
    public boolean isSecure() {
        return context.isSecure();
    }

    @Override
    public String getAuthenticationScheme() {
        return context.getAuthenticationScheme();
    }
}
