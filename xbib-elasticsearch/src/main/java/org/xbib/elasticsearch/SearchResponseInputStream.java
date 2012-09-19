package org.xbib.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class SearchResponseInputStream extends StreamOutput {

    private static final Logger logger = LoggerFactory.getLogger(SearchResponseInputStream.class.getName());
    
    private StreamByteBuffer buffer;
    
    public SearchResponseInputStream() {
        this.buffer = new StreamByteBuffer();
    }
    
    @Override
    public void writeByte(byte b) throws IOException {
        //logger.info("got byte {}", b);
        buffer.getOutputStream().write(b);
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {
        buffer.getOutputStream().write(b, offset, length);
        //logger.info("got bytes {}", new String(Arrays.copyOfRange(b, offset, length)));
    }

    @Override
    public void flush() throws IOException {
        logger.info("got flush");
    }

    @Override
    public void close() throws IOException {
        logger.info("got close");
    }

    @Override
    public void reset() throws IOException {
        logger.info("got reset");
    }

    public byte[] getByteArray() throws IOException {
        buffer.getOutputStream().flush();
        return buffer.readAsByteArray();
    }
    
    public InputStream getInputStream() throws IOException {
        buffer.getOutputStream().flush();
        return buffer.getInputStream();
    }
    
    public String getString() throws IOException {
        buffer.getOutputStream().flush();
        return buffer.readAsString("UTF-8");
    }
    
}
