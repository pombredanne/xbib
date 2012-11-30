package org.xbib.elasticsearch;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import javax.xml.stream.util.XMLEventConsumer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.logging.Logger;
import org.xbib.xml.transform.StylesheetTransformer;

public interface ElasticsearchInterface {

    ElasticsearchInterface settings(Settings settings);

    ElasticsearchInterface newClient(boolean force);

    ElasticsearchInterface newClient(URI uri, boolean force);
    
    void shutdown();
    
    ElasticsearchInterface newRequest();

    ElasticsearchInterface setIndex(String index);

    ElasticsearchInterface setIndex(String... index);

    ElasticsearchInterface setType(String type);

    ElasticsearchInterface setType(String... type);

    ElasticsearchInterface setFrom(int from);

    ElasticsearchInterface setSize(int size);

    ElasticsearchInterface filter(String filter);

    ElasticsearchInterface facets(String facets);

    ElasticsearchInterface timeout(TimeValue timeout);

    ElasticsearchInterface logger(Logger queryLogger);

    ElasticsearchInterface query(String query);

    ElasticsearchInterface fromCQL(String query);

    ElasticsearchInterface execute() throws IOException;

    long getTookInMillis();
    
    ElasticsearchInterface outputFormat(OutputFormat format);

    ElasticsearchInterface toJson(OutputStream out) throws IOException;

    ElasticsearchInterface xmlEventConsumer(XMLEventConsumer consumer) 
            throws IOException;

    ElasticsearchInterface styleWith(StylesheetTransformer transformer, String stylesheets, OutputStream target)
            throws IOException;

    ElasticsearchInterface dispatch() 
            throws IOException;
    
    ElasticsearchInterface dispatchTo(OutputProcessor processor) 
            throws IOException;

    ElasticsearchInterface get(String index, String type, String id, OutputProcessor processor) 
            throws IOException;
    
}
