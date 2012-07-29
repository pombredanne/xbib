package org.xbib.io.iso23950.adapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.testng.annotations.Test;
import org.xbib.io.iso23950.Diagnostics;
import org.xbib.io.iso23950.PQFSearchRetrieve;
import org.xbib.io.iso23950.ZAdapter;
import org.xbib.io.iso23950.ZAdapterFactory;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.sru.iso23950.adapter.SRUAdapterTest;
import org.xbib.xml.transform.StylesheetTransformer;

public class ZAdapterTest {

    private final static Logger logger = LoggerFactory.getLogger(ZAdapterTest.class.getName());
    private final String[] adapterNames = new String[]{
        "BVB", "GBV", "HBZ", "HEBIS", "KOBV", "ZDB"
    // "SWB" broken:  z3950.bsz-bw.de: ASN error, non-surrogate diagnostics: [UNIVERSAL 16]{[UNIVERSAL 6] '2a8648ce130401'H,[UNIVERSAL 2] '00ec'H,[UNIVERSAL 27] '737762'H}
    };

    @Test
    public void testAdapterSearchRetrieve() throws Diagnostics, IOException {
        for (String adapterName : adapterNames) {
            ZAdapter adapter = ZAdapterFactory.getAdapter(adapterName);
            FileOutputStream out = new FileOutputStream("target/" + adapter.getURI().getHost() + ".xml");
            try (Writer sw = new OutputStreamWriter(out, "UTF-8")) {
                String query = "@attr 1=4 test";
                String resultSetName = "default";
                String elementSetName = "F";
                int from = 1;
                int size = 10;
                StylesheetTransformer transformer = new StylesheetTransformer("src/main/resources");
                PQFSearchRetrieve op = new PQFSearchRetrieve();
                try {
                    adapter.connect();
                    adapter.setStylesheetTransformer(transformer);
                    op.setDatabase(adapter.getDatabases()).setQuery(query).setResultSetName(resultSetName).setElementSetName(elementSetName).setPreferredRecordSyntax(adapter.getPreferredRecordSyntax()).setFrom(from).setSize(size);
                    adapter.searchRetrieve(op, new SearchRetrieveResponse(sw));
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                } finally {
                    adapter.disconnect();
                }
            }
        }
    }
}
