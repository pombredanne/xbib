package org.xbib.filestorage.oauth;

import com.sun.jersey.oauth.server.spi.OAuthConsumer;
import com.sun.jersey.oauth.server.spi.OAuthProvider;
import com.sun.jersey.oauth.server.spi.OAuthToken;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MultivaluedMap;

public class Token implements OAuthToken {

    private final OAuthProvider provider;
    private final String token;
    private final String secret;
    private final String consumerKey;
    private final String callbackUrl;
    private final Principal principal;
    private final Set<String> roles;
    private final MultivaluedMap<String, String> attribs;

    protected Token(OAuthProvider provider, String token, String secret, String consumerKey, String callbackUrl,
            Principal principal, Set<String> roles, MultivaluedMap<String, String> attributes) {
        this.provider = provider;
        this.token = token;
        this.secret = secret;
        this.consumerKey = consumerKey;
        this.callbackUrl = callbackUrl;
        this.principal = principal;
        this.roles = roles;
        this.attribs = attributes;
    }

    public Token(OAuthProvider provider, String token, String secret, String consumerKey, String callbackUrl, Map<String, List<String>> attributes) {
        this(provider, token, secret, consumerKey, callbackUrl, null, Collections.<String>emptySet(),
                ImmutableMultiMap.newImmutableMultiMap(attributes));
    }

    public Token(OAuthProvider provider, String token, String secret, Token requestToken) {
        this(provider, token, secret, requestToken.getConsumer().getKey(), null,
                requestToken.principal, requestToken.roles, ImmutableMultiMap.EMPTY);
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public OAuthConsumer getConsumer() {
        return provider.getConsumer(consumerKey);
    }

    @Override
    public MultivaluedMap<String, String> getAttributes() {
        return attribs;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public boolean isInRole(String role) {
        return roles.contains(role);
    }

    /**
     * Returns callback URL for this token (applicable just to request tokens)
     *
     * @return callback url
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * Authorizes this token - i.e. generates a clone with principal and roles
     * set to the passed values.
     *
     * @param principal Principal to add to the token.
     * @param roles Roles to add to the token.
     * @return Cloned token with the principal and roles set.
     */
    protected Token authorize(Principal principal, Set<String> roles) {
        return new Token(provider, token, secret, consumerKey, callbackUrl, principal, roles == null ? Collections.<String>emptySet() : new HashSet<String>(roles), attribs);
    }
}