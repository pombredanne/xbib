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
