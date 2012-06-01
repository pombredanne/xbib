package org.xbib.sru;

import java.io.Closeable;
import org.xbib.io.http.netty.HttpResultProcessor;

public interface SRUResultProcessor extends HttpResultProcessor, Closeable {
    
}
