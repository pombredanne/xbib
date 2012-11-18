package org.xbib.elasticsearch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.xml.transform.StylesheetTransformer;

public class DaoTest {

    private static final Logger logger = LoggerFactory.getLogger(DaoTest.class.getName());

    public void test() throws Exception {
        String mediaType = "application/xml";
        String index = "hbz";
        String type = "*";
        int from = 0;
        int size = 10;
        String query = "test";

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        ElasticsearchDAO dao = new ElasticsearchDAO()
                .logger(LoggerFactory.getLogger(mediaType, DaoTest.class.getName()))
                .newClient(false).newRequest()
                .setIndex(index).setType(type)
                .setFrom(from).setSize(size)
                .fromCQL(query)
                .execute()
                .outputFormat(OutputFormat.formatOf(mediaType))
                .styleWith(new StylesheetTransformer("/xsl"), null, output)
                .dispatchTo(new OutputProcessor() {
            @Override
            public void process(OutputStatus status, OutputFormat format, byte[] message) throws IOException {
                output.write(message);
            }
        });
    }
}
