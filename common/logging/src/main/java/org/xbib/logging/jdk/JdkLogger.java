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
package org.xbib.logging.jdk;

import org.xbib.logging.support.AbstractLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class JdkLogger extends AbstractLogger {

    private final Logger logger;

    private final String name;

    public JdkLogger(String prefix, String name, Logger logger) {
        super(prefix);
        this.logger = logger;
        this.name = name;
    }

    @Override
    public void setLevel(String level) {
        if ("error".equalsIgnoreCase(level)) {
            logger.setLevel(Level.SEVERE);
        } else if ("warn".equalsIgnoreCase(level)) {
            logger.setLevel(Level.WARNING);
        } else if ("info".equalsIgnoreCase(level)) {
            logger.setLevel(Level.INFO);
        } else if ("debug".equalsIgnoreCase(level)) {
            logger.setLevel(Level.FINE);
        } else if ("trace".equalsIgnoreCase(level)) {
            logger.setLevel(Level.FINE);
        }
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINEST);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    protected void internalTrace(String msg) {
        logger.logp(Level.FINEST, name, null, msg);
    }

    @Override
    protected void internalTrace(String msg, Throwable cause) {
        logger.logp(Level.FINEST, name, null, msg, cause);
    }

    @Override
    protected void internalDebug(String msg) {
        logger.logp(Level.FINE, name, null, msg);
    }

    @Override
    protected void internalDebug(String msg, Throwable cause) {
        logger.logp(Level.FINE, name, null, msg, cause);
    }

    @Override
    protected void internalInfo(String msg) {
        logger.logp(Level.INFO, name, null, msg);
    }

    @Override
    protected void internalInfo(String msg, Throwable cause) {
        logger.logp(Level.INFO, name, null, msg, cause);
    }

    @Override
    protected void internalWarn(String msg) {
        logger.logp(Level.WARNING, name, null, msg);
    }

    @Override
    protected void internalWarn(String msg, Throwable cause) {
        logger.logp(Level.WARNING, name, null, msg, cause);
    }

    @Override
    protected void internalError(String msg) {
        logger.logp(Level.SEVERE, name, null, msg);
    }

    @Override
    protected void internalError(String msg, Throwable cause) {
        logger.logp(Level.SEVERE, name, null, msg, cause);
    }
}
