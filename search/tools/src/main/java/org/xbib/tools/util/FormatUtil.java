package org.xbib.tools.util;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtil {

    public static String convertFileSize(double size) {
        return convertFileSize(size, Locale.getDefault());
    }
    
    public static String convertFileSize(double size, Locale locale) {
        String strSize;
        long kb = 1024;
        long mb = 1024 * kb;
        long gb = 1024 * mb;
        long tb = 1024 * gb;

        NumberFormat formatter = NumberFormat.getNumberInstance(locale);
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);

        if (size < kb) {
            strSize = size + " bytes";
        } else if (size < mb) {
            strSize = formatter.format(size / kb) + " KB";
        } else if (size < gb) {
            strSize = formatter.format(size / mb) + " MB";
        } else if (size < tb) {
            strSize = formatter.format(size / gb) + " GB";
        } else {
            strSize = formatter.format(size / tb) + " TB";
        }
        return strSize;
    }


}
