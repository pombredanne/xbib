package org.xbib.sru.iso23950.adapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.SRUService;
import org.xbib.sru.iso23950.ISO23950SRUServiceFactory;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.sru.iso23950.ISO23950SRUServiceFactory;
import org.xbib.xml.transform.StylesheetTransformer;

public class SRUAdapterTest {

    private final static Logger logger = LoggerFactory.getLogger(SRUAdapterTest.class.getName());
    
    public void testAdapterSearchRetrieve() throws Diagnostics, IOException {
        for (String adapterName : Arrays.asList("BVB","GBV","HBZ","HEBIS","ZDB")) {            
            SRUService adapter = ISO23950SRUServiceFactory.getAdapter(adapterName);
            FileOutputStream out = new FileOutputStream("target/sru-" + adapter.getURI().getHost() + ".xml");
            try (Writer sw = new OutputStreamWriter(out, "UTF-8")) {
                String query = "dc.title = test";
                int from = 1;
                int size = 10;
                StylesheetTransformer transformer = new StylesheetTransformer("src/main/resources/xsl");
                try {
                    adapter.connect();
                    adapter.setStylesheetTransformer(transformer);
                    SearchRetrieveRequest op = new SearchRetrieveRequest();
                    op.setQuery(query).setStartRecord(from).setMaximumRecords(size);
                    SearchRetrieveResponse resp =new SearchRetrieveResponse(sw);
                    adapter.searchRetrieve(op, resp);
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                } finally {
                    adapter.disconnect();
                }
            }
        }
    }
}
