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
package org.xbib.logging;

import org.xbib.logging.jdk.JdkLoggerFactory;
import org.xbib.logging.log4j.Log4jLoggerFactory;
import org.xbib.logging.slf4j.Slf4jLoggerFactory;

public abstract class LoggerFactory {

    private static volatile LoggerFactory defaultFactory = new JdkLoggerFactory();

    static {
        try {
            Class.forName("org.slf4j.Logger");
            defaultFactory = new Slf4jLoggerFactory();
        } catch (Throwable e1) {
            // no slf4j
            try {
                Class<?> loggerClazz = Class.forName("org.apache.log4j.Logger");
                // below will throw a NoSuchMethod failure with using slf4j log4j bridge
                loggerClazz.getMethod("setLevel", Class.forName("org.apache.log4j.Level"));
                defaultFactory = new Log4jLoggerFactory();
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                // no log4j
            }
        }
    }

    /**
     * Changes the default factory.
     */
    public static void setDefaultFactory(LoggerFactory defaultFactory) {
        if (defaultFactory == null) {
            throw new NullPointerException("defaultFactory");
        }
        LoggerFactory.defaultFactory = defaultFactory;
    }

    public static Logger getLogger(String prefix, String name) {
        return defaultFactory.newInstance(prefix == null ? null : prefix.intern(), name.intern());
    }

    public static Logger getLogger(String name) {
        return defaultFactory.newInstance(name.intern());
    }

    public Logger newInstance(String name) {
        return newInstance(null, name);
    }

    protected abstract Logger newInstance(String prefix, String name);
}
