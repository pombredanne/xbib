package org.xbib.elasticsearch;

import java.io.IOException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import org.elasticsearch.indices.IndexMissingException;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class SearchResponseStreamTest {

    private static final Logger logger = LoggerFactory.getLogger(SearchResponseStreamTest.class.getName());

    @Test
    public void testQuery() throws IOException {

        String index = "hbztest";
        String type = "title";
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", "joerg")
                .put("client.transport.sniff", false).build();
        TransportClient client = new TransportClient(settings);
        InetSocketTransportAddress address = new InetSocketTransportAddress("127.0.0.1", 9300);
        client.addTransportAddress(address);
        try {
            SearchResponse response = client.prepareSearch().
                    setIndices(index).
                    setTypes(type).
                    setFrom(0).setSize(10).setQuery(termQuery("_all", "test")).execute().actionGet();
            SearchResponseInputStream in = new SearchResponseInputStream();
            response.writeTo(in);
            logger.info("in={}", new String(in.getByteArray()));
        } catch (ClusterBlockException | NoNodeAvailableException | IndexMissingException e) {
            logger.warn(e.getMessage());
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
