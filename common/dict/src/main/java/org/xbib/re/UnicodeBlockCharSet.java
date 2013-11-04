package org.xbib.re;

import java.util.Map;

public class UnicodeBlockCharSet {

    private static final Map<Character.UnicodeBlock, CharSet> blockMap = Generics.newHashMap();

    public static CharSet get(Character.UnicodeBlock block) {
        CharSet cset = blockMap.get(block);
        return (cset != null) ? cset : EmptyCharSet.INSTANCE;
    }

    private static void build(int start, int end) {
        if (blockMap.put(Character.UnicodeBlock.of(start), new CharSetBuilder().add(start, end).build()) != null) {
            throw new IllegalStateException("replacing " + Character.UnicodeBlock.of(start));
        }
    }

    // TODO: lazy-load
    static {
        String s = "\u0000\u007f\u0080\u00ff\u0100\u017f\u0180\u024f\u0250\u02af\u02b0\u02ff\u0300\u036f\u0370\u03ff\u0400\u04ff\u0500\u052f\u0530\u058f\u0590\u05ff\u0600\u06ff\u0700\u074f\u0780\u07bf\u0900\u097f\u0980\u09ff\u0a00\u0a7f\u0a80\u0aff\u0b00\u0b7f\u0b80\u0bff\u0c00\u0c7f\u0c80\u0cff\u0d00\u0d7f\u0d80\u0dff\u0e00\u0e7f\u0e80\u0eff\u0f00\u0fff\u1000\u109f\u10a0\u10ff\u1100\u11ff\u1200\u137f\u13a0\u13ff\u1400\u167f\u1680\u169f\u16a0\u16ff\u1700\u171f\u1720\u173f\u1740\u175f\u1760\u177f\u1780\u17ff\u1800\u18af\u1900\u194f\u1950\u197f\u19e0\u19ff\u1d00\u1d7f\u1e00\u1eff\u1f00\u1fff\u2000\u206f\u2070\u209f\u20a0\u20cf\u20d0\u20ff\u2100\u214f\u2150\u218f\u2190\u21ff\u2200\u22ff\u2300\u23ff\u2400\u243f\u2440\u245f\u2460\u24ff\u2500\u257f\u2580\u259f\u25a0\u25ff\u2600\u26ff\u2700\u27bf\u27c0\u27ef\u27f0\u27ff\u2800\u28ff\u2900\u297f\u2980\u29ff\u2a00\u2aff\u2b00\u2bff\u2e80\u2eff\u2f00\u2fdf\u2ff0\u2fff\u3000\u303f\u3040\u309f\u30a0\u30ff\u3100\u312f\u3130\u318f\u3190\u319f\u31a0\u31bf\u31f0\u31ff\u3200\u32ff\u3300\u33ff\u3400\u4dbf\u4dc0\u4dff\u4e00\u9fff\ua000\ua48f\ua490\ua4cf\uac00\ud7af\ud800\udb7f\udb80\udbff\udc00\udfff\ue000\uf8ff\uf900\ufaff\ufb00\ufb4f\ufb50\ufdff\ufe00\ufe0f\ufe20\ufe2f\ufe30\ufe4f\ufe50\ufe6f\ufe70\ufeff\uff00\uffef\ufff0\uffff";
        for (int i = 0, len = s.length(); i < len; i += 2) {
            build(s.charAt(i), s.charAt(i + 1));
        }

        build(0x10000, 0x1007f);
        build(0x10080, 0x100ff);
        build(0x10100, 0x1013f);
        build(0x10300, 0x1032f);
        build(0x10330, 0x1034f);
        build(0x10380, 0x1039f);
        build(0x10400, 0x1044f);
        build(0x10450, 0x1047f);
        build(0x10480, 0x104af);
        build(0x10800, 0x1083f);
        build(0x1d000, 0x1d0ff);
        build(0x1d100, 0x1d1ff);
        build(0x1d300, 0x1d35f);
        build(0x1d400, 0x1d7ff);
        build(0x20000, 0x2a6df);
        build(0x2f800, 0x2fa1f);
        build(0xe0000, 0xe007f);
        build(0xe0100, 0xe01ef);
        build(0xf0000, 0xfffff);
        build(0x100000, 0x10fffe);
    }
}
