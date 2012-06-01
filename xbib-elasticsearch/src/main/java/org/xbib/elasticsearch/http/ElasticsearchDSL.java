package org.xbib.elasticsearch.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.xbib.elasticsearch.AbstractQueryResultAction;
import org.xbib.io.InputStreamEmptyProcessor;
import org.xbib.io.InputStreamErrorProcessor;
import org.xbib.io.InputStreamProcessor;
import org.xbib.io.Mode;
import org.xbib.io.http.netty.HttpOperation;
import org.xbib.io.http.netty.HttpRequest;

public class ElasticsearchDSL extends AbstractQueryResultAction
        implements InputStreamProcessor, InputStreamEmptyProcessor, InputStreamErrorProcessor {

    private ElasticsearchSession session;
    private OutputStream out;

    public ElasticsearchDSL(ElasticsearchSession session) {
        this.session = session;
    }

    @Override
    public void search(String query)
            throws IOException {
        searchAndProcess(query);
    }

    @Override
    public void searchAndProcess(String query)
            throws IOException {
        HttpRequest request = new HttpRequest("POST").setURI(URI.create(session.getURI() + "/_search")).addParameter("q", query);
        session.open(Mode.READ);
        session.addRequest(request);
        HttpOperation op = new HttpOperation();
        op.prepareExecution(session);
        op.execute(30, TimeUnit.SECONDS);
        session.close();
    }

    @Override
    public void setTarget(OutputStream out) {
        this.out = out;
    }

    @Override
    public OutputStream getTarget() {
        return out;
    }

    @Override
    public void process(InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    @Override
    public void processEmpty(InputStream in) throws IOException {
        // we can't detect empty results without parsing the result stream
    }

    @Override
    public void processError(InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }
}
