/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.xbib.logging;

/**
 * A set of utilities around Logging.
 *
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
