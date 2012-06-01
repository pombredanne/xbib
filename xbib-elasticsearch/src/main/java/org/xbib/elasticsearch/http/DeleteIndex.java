package org.xbib.elasticsearch.http;

import java.io.IOException;
import java.io.InputStream;
import org.xbib.io.ResultProcessor;
import org.xbib.io.http.netty.HttpOperation;

/**
 * Remove ElasticSearch index
 * 
 */
public class DeleteIndex extends AbstractRequest {

    @Override
    public void execute(ElasticsearchSession session) throws IOException {
        HttpOperation op = new HttpOperation();
        op.prepareExecution(session);
    }

    @Override
    public void addProcessor(ResultProcessor<InputStream> processor) {
        
    }
}
