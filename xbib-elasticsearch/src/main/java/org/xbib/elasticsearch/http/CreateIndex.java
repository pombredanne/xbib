package org.xbib.elasticsearch.http;

import java.io.IOException;
import java.io.InputStream;
import org.xbib.io.Identifiable;
import org.xbib.io.ResultProcessor;
import org.xbib.io.StringData;
import org.xbib.io.http.netty.HttpOperation;
import org.xbib.io.http.netty.HttpRequest;
import org.xbib.io.operator.CreateOperator;

/**
 * Create index operation for ElasticSearch with optional mapping
 *
 */
public class CreateIndex extends AbstractRequest
        implements CreateOperator<ElasticsearchSession, Identifiable, StringData> {

    @Override
    public void write(ElasticsearchSession session, StringData mapping) throws IOException {
        StringData mappings = mapping != null ? mapping : getMappings();
        HttpRequest req = new HttpRequest("PUT").setURI(session.getURI()).setTimeout(30000);
        if (mappings != null) {
            req.setBody(mappings.toString());
        }
        session.addRequest(req);
        new HttpOperation().prepareExecution(session).execute();        
    }

    @Override
    public void flush(ElasticsearchSession session) throws IOException {
    }

    @Override
    public void execute(ElasticsearchSession session) throws IOException {
        write(session, null);
    }

    @Override
    public void create(ElasticsearchSession session, Identifiable identifier, StringData data) throws IOException {
        // ignore identifier
        write(session, data);
    }

    @Override
    public void addProcessor(ResultProcessor<InputStream> processor) {
    }
}
