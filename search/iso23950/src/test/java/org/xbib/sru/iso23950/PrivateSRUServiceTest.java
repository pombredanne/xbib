package org.xbib.sru.iso23950;

import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.iso23950.service.ZSRUServiceFactory;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.service.SRUService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

public class PrivateSRUServiceTest {

    private final Logger logger = LoggerFactory.getLogger(SRUServiceTest.class.getName());

    @Test
    public void testSRUService() throws IOException {
        for (String name : Arrays.asList("DE-600", "DE-601", "DE-602", "DE-604", "DE-604", "DE-605")) {
            logger.info("trying " + name);
            try {
                SRUService service = ZSRUServiceFactory.getService(name);
                SRUClient client = service.newClient();
                FileOutputStream out = new FileOutputStream("target/sru-" + service.getURI().getHost() + ".xml");
                try (Writer writer = new OutputStreamWriter(out, "UTF-8")) {
                    String query = "dc.title = test";
                    int from = 1;
                    int size = 10;
                    SearchRetrieveRequest request = client.newSearchRetrieveRequest();
                    request.setQuery(query)
                            .setStartRecord(from)
                            .setMaximumRecords(size);
                    client.searchRetrieve(request).to(writer);
                }
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }
    }
}
