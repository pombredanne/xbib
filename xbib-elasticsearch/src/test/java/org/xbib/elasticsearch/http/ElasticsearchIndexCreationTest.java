package org.xbib.elasticsearch.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import org.testng.annotations.Test;
import org.xbib.io.Mode;
import org.xbib.io.StringData;

public class ElasticsearchIndexCreationTest {

    @Test
    public void testCreate() throws UnsupportedEncodingException, IOException {
        ElasticsearchSession session = new ElasticsearchSession(URI.create("http://localhost:9200/test"));
        CreateIndex op = new CreateIndex();
        session.open(Mode.WRITE);
        StringData data = new StringData("{ \"mappings\" : \"test\" : { \"date_detection\" : { \"false\" } } }");
        op.write(session, data);
        
        session.close();        
    }
}
