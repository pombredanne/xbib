
package org.xbib.atom;

import java.io.IOException;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.provider.managed.FeedConfiguration;

/**
 * This is an Atom feed factory for query implementations
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public interface AtomFeedFactory {
    Feed createFeed(RequestContext request, FeedConfiguration config, String query, int from, int size) throws IOException;
}
