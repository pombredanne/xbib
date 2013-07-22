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

/**
 * A set of utilities around Logging.
 *
 */
public class Loggers {

    private final static String commonPrefix = System.getProperty("org.xbib.logger.prefix", "org.xbib.");

    public static final String SPACE = " ";

    private static boolean consoleLoggingEnabled = true;

    public static void disableConsoleLogging() {
        consoleLoggingEnabled = false;
    }

    public static void enableConsoleLogging() {
        consoleLoggingEnabled = true;
    }

    public static boolean consoleLoggingEnabled() {
        return consoleLoggingEnabled;
    }

    public static Logger getLogger(Logger parentLogger, String s) {
        return LoggerFactory.getLogger(parentLogger.getPrefix(), getLoggerName(parentLogger.getName() + s));
    }

    public static Logger getLogger(String s) {
        return LoggerFactory.getLogger(s);
    }

    public static Logger getLogger(Class clazz) {
        return LoggerFactory.getLogger(getLoggerName(buildClassLoggerName(clazz)));
    }

    public static Logger getLogger(Class clazz, String... prefixes) {
        return getLogger(buildClassLoggerName(clazz), prefixes);
    }

    public static Logger getLogger(String name, String... prefixes) {
        String prefix = null;
        if (prefixes != null && prefixes.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String prefixX : prefixes) {
                if (prefixX != null) {
                    if (prefixX.equals(SPACE)) {
                        sb.append(" ");
                    } else {
                        sb.append("[").append(prefixX).append("]");
                    }
                }
            }
            if (sb.length() > 0) {
                sb.append(" ");
                prefix = sb.toString();
            }
        }
        return LoggerFactory.getLogger(prefix, getLoggerName(name));
    }

    private static String buildClassLoggerName(Class clazz) {
        String name = clazz.getName();
        if (name.startsWith("org.xbib.")) {
            name = clazz.getPackage().getName();
        }
        return name;
    }

    private static String getLoggerName(String name) {
        if (name.startsWith("org.xbib.")) {
            name = name.substring("org.xbib.".length());
        }
        return commonPrefix + name;
    }
}
