package org.xbib.json;

import com.fasterxml.jackson.core.JsonToken;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class JsonPushParserTest {

    private static final Logger logger = LoggerFactory.getLogger(JsonPushParserTest.class.getName());

    @Test
    public void testPushParser() throws IOException {
        JsonPushParser parser = new JsonPushParser();
        parser.register(new JsonConsumer() {
            @Override
            public void add(JsonToken token) {
                logger.info("token={}", token);
            }
        });
        OutputStreamWriter w = new OutputStreamWriter(parser, "UTF-8");
        w.write("{ \"Hello\"");
        w.flush();
        parser.parse();
        w.write(": \"World\" }");
        w.flush();
        parser.parse();
    }
}
