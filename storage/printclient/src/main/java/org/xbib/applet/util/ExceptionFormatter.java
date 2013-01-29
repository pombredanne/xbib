package org.xbib.applet.util;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

/**
 * Format java exception messages and stack traces.
 *
 */
public final class ExceptionFormatter {

    private ExceptionFormatter() {
    }

    public static void appendException(StringBuilder buf, Throwable exception,
            int level, boolean details) {
        try {
            if (((exception != null) && (exception.getMessage() != null))
                    && (exception.getMessage().length() > 0)) {
                if (details && (level > 0)) {
                    buf.append("\n\nCaused by\n");
                }
                buf.append(exception.getMessage());
            }
            if (details) {
                if (exception != null) {
                    if ((exception.getMessage() != null)
                            && (exception.getMessage().length() == 0)) {
                        buf.append("\n\nCaused by ");
                    } else {
                        buf.append("\n\n");
                    }
                }
                StringWriter sw = new StringWriter();
                if (exception != null) {
                    exception.printStackTrace(new PrintWriter(sw));
                }
                buf.append(sw.toString());
            }
            if (exception != null) {
                Method method = exception.getClass().getMethod("getCause",
                        new Class[]{});
                Throwable cause = (Throwable) method.invoke(exception,
                        (Object) null);

                if (cause != null) {
                    appendException(buf, cause, level + 1, details);
                }
            }
        } catch (Exception ex) {
        }
    }

    /**
     * Format exception with stack trace
     *
     * @param t the thrown object
     *
     * @return the formatted exception
     */
    public static String format(Throwable t) {
        StringBuilder sb = new StringBuilder();
        appendException(sb, t, 0, true);
        return sb.toString();
    }
}
