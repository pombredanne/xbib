package org.xbib.sru;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.xbib.io.negotiate.MediaRangeSpec;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ContentTypeNegotiatorTest extends Assert {
    
    private static final Logger logger = Logger.getLogger(ContentTypeNegotiatorTest.class.getName());

    @Test
    public void testNegotiator() throws Exception {
        SRUContentTypeNegotiator neg = new SRUContentTypeNegotiator();
        MediaRangeSpec mediaType = neg.getBestMatch(null);
        logger.log(Level.INFO, "mimeType = null mediaType = " + mediaType.getMediaType());        
    }

    @Test
    public void testTextXMLNegotiator() throws Exception {
        String mimeType = "text/xml";
        SRUContentTypeNegotiator neg = new SRUContentTypeNegotiator();
        MediaRangeSpec mediaType = neg.getBestMatch(mimeType);
        logger.log(Level.INFO, "mimeType = "+mimeType+" mediaType = " + mediaType.getMediaType());        
    }
    
    @Test
    public void testAppNegotiator() throws Exception {
        String mimeType = "application/sru+xml";
        SRUContentTypeNegotiator neg = new SRUContentTypeNegotiator();
        MediaRangeSpec mediaType = neg.getBestMatch(mimeType);
        logger.log(Level.INFO, "mimeType = "+mimeType+" mediaType = " + mediaType.getMediaType());        
    }
    
    @Test
    public void testBrowserNegotiator() throws Exception {
        String mimeType = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        SRUContentTypeNegotiator neg = new SRUContentTypeNegotiator();
        MediaRangeSpec mediaType = neg.getBestMatch(mimeType, "Mozilla");
        logger.log(Level.INFO, "mimeType = "+mimeType+" mediaType = " + mediaType.getMediaType());        
    }
    
}
