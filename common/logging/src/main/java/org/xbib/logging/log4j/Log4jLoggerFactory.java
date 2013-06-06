
package org.xbib.logging.log4j;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 *
 */
public class Log4jLoggerFactory extends LoggerFactory {

    @Override
    protected Logger newInstance(String prefix, String name) {
        final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(name);
        return new Log4jLogger(prefix, logger);
    }
}
