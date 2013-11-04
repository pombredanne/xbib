package org.xbib.re;

abstract class JavaCharSet extends GenericCharSet {

    public static final CharSet ISOControl =
            new CharSetBuilder().add('\u0000', '\u001F').add('\u007F', '\u009F').build();

    public static final CharSet Digit = new CharSetBuilder().add(48, 57).add(1632, 1641).add(1776, 1785).add(2406, 2415).add(2534, 2543).add(2662, 2671).add(2790, 2799).add(2918, 2927).add(3047, 3055).add(3174, 3183).add(3302, 3311).add(3430, 3439).add(3664, 3673).add(3792, 3801).add(3872, 3881).add(4160, 4169).add(4969, 4977).add(6112, 6121).add(6160, 6169).add(6470, 6479).add(65296, 65305).add(66720, 66729).add(120782, 120831).build();

    public static final CharSet IdentifierIgnorable = new CharSetBuilder().add(0, 8).add(14, 27).add(127, 159).add(173).add(1536, 1539).add(1757).add(1807).add(6068, 6069).add(8204, 8207).add(8234, 8238).add(8288, 8291).add(8298, 8303).add(65279, 65279).add(65529, 65531).add(119155, 119162).add(917505).add(917536, 917631).build();

    public static final CharSet SpaceChar = new CharSetBuilder().add(32).add(160).add(5760).add(6158).add(8192, 8203).add(8232, 8233).add(8239).add(8287).add(12288).build();

    public static final CharSet TitleCase = new CharSetBuilder().add(453).add(456).add(459).add(498).add(8072, 8079).add(8088, 8095).add(8104, 8111).add(8124).add(8140).add(8188).build();

    public static final CharSet Whitespace = new CharSetBuilder().add(9, 13).add(28, 32).add(5760, 5760).add(6158).add(8192, 8198).add(8200, 8203).add(8232, 8233).add(8287).add(12288).build();

    public static final CharSet Letter = new JavaCharSet(90547, 195101) {
        public boolean contains(int c) {
            return Character.isLetter(c);
        }
    };

    public static final CharSet LetterOrDigit = new JavaCharSet(90815, 195101) {
        public boolean contains(int c) {
            return Character.isLetterOrDigit(c);
        }
    };

    public static final CharSet LowerCase = new JavaCharSet(1415, 120777) {
        public boolean contains(int c) {
            return Character.isLowerCase(c);
        }
    };

    public static final CharSet UpperCase = new JavaCharSet(1190, 120744) {
        public boolean contains(int c) {
            return Character.isUpperCase(c);
        }
    };

    public static final CharSet JavaIdentifierPart = new JavaCharSet(92040, 917999) {
        public boolean contains(int c) {
            return Character.isJavaIdentifierPart(c);
        }
    };

    public static final CharSet JavaIdentifierStart = new JavaCharSet(90648, 195101) {
        public boolean contains(int c) {
            return Character.isJavaIdentifierStart(c);
        }
    };

    public static final CharSet Mirrored = new JavaCharSet(492, 65379) {
        public boolean contains(int c) {
            return Character.isMirrored(c);
        }
    };

    public static final CharSet UnicodeIdentifierPart = new JavaCharSet(92004, 917999) {
        public boolean contains(int c) {
            return Character.isUnicodeIdentifierPart(c);
        }
    };

    public static final CharSet UnicodeIdentifierStart = new JavaCharSet(90600, 195101) {
        public boolean contains(int c) {
            return Character.isUnicodeIdentifierStart(c);
        }
    };

    private final int cardinality;

    private JavaCharSet(int cardinality, int last) {
        super(last);
        this.cardinality = cardinality;
    }

    @Override
    public int cardinality() {
        return cardinality;
    }
}
