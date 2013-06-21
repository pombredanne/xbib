package org.xbib.strings.encode;

import org.xbib.io.util.MessageDigestUtil;
import org.xbib.io.util.URIUtil;

import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class BaseformEncoder {

    private static final Pattern nonword = Pattern.compile("[\\P{IsWord}]");

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final Charset ISO88591 = Charset.forName("ISO-8859-1");

    private static final MessageDigestUtil digest = new MessageDigestUtil("MD5");

    public static String normalizedName(String name) {
        String s = Normalizer.normalize(new String(name.getBytes(ISO88591), UTF8), Normalizer.Form.NFKC);
        s = URIUtil.decode(s, UTF8);
        s = nonword.matcher(s).replaceAll("");
        s = s.toLowerCase(Locale.ENGLISH);
        return s;
    }

    public static String createBaseIdentifier(String value) {
        synchronized (digest) {
            return digest.reset().add(normalizedName(value)).toString();
        }
    }

}
