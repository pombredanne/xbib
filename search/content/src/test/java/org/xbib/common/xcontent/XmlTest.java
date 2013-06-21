package org.xbib.common.xcontent;

import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;

import static org.xbib.common.xcontent.XContentFactory.xmlBuilder;

public class XmlTest {

    private final Logger logger = Loggers.getLogger(XmlTest.class);

    public void testXml() throws Exception {
        XContentBuilder builder = xmlBuilder();
        builder.startObject().field("Hello", "World").endObject();
        logger.info("xml = {}", builder.string());
    }
}
