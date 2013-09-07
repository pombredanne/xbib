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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A custom log formatter
 *
 */
public class CustomFormatter extends Formatter {

    /**
     * Format a log record
     *
     * @param record the log record
     *
     * @return the formatted log record
     */
    @Override
    public synchronized String format(final LogRecord record) {
        StringBuilder sb = new StringBuilder();
        Date date = new Date(record.getMillis());
        sb.append(date).append(" [").append(Thread.currentThread().getName()).append("] ").append(record.getLevel().getName()).append(" ").append(record.getSourceClassName()).append(".").append(record.getSourceMethodName()).append(" ").append(formatMessage(record)).append("\n");
        appendThrowable(sb, record.getThrown(), 0, true);
        return sb.toString();
    }

    private void appendThrowable(StringBuilder buf, Throwable t, int level, boolean details) {
        if (t == null) {
            return;
        }
        try {
            if (t.getMessage() != null && t.getMessage().length() > 0) {
                if (details && (level > 0)) {
                    buf.append("\n\nCaused by\n");
                }
                buf.append(t.getMessage());
            }
            if (details) {
                buf.append(((t.getMessage() != null) && (t.getMessage().length() == 0)) ? "\n\nCaused by " : "\n\n");
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                buf.append(sw.toString());
            }
            Method method = t.getClass().getMethod("getCause", new Class[]{});
            Throwable cause = (Throwable) method.invoke(t, (Object) null);
            appendThrowable(buf, cause, level + 1, details);
        } catch (Exception ex) {
        }
    }
}
