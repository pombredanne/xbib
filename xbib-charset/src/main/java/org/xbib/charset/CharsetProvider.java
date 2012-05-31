package org.xbib.charset;

import java.lang.ref.SoftReference;

/**
 * Extra bibliographic character sets.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class CharsetProvider extends AbstractCharsetProvider {

    /**
     * The reference to the character set instance.
     * If there are no remaining references to this instance,
     * the character set will be removed by the garbage collector.
     */
    static volatile SoftReference instance = null;

    /**
     * Constructor
     */
    public CharsetProvider() {
        charset("ANSI-Z39_47", "ANSI_Z39_47",
            new String[] { "ANSI_Z39_47", "ANSI-Z39-47", "Z39_47", "Z39-47", "ANSEL", "Ansel", "ansel"});
        charset("x-MAB", "MabCharset",
            new String[] { "x-mab",  "ISO-5426", "ISO_5426", "ISO_5426:1983", "5426-1983", "MAB2" } );
        charset("PICA", "Pica", 
            new String[] { "Pica", "pica"} );
        charset("x-PICA", "PicaCharset",
            new String[] { "x-pica" } );
        instance = new SoftReference(this);
    }

    /**
     * List all aliases defined for a character set.
     * @param s the name of the character set
     * @return an alias string array
     */
    public static String[] aliasesFor(String s) {
        SoftReference softreference = instance;
        CharsetProvider charsets = null;
        if (softreference != null) {
            charsets = (CharsetProvider) softreference.get();
        }
        if (charsets == null) {
            charsets = new CharsetProvider();
            instance = new SoftReference(charsets);
        }
        return charsets.aliases(s);
    }
}
