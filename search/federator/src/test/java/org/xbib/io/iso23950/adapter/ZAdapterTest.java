package org.xbib.io.iso23950.adapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.xbib.io.iso23950.Diagnostics;
import org.xbib.io.iso23950.searchretrieve.PQFSearchRetrieveRequest;
import org.xbib.io.iso23950.ZService;
import org.xbib.io.iso23950.ZAdapterFactory;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;

public class ZAdapterTest {

    private final static Logger logger = LoggerFactory.getLogger(ZAdapterTest.class.getName());
    private final String[] adapterNames = new String[]{
        "BVB", "GBV", "HBZ", "HEBIS", "ZDB" 
        // "KOBV"z3950.kobv.de: connection read PDU error
        //java.io.IOException: connection read PDU error
        //	at org.xbib.io.iso23950.ZConnection.readPDU(ZConnection.java:215
        // "SWB" broken:  z3950.bsz-bw.de: ASN error, non-surrogate diagnostics: [UNIVERSAL 16]{[UNIVERSAL 6] '2a8648ce130401'H,[UNIVERSAL 2] '00ec'H,[UNIVERSAL 27] '737762'H}
    };

    public void testAdapterSearchRetrieve() throws Diagnostics, IOException {
        for (String adapterName : adapterNames) {
            ZService adapter = ZAdapterFactory.getAdapter(adapterName);
            FileOutputStream out = new FileOutputStream("target/" + adapter.getURI().getHost() + ".xml");
            try (Writer sw = new OutputStreamWriter(out, "UTF-8")) {
                String query = "@attr 1=4 test";
                String resultSetName = "default";
                String elementSetName = "F";
                int from = 1;
                int size = 10;
                StylesheetTransformer transformer = new StylesheetTransformer("src/main/resources");
                PQFSearchRetrieveRequest request = new PQFSearchRetrieveRequest();
                try {
                    adapter.connect();
                    adapter.setStylesheetTransformer(transformer);
                    request.setDatabase(adapter.getDatabases()).setQuery(query).setResultSetName(resultSetName).setElementSetName(elementSetName).setPreferredRecordSyntax(adapter.getPreferredRecordSyntax()).setFrom(from).setSize(size);
                    adapter.searchRetrieve(request, new SearchRetrieveResponse(request));
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                } finally {
                    adapter.disconnect();
                }
            }
        }
    }
}
