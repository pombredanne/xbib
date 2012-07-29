package org.xbib.filestorage.oauth;

import com.sun.jersey.oauth.server.spi.OAuthConsumer;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

public class Consumer implements OAuthConsumer {

    private final String key;
    private final String secret;
    private final String owner;
    private final MultivaluedMap<String, String> attribs;

    public Consumer(String key, String secret, String owner, Map<String, List<String>> attributes) {
        this.key = key;
        this.secret = secret;
        this.owner = owner;
        this.attribs = ImmutableMultiMap.newImmutableMultiMap(attributes);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    /**
     * Returns identifier of owner of this consumer - i.e. who registered the
     * consumer.
     *
     * @return consumer owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Returns additional attributes associated with the consumer (e.g. name,
     * URI, description, etc.)
     *
     * @return name-values pairs of additional attributes
     */
    public MultivaluedMap<String, String> getAttributes() {
        return attribs;
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public boolean isInRole(String role) {
        return false;
    }
}
