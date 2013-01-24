package org.xbib.applet.util;


import java.io.UnsupportedEncodingException;

public class URLUtil {

    private static final String hex = "0123456789ABCDEF";
    private static final String encoding = "UTF-8";

    public static String renderQueryString(String... params) throws UnsupportedEncodingException {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < params.length; i += 2) {
            String key = params[i];
            if (key != null) {
                if (out.length() > 0) {
                    out.append("&");
                }
                out.append(key);
                String value = params[i + 1] != null ? encode(params[i + 1], encoding) : null;
                if ((value != null) && (value.length() > 0)) {
                    out.append("=").append(value);
                }
            }
        }
        return out.toString();
    }

    public static String encode(String s, String encoding)
            throws UnsupportedEncodingException {
        int length = s.length();
        int start = 0;
        int i = 0;
        StringBuilder result = new StringBuilder(length);
        while (true) {
            while ((i < length) && isSafe(s.charAt(i))) {
                i++;
            }
            result.append(s.substring(start, i));
            if (i >= length) {
                return result.toString();
            } else if (s.charAt(i) == ' ') {
                result.append('+');
                i++;
            } else {
                start = i;
                char c;
                while ((i < length) && ((c = s.charAt(i)) != ' ') && !isSafe(c)) {
                    i++;
                }
                String unsafe = s.substring(start, i);
                byte[] bytes = unsafe.getBytes(encoding);
                for (int j = 0; j < bytes.length; j++) {
                    result.append('%');
                    int val = bytes[j];
                    result.append(hex.charAt((val & 0xf0) >> 4));
                    result.append(hex.charAt(val & 0x0f));
                }
            }
            start = i;
        }
    }

    private static boolean isSafe(char c) {
        return (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))
                || ((c >= '0') && (c <= '9')) || (c == '-') || (c == '_') || (c == '.') || (c == '*'));
    }
}
