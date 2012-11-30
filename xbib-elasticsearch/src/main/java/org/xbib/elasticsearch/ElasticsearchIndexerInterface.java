package org.xbib.elasticsearch;

import java.io.IOException;
import java.net.URI;
import org.elasticsearch.common.settings.Settings;
import org.xbib.rdf.Resource;

public interface ElasticsearchIndexerInterface {

    ElasticsearchIndexerInterface settings(Settings settings);

    ElasticsearchIndexerInterface newClient(URI uri, boolean force);

    void shutdown();

    ElasticsearchIndexerInterface setIndex(String index);

    ElasticsearchIndexerInterface setType(String type);

    ElasticsearchIndexerInterface setBulkSize(int bulkSize);
    
    ElasticsearchIndexerInterface setMaxActiveRequests(int maxActiveRequests);
    
    ElasticsearchIndexerInterface setMillisBeforeContinue(long millis);

    ElasticsearchIndexerInterface write(Resource resource) throws IOException;

    ElasticsearchIndexerInterface flush() throws IOException;
}
