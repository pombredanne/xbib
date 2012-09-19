package org.xbib.elasticsearch.http;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.io.InputStream;
import org.xbib.io.ResultProcessor;
import org.xbib.io.StringData;
import org.xbib.io.operator.ResultOperator;

/**
 * Base class for Elasticsearch HTTP operation
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public abstract class AbstractRequest 
        implements ResultOperator<ElasticsearchSession, InputStream>, ResultProcessor<InputStream> {

    private final JsonFactory factory = new JsonFactory();
    private IOException exception;
    private String response;
    protected String index;
    protected String type;

    public void setIndex(String index) {
        this.index = index;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void process(InputStream in) throws IOException {
        JsonParser jp = factory.createJsonParser(in);
        this.response = toString(jp);
    }

    public void processError(InputStream in) throws IOException {
        JsonParser jp = factory.createJsonParser(in);
        toException(jp);
    }

    public String getResponse() {
        return response;
    }

    private String toString(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        // first token must be a START_OBJECT token
        if (token != JsonToken.START_OBJECT) {
            throw new IOException("JSON first token is not START_OBJECT");
        }
        StringBuilder sb = new StringBuilder();
        while (token != null) {
            switch (token) {
                case VALUE_STRING:
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                case VALUE_NULL:
                case VALUE_TRUE:
                case VALUE_FALSE:
                    sb.append(parser.getText());
                    break;
            }
            token = parser.nextToken();
        }
        return sb.toString();
    }

    private void toException(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        // first token must be a START_OBJECT token
        if (token != JsonToken.START_OBJECT) {
            throw new IOException("JSON first token is not START_OBJECT");
        }
        this.exception = new IOException("unknown");
        while (token != null) {
            switch (token) {
                case VALUE_STRING:
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                case VALUE_NULL:
                case VALUE_TRUE:
                case VALUE_FALSE:
                    // get any JSONData value and wrap it into an IOException
                    this.exception = new IOException(parser.getText());
                    break;
            }
            token = parser.nextToken();
        }
        throw exception;
    }
    
    protected StringData getMappings() throws IOException {
        String s = "/org/xbib/elasticsearch/mappings/" + index;
        if (type != null) {
            s += "/" + type;
        }
        InputStream in = getClass().getClassLoader().getResourceAsStream(s);
        if (in == null) {
            throw new IOException("mapping file not found: " + s);
        }
        return new StringData(in);
    }    
}
