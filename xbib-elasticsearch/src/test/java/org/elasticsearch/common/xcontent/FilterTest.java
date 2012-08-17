
package org.elasticsearch.common.xcontent;

import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
        
public class FilterTest extends Assert {

    private static final Logger logger = LoggerFactory.getLogger(FilterTest.class.getName());

    @Test
    public void testHelloFilter() throws IOException {
        FilterContext context = new SimpleFilterContext("hello");
        XContentBuilder builder = FilteredXContent.contentBuilder(context);
        builder.startObject().field("hello", "World").endObject();
        logger.info(builder.string());
    }

    @Test
    public void testHelloFilter2() throws IOException {
        FilterContext context = new SimpleFilterContext("hello");
        XContentBuilder builder = FilteredXContent.contentBuilder(context);
        builder.startObject()
                .field("hello", "World")
                .field("john", "Doe")
                .field("foo", "bar")
                .endObject();
        logger.info(builder.string());
    }

    public void testHelloFilter3() throws IOException {
        FilterContext context = new SimpleFilterContext("hello");
        XContentBuilder builder = FilteredXContent.contentBuilder(context);
        //XContentBuilder builder = jsonBuilder();
        builder.startObject()
                .startArray("field_name").value("1").value("2").endArray()
                .field("hello", "World")
                .startObject("object")
                .field("john", "Doe")
                .endObject()
                .field("foo", "bar")
                .endObject();
        logger.info(builder.string());
    }
    
    
}
