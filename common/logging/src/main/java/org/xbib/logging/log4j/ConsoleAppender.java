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

import java.io.IOException;
import java.io.OutputStream;
import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.helpers.LogLog;
import org.xbib.logging.Loggers;

/**
 * ConsoleAppender appends log events to <code>System.out</code> or
 * <code>System.err</code> using a layout specified by the user. The
 * default target is <code>System.out</code>.
 */
public class ConsoleAppender extends WriterAppender {

    public static final String SYSTEM_OUT = "System.out";
    public static final String SYSTEM_ERR = "System.err";

    protected String target = SYSTEM_OUT;

    /**
     * Determines if the appender honors reassignments of System.out
     * or System.err made after configuration.
     */
    private boolean follow = true;

    /**
     * Constructs an unconfigured appender.
     */
    public ConsoleAppender() {
    }

    /**
     * Creates a configured appender.
     *
     * @param layout layout, may not be null.
     */
    public ConsoleAppender(Layout layout) {
        this(layout, SYSTEM_OUT);
    }

    /**
     * Creates a configured appender.
     *
     * @param layout layout, may not be null.
     * @param target target, either "System.err" or "System.out".
     */
    public ConsoleAppender(Layout layout, String target) {
        setLayout(layout);
        setTarget(target);
        activateOptions();
    }

    /**
     * Sets the value of the <b>Target</b> option. Recognized values
     * are "System.out" and "System.err". Any other value will be
     * ignored.
     */
    public void setTarget(String value) {
        String v = value.trim();

        if (SYSTEM_OUT.equalsIgnoreCase(v)) {
            target = SYSTEM_OUT;
        } else if (SYSTEM_ERR.equalsIgnoreCase(v)) {
            target = SYSTEM_ERR;
        } else {
            targetWarn(value);
        }
    }

    /**
     * Returns the current value of the <b>Target</b> property. The
     * default value of the option is "System.out".
     * <p/>
     * See also {@link #setTarget}.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets whether the appender honors reassignments of System.out
     * or System.err made after configuration.
     *
     * @param newValue if true, appender will use value of System.out or
     *                 System.err in force at the time when logging events are appended.
     */
    public final void setFollow(final boolean newValue) {
        follow = newValue;
    }

    /**
     * Gets whether the appender honors reassignments of System.out
     * or System.err made after configuration.
     *
     * @return true if appender will use value of System.out or
     *         System.err in force at the time when logging events are appended.
     */
    public final boolean getFollow() {
        return follow;
    }

    void targetWarn(String val) {
        LogLog.warn("[" + val + "] should be System.out or System.err.");
        LogLog.warn("Using previously set target, System.out by default.");
    }

    /**
     * Prepares the appender for use.
     */
    @Override
    public void activateOptions() {
        if (follow) {
            if (target.equals(SYSTEM_ERR)) {
                setWriter(createWriter(new SystemErrStream()));
            } else {
                setWriter(createWriter(new SystemOutStream()));
            }
        } else {
            if (target.equals(SYSTEM_ERR)) {
                setWriter(createWriter(System.err));
            } else {
                setWriter(createWriter(System.out));
            }
        }

        super.activateOptions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void closeWriter() {
        if (follow) {
            super.closeWriter();
        }
    }


    /**
     * An implementation of OutputStream that redirects to the
     * current System.err.
     */
    private static class SystemErrStream extends OutputStream {
        public SystemErrStream() {
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
            System.err.flush();
        }

        @Override
        public void write(final byte[] b) throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.err.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len)
                throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.err.write(b, off, len);
        }

        @Override
        public void write(final int b) throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.err.write(b);
        }
    }

    /**
     * An implementation of OutputStream that redirects to the
     * current System.out.
     */
    private static class SystemOutStream extends OutputStream {
        public SystemOutStream() {
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
            System.out.flush();
        }

        @Override
        public void write(final byte[] b) throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.out.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len)
                throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.out.write(b, off, len);
        }

        @Override
        public void write(final int b) throws IOException {
            if (!Loggers.consoleLoggingEnabled()) {
                return;
            }
            System.out.write(b);
        }
    }

}
