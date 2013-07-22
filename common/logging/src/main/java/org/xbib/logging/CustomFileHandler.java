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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * Implementation of <b>Handler</b> that appends log messages to a file
 * named {prefix}.{date}.{suffix} in a configured directory, with an optional
 * preceding timestamp. Taken from org.apache.juli (part of Apache Tomcat)
 */
public class CustomFileHandler extends Handler {

    /**
     * The as-of date for the currently open log file, or a zero-length
     * string if there is no open log file.
     */
    private String date = "";
    /** The directory in which log files are created. */
    private String directory = null;
    /** The prefix that is added to log file filenames. */
    private String prefix = null;
    /** The suffix that is added to log file filenames. */
    private String suffix = null;
    /** The PrintWriter to which we are currently logging, if any. */
    private PrintWriter writer = null;
    /** write lock for flushing write before output stream is closed */
    private final Object lock = new Object();

    /**
     * Creates a new CustomFileHandler object.
     */
    public CustomFileHandler() {
        configure();
        String className = CustomFileHandler.class.getName();
        // Retrieve configuration of logging file name
        this.directory = getProperty(className + ".directory", "logs");
        this.prefix = getProperty(className + ".prefix", "default.");
        this.suffix = getProperty(className + ".suffix", ".log");
        open();
    }

    /**
     * Creates a new CustomFileHandler object.
     *
     * @param directory the directory for logging
     * @param prefix the prefix of the log file name
     * @param suffix the suffix of the log file name
     */
    public CustomFileHandler(String directory, String prefix, String suffix) {
        configure(); // date, filter, formatter, errormanager
        this.directory = directory;
        this.prefix = prefix;
        this.suffix = suffix;
        open();
    }

    /**
     * Format and publish a <tt>LogRecord</tt>.
     *
     * @param record description of the log event
     */
    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        // Construct the timestamp we will use, if requested
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String tsString = ts.toString().substring(0, 19);
        String tsDate = tsString.substring(0, 10);

        String className = CustomFileHandler.class.getName();

        // If date has changed, rotate log files
        try {
            synchronized (lock) {
                if (!date.equals(tsDate)) {
                    close();
                    this.date = tsDate;
                    open();
                }
            }
        } catch (Exception e) {
            reportError(null, e, ErrorManager.CLOSE_FAILURE);
            return;
        }

        String result = null;

        try {
            result = getFormatter().format(record);
        } catch (Exception e) {
            reportError(null, e, ErrorManager.FORMAT_FAILURE);
            return;
        }

        try {
            synchronized (lock) {
                if ((result != null) && (writer != null)) {
                    writer.write(result);
                    writer.flush();
                }
            }
        } catch (Exception e) {
            reportError(null, e, ErrorManager.WRITE_FAILURE);

            return;
        }
    }

    /**
     * Close the currently open log file (if any).
     */
    @Override
    public void close() {
        try {
            if (writer == null) {
                return;
            }

            synchronized (lock) {
                writer.write(getFormatter().getTail(this));
                writer.flush();
                writer.close();
                writer = null;
            }
            date = "";
        } catch (Exception e) {
            reportError(null, e, ErrorManager.CLOSE_FAILURE);
        }
    }

    /**
     * Flush the writer.
     */
    @Override
    public void flush() {
        try {
            if (writer != null) {
                writer.flush();
            }
        } catch (Exception e) {
            reportError(null, e, ErrorManager.FLUSH_FAILURE);
        }
    }

    /**
     * Configure from <code>LogManager</code> properties.
     */
    private void configure() {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String tsString = ts.toString().substring(0, 19);
        this.date = tsString.substring(0, 10);

        LogManager manager = LogManager.getLogManager();
        String className = CustomFileHandler.class.getName();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        // Get logging level for the handler
        setLevel(Level.parse(getProperty(className + ".level", "" + Level.ALL)));

        // Get filter configuration
        String filterName = getProperty(className + ".filter", null);

        if (filterName != null) {
            try {
                setFilter((Filter) cl.loadClass(filterName).newInstance());
            } catch (Exception e) {
                // Ignore
            }
        }

        // Set formatter
        String formatterName = getProperty(className + ".formatter", null);

        if (formatterName != null) {
            try {
                setFormatter((Formatter) cl.loadClass(formatterName).newInstance());
            } catch (Exception e) {
                // Ignore
            }
        } else {
            setFormatter(new CustomFormatter());
        }

        // Set error manager
        setErrorManager(new ErrorManager());
    }

    /**
     * Get property
     *
     * @param name the name of the property
     * @param defaultValue the default value if the property value does not exist
     *
     * @return the property value
     */
    private String getProperty(String name, String defaultValue) {
        String value = LogManager.getLogManager().getProperty(name);

        if (value == null) {
            value = defaultValue;
        } else {
            value = value.trim();
        }

        return value;
    }

    /**
     * Open the new log file for the date specified by
     * <code>date</code>.
     */
    private void open() {
        // Create the directory if necessary
        File dir = new File(directory);
        dir.mkdirs();
        // Open the current log file
        try {
            String pathname = dir.getAbsolutePath() + File.separator + prefix + date + suffix;
            this.writer = new PrintWriter(new FileWriter(pathname, true), true);
            if (getFormatter() != null) {
                writer.write(getFormatter().getHead(this));
            }
        } catch (Exception e) {
            reportError(null, e, ErrorManager.OPEN_FAILURE);
            this.writer = null;
        }
    }
}
