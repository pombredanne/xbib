
package org.xbib.filestorage.oauth;

import com.sun.jersey.oauth.server.spi.OAuthProvider;
import com.sun.jersey.oauth.server.spi.OAuthToken;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@Provider
public class LDAPOAuthProvider implements OAuthProvider {
    private static final ConcurrentHashMap<String, Consumer> consumerByConsumerKey = new ConcurrentHashMap<String, Consumer>();
    private static final ConcurrentHashMap<String, Token> accessTokenByTokenString = new ConcurrentHashMap<String, Token>();
    private static final ConcurrentHashMap<String, Token> requestTokenByTokenString = new ConcurrentHashMap<String, Token>();
    private static final ConcurrentHashMap<String, String> verifierByTokenString = new ConcurrentHashMap<String, String>();

    @Override
    public Token getRequestToken(String token) {
        return requestTokenByTokenString.get(token);
    }

    @Override
    public OAuthToken newRequestToken(String consumerKey, String callbackUrl, Map<String, List<String>> attributes) {
        Token rt = new Token(this, newUUIDString(), newUUIDString(), consumerKey, callbackUrl, attributes);
        requestTokenByTokenString.put(rt.getToken(), rt);
        return rt;
    }

    @Override
    public OAuthToken newAccessToken(OAuthToken requestToken, String verifier) {
        if (verifier == null || !verifier.equals(verifierByTokenString.remove(requestToken.getToken()))) {
            return null;
        }
        Token token = requestToken == null ? null : requestTokenByTokenString.remove(requestToken.getToken());
        if (token == null) {
            return null;
        }
        Token at = new Token(this, newUUIDString(), newUUIDString(), token);
        accessTokenByTokenString.put(at.getToken(), at);
        return at;
    }

    @Override
    public OAuthToken getAccessToken(String token) {
        return accessTokenByTokenString.get(token);
    }

    @Override
    public Consumer getConsumer(String consumerKey) {
        return consumerByConsumerKey.get(consumerKey);
    }

    public Consumer registerConsumer(String owner, MultivaluedMap<String, String> attributes) {
        Consumer c = new Consumer(newUUIDString(), newUUIDString(), owner, attributes);
        consumerByConsumerKey.put(c.getKey(), c);
        return c;
    }

    public Set<Consumer> getConsumers(String owner) {
        Set<Consumer> result = new HashSet<Consumer>();
        for (Consumer consumer : consumerByConsumerKey.values()) {
            if (consumer.getOwner().equals(owner)) {
                result.add(consumer);
            }
        }
        return result;
    }

    public Set<Token> getAccessTokens(String principalName) {
        Set<Token> tokens = new HashSet<Token>();
        for (Token token : accessTokenByTokenString.values()) {
            if (principalName.equals(token.getPrincipal().getName())) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    public String authorizeToken(Token token, Principal userPrincipal, Set<String> roles) {
        Token authorized = token.authorize(userPrincipal, roles);
        requestTokenByTokenString.put(token.getToken(), authorized);
        String verifier = newUUIDString();
        verifierByTokenString.put(token.getToken(), verifier);
        return verifier;
    }

    public void revokeAccessToken(String token, String principalName) {
        Token t = (Token) getAccessToken(token);
        if (t != null && t.getPrincipal().getName().equals(principalName)) {
            accessTokenByTokenString.remove(token);
        }
    }

    protected String newUUIDString() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
