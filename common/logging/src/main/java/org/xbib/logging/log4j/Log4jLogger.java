
package org.xbib.logging.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xbib.logging.support.AbstractLogger;

/**
 *
 */
public class Log4jLogger extends AbstractLogger {

    private final org.apache.log4j.Logger logger;

    public Log4jLogger(String prefix, Logger logger) {
        super(prefix);
        this.logger = logger;
    }

    @Override
    public void setLevel(String level) {
        if ("error".equalsIgnoreCase(level)) {
            logger.setLevel(Level.ERROR);
        } else if ("warn".equalsIgnoreCase(level)) {
            logger.setLevel(Level.WARN);
        } else if ("info".equalsIgnoreCase(level)) {
            logger.setLevel(Level.INFO);
        } else if ("debug".equalsIgnoreCase(level)) {
            logger.setLevel(Level.DEBUG);
        } else if ("trace".equalsIgnoreCase(level)) {
            logger.setLevel(Level.TRACE);
        }
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    @Override
    protected void internalTrace(String msg) {
        logger.trace(msg);
    }

    @Override
    protected void internalTrace(String msg, Throwable cause) {
        logger.trace(msg, cause);
    }

    @Override
    protected void internalDebug(String msg) {
        logger.debug(msg);
    }

    @Override
    protected void internalDebug(String msg, Throwable cause) {
        logger.debug(msg, cause);
    }

    @Override
    protected void internalInfo(String msg) {
        logger.info(msg);
    }

    @Override
    protected void internalInfo(String msg, Throwable cause) {
        logger.info(msg, cause);
    }

    @Override
    protected void internalWarn(String msg) {
        logger.warn(msg);
    }

    @Override
    protected void internalWarn(String msg, Throwable cause) {
        logger.warn(msg, cause);
    }

    @Override
    protected void internalError(String msg) {
        logger.error(msg);
    }

    @Override
    protected void internalError(String msg, Throwable cause) {
        logger.error(msg, cause);
    }
}
