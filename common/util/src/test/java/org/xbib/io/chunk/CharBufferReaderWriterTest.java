package org.xbib.io.chunk;


import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.CharBuffer;

public class CharBufferReaderWriterTest {

    @Test
    public void testCharBuffer() throws InterruptedException {
        final Logger logger = LoggerFactory.getLogger("CharBufferReaderWriter");
        final CharBufferReaderWriter c = new CharBufferReaderWriter();
        new Thread() {
            public void run() {
                logger.info("sending");
                int count = 0;
                try {
                    for (int i = 1; i < 10000; i++) {
                        String s = "test" + i + " ";
                        c.append(s);
                        count += s.length();
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                logger.info("sending done, count= {}", count);
            }
        }.start();
        new Thread() {
            public void run() {
                logger.info("receiving");
                int count = 0;
                try {
                    int n;
                    CharBuffer buffer = CharBuffer.allocate(1024);
                    do {
                        c.waitFor();
                        n = c.read(buffer);
                        if (n >= 0) {
                            count += n;
                        }
                        logger.debug("receive count = {}", count);
                        buffer.clear();
                    } while (n > 0);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                logger.info("receive done, count = {}", count);
            }
        }.start();
        Thread.sleep(2000L);
    }
}
