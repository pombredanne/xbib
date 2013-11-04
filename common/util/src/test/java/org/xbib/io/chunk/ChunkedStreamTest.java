package org.xbib.io.chunk;

import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class ChunkedStreamTest {

    @Test
    public void testChunk() throws InterruptedException {
        final Logger logger = LoggerFactory.getLogger("Chunk");
        final ChunkedStream cs = new ChunkedStream();
        final InputStream in = cs.getInputStream();
        final OutputStream out = cs.getOutputStream();
        new Thread() {
            public void run() {
                logger.info("sending");
                int count = 0;
                try {
                    for (int i = 1; i < 10000; i++) {
                        String s = "test" + i + " ";
                        out.write(s.getBytes());
                        out.flush();
                        //logger.debug("written {}", s);
                        count += s.length();
                    }
                    out.close();
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
                    int unread = Integer.MAX_VALUE;
                    while (unread > 0) {
                        byte[] buf = cs.readAsByteArray();
                        count += buf.length;
                        unread = in.available();
                        logger.debug("receive count = {}, unread = {}", count, unread);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                logger.info("receive done, count = {}", count);
            }
        }.start();
        Thread.sleep(2000L);
    }

    @Test
    public void testBlockingChunk() throws InterruptedException {
        final Logger logger = LoggerFactory.getLogger("BlockingChunk");
        final ChunkedStream cs = new BlockingChunkedStream();
        final InputStream in = cs.getInputStream();
        final OutputStream out = cs.getOutputStream();
        new Thread() {
            public void run() {
                logger.info("sending");
                int count = 0;
                try {
                    for (int i = 1; i < 10000; i++) {
                        String s = "test" + i + " ";
                        out.write(s.getBytes());
                        out.flush();
                        //logger.debug("written {}", s);
                        count += s.length();
                    }
                    out.close();
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
                    int unread = Integer.MAX_VALUE;
                    while (unread > 0) {
                        byte[] buf = cs.readAsByteArray();
                        count += buf.length;
                        unread = in.available();
                        logger.debug("receive count = {}, unread = {}", count, unread);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                logger.info("receive done, count = {}", count);
            }
        }.start();
        Thread.sleep(2000L);
    }

    @Test
    public void testAppendable() throws Exception {
        final Logger logger = LoggerFactory.getLogger("Appendable");
        final ChunkedStream cs = new BlockingChunkedStream();
        final Appendable appendable = cs.getAppendable();
        new Thread() {
            public void run() {
                logger.info("sending");
                int count = 0;
                try {
                    for (int i = 1; i < 10000; i++) {
                        String s = "test" + i + " ";
                        appendable.append(s);
                        //logger.debug("written {}", s);
                        count += s.length();
                    }
                    cs.closeProduction();
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
                    int unread = Integer.MAX_VALUE;
                    while (unread > 0) {
                        String s = cs.readAsString();
                        if (s != null) {
                            count += s.length();
                            unread = cs.available();
                            logger.debug("receive count = {}, unread = {}", count, unread);
                        }
                    }
                    cs.closeConsumption();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                logger.info("receive done, count = {}", count);
            }
        }.start();
        Thread.sleep(2000L);
    }

}
