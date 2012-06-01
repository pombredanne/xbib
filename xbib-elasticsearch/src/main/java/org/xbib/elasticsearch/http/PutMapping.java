package org.xbib.elasticsearch.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.xbib.io.Identifiable;
import org.xbib.io.ResultProcessor;
import org.xbib.io.StringData;
import org.xbib.io.http.netty.HttpOperation;
import org.xbib.io.http.netty.HttpRequest;
import org.xbib.io.operator.CreateOperator;

/**
 * Put ElasticSearch mapping
 *
 */
public class PutMapping extends AbstractRequest
        implements CreateOperator<ElasticsearchSession, Identifiable, StringData> {

    @Override
    public void write(ElasticsearchSession session, StringData mapping) throws IOException {
        StringData json = mapping != null ? mapping : getMappings();
        HttpRequest req = new HttpRequest("PUT").setURI(URI.create(session.getURI() + "/_mapping"));
        if (json != null) {
            req.setBody(json.toString());
        }
        session.addRequest(req);
        HttpOperation op = new HttpOperation();
        op.prepareExecution(session);
        op.execute();
    }

    @Override
    public void flush(ElasticsearchSession session) throws IOException {
    }

    @Override
    public void addProcessor(ResultProcessor<InputStream> processor) {
    }

    @Override
    public void create(ElasticsearchSession session, Identifiable identifier, StringData data) throws IOException {
    }

    @Override
    public void execute(ElasticsearchSession session) throws IOException {
    }
}
