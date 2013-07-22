/**
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
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
