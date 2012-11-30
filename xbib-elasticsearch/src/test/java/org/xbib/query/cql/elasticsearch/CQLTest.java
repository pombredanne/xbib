package org.xbib.query.cql.elasticsearch;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.query.cql.CQLParser;

public class CQLTest extends Assert {

    private final static Logger logger = LoggerFactory.getLogger(CQLTest.class.getName());

    @Test
    public void testQueries() throws UnsupportedEncodingException, IOException {
        int ok = 0;
        int errors = 0;
        String path = "org/xbib/query/cql/elasticsearch/valid";
        try (LineNumberReader lr = new LineNumberReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path), "UTF-8"))) {
            String line;
            while ((line = lr.readLine()) != null) {
                if (line.trim().length() > 0 && !line.startsWith("#")) {
                    try {
                        int pos = line.indexOf('|');
                        line = pos > 0 ? line.substring(0,pos) : line;
                        process(line);
                        ok++;
                    } catch (Exception e) {
                        logger.warn(e.getMessage());
                        errors++;
                    }
                }
            }
        }
        assertEquals(errors, 0);
    }

    private void process(String line) throws Exception {
        CQLParser parser = new CQLParser(new StringReader(line));
        parser.parse();
        logger.info("{} ===> {}", line, parser.getCQLQuery());
    }
}
