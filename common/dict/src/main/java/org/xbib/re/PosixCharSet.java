package org.xbib.re;

class PosixCharSet {
    public static final CharSet Lower =
            new CharSetBuilder().add('a', 'z').build();

    public static final CharSet Upper =
            new CharSetBuilder().add('A', 'Z').build();

    public static final CharSet ASCII =
            new CharSetBuilder().add('\u0000', '\u007F').build();

    public static final CharSet Alpha =
            new CharSetBuilder().add(Lower).add(Upper).build();

    public static final CharSet Digit =
            new CharSetBuilder().add('0', '9').build();

    public static final CharSet Alnum =
            new CharSetBuilder().add(Alpha).add(Digit).build();

    public static final CharSet Punct =
            new CharSetBuilder().add("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~").build();

    public static final CharSet Graph =
            new CharSetBuilder().add(Alnum).add(Punct).build();

    public static final CharSet Print =
            new CharSetBuilder().add(Graph).add(' ').build();

    public static final CharSet Blank =
            new CharSetBuilder().add(" \t").build();

    public static final CharSet Cntrl =
            new CharSetBuilder().add('\u0000', '\u001F').add("\u007F").build();

    public static final CharSet XDigit =
            new CharSetBuilder().add(Digit).add('a', 'f').add('A', 'F').build();

    public static final CharSet Space =
            new CharSetBuilder().add(" \t\n\u000B\f\r").build();
}
