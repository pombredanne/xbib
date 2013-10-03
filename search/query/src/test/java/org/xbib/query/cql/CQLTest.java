package org.xbib.query.cql;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class CQLTest extends Assert {

    private final static Logger logger = LoggerFactory.getLogger(CQLTest.class.getName());

    @Test
    public void testValidQueries() throws IOException {
        test("org/xbib/query/cql/valid");
    }

    @Test
    public void testInvalidQueries() throws IOException {
        test("org/xbib/query/cql/invalid");
    }

    private void test(String path) throws IOException {
        int ok = 0;
        int errors = 0;
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
