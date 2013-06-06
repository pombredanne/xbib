/*
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
package org.xbib.xml;

public final class XMLUtil {

    private XMLUtil() {
    }

    /**
     * Replace special characters with XML escapes:
     * <pre>
     * &amp; <small>(ampersand)</small> is replaced by &amp;amp;
     *
     * &lt; <small>(less than)</small> is replaced by &amp;lt;
     * &gt; <small>(greater than)</small> is replaced by &amp;gt;
     * &quot; <small>(double quote)</small> is replaced by &amp;quot;
     * </pre>
     *
     * @param string The string to be escaped.
     * @return The escaped string.
     */
    public static CharSequence escape(CharSequence string) {
        StringBuilder sb = new StringBuilder();
        escape(sb, string);
        return sb;
    }

    public static CharSequence escape(char[] chars, int start, int len) {
        StringBuilder sb = new StringBuilder();
        escape(sb, chars, start, len);
        return sb;
    }

    public static void escape(StringBuilder sb, String string) {
        escape(sb, string.toCharArray(), 0, string.length());
    }

    public static void escape(StringBuilder sb, CharSequence string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
    }

    public static void escape(StringBuilder sb, char[] chars, int start, int len) {
        for (int i = start; i < len; i++) {
            char c = chars[i];
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
    }

    public static String unescape(String str) {
        int idxS = str.indexOf("&");
        if (idxS < 0) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        int idxE, idx, size;
        char c;
        int prev = 0;
        while (idxS != -1) {
            if (prev < idxS) {
                sb.append(str.substring(prev, idxS));
                prev = idxS;
            }
            idxE = str.indexOf(";", idxS);
            if (idxE < 0) {
                break;
            }
            idx = idxS + 1;
            size = idxE - idxS - 1;
            if (size < 2) {
                sb.append(str.substring(idxS, idxE + 1));
            } else {
                c = str.charAt(idx);
                switch (c) {
                    case 'l':
                        if (!xmlDecodeLT(str, idx, sb, size)) {
                            sb.append("&");
                            idxE = idxS;
                        }
                        break;

                    case 'g':
                        if (!xmlDecodeGT(str, idx, sb, size)) {
                            sb.append("&");
                            idxE = idxS;
                        }
                        break;

                    case 'q':
                        if (!xmlDecodeQUOT(str, idx, sb, size)) {
                            sb.append("&");
                            idxE = idxS;
                        }
                        break;

                    case 'a':
                        if (!xmlDecodeAMPAPOS(str, idx, sb, size)) {
                            sb.append("&");
                            idxE = idxS;
                        }
                        break;

                    case 'n':
                        if (!xmlDecodeNBSP(str, idx, sb, size)) {
                            sb.append("&");
                            idxE = idxS;
                        }
                        break;

                    case '#':
                        if (!xmlDecodeNumber(str, idx, sb, idxE)) {
                            sb.append("&");
                            idxE = idxS;
                        }
                        break;

                    default:
                        sb.append("&");
                        idxE = idxS;
                }
            }
            prev = idxE + 1;
            idxS = str.indexOf("&", prev);
        }
        if (prev < str.length()) {
            sb.append(str.substring(prev));
        }
        return sb.toString();
    }


    private static boolean xmlDecodeLT(String str, int idx, StringBuilder sb, int size) {
        boolean isRecognized = true;
        char c_1 = str.charAt(idx + 1);
        if ((size != 2) || (c_1 != 't')) {
            isRecognized = false;
        } else {
            // lt
            sb.append('<');
        }
        return isRecognized;
    }

    private static boolean xmlDecodeGT(String str, int idx, StringBuilder sb, int size) {
        boolean isRecognized = true;
        char c_1 = str.charAt(idx + 1);
        if ((size != 2) || (c_1 != 't')) {
            isRecognized = false;
        } else {
            sb.append('>');
        }
        return isRecognized;
    }

    private static boolean xmlDecodeQUOT(String str, int idx, StringBuilder sb, int size) {
        boolean isRecognized = true;
        char c_1 = str.charAt(idx + 1);
        if ((size != 4) || (c_1 != 'u' || str.charAt(idx + 2) != 'o' || str.charAt(idx + 3) != 't')) {
            isRecognized = false;
        } else {
            sb.append('"');
        }
        return isRecognized;
    }

    private static boolean xmlDecodeAMPAPOS(String str, int idx, StringBuilder sb, int size) {
        boolean isRecognized = true;
        char c_1 = str.charAt(idx + 1);
        if ((size == 3) && (c_1 == 'm') && (str.charAt(idx + 2) == 'p')) {
            sb.append('&');
        } else if ((size == 4) && (c_1 == 'p') && (str.charAt(idx + 2) == 'o') && (str.charAt(idx + 3) == 's')) {
            sb.append('\'');
        } else {
            isRecognized = false;
        }
        return isRecognized;
    }

    private static boolean xmlDecodeNBSP(String str, int idx, StringBuilder sb, int size) {
        boolean isRecognized = true;
        char c_1 = str.charAt(idx + 1);
        if ((size != 4) || (c_1 != 'b' || str.charAt(idx + 2) != 's' || str.charAt(idx + 3) != 'p')) {
            isRecognized = false;
        } else {
            sb.append(' ');
        }
        return isRecognized;
    }

    private static boolean xmlDecodeNumber(String str, int idx, StringBuilder sb, int idxE) {
        boolean isRecognized = true;
        try {
            char c = 0;
            char c_1 = str.charAt(idx + 1);
            if (c_1 == 'x') {
                c = (char) Integer.parseInt(str.substring(idx + 2, idxE), 16);
            } else {
                c = (char) Integer.parseInt(str.substring(idx + 1, idxE));
            }
            sb.append(c);
        } catch (NumberFormatException ex) {
            isRecognized = false;
        }
        return isRecognized;
    }

    /**
     * Clean strings from illegal XML characters.
     * <p/>
     * See <http://www.w3.org/TR/2006/REC-xml-20060816/#charsets>
     *
     * @param string string to clean
     * @return the cleaned string
     */
    public static String clean(CharSequence string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = string.length(); i < len; i++) {
            char c = string.charAt(i);
            boolean legal = c == '\u0009' || c == '\n' || c == '\r'
                    || (c >= '\u0020' && c <= '\uD7FF')
                    || (c >= '\uE000' && c <= '\uFFFD');
            if (legal) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /*
     * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
     *
     * Licensed under the Aduna BSD-style license.
     */

    /**
     * Checks whether the supplied String is an NCName (Namespace Classified
     * Name) as specified at <a
     * href="http://www.w3.org/TR/REC-xml-names/#NT-NCName">
     * http://www.w3.org/TR/REC-xml-names/#NT-NCName</a>.
     */
    public static boolean isNCName(CharSequence name) {
        int nameLength = name.length();
        if (nameLength == 0) {
            return false;
        }
        // Check first character
        char c = name.charAt(0);
        if (c == '_' || isLetter(c)) {
            // Check the rest of the characters
            for (int i = 1; i < nameLength; i++) {
                c = name.charAt(i);
                if (!isNCNameChar(c)) {
                    return false;
                }
            }
            // All characters have been checked
            return true;
        }
        return false;
    }

    public static boolean isNCNameChar(char c) {
        return isAsciiBaseChar(c)
                || _isAsciiDigit(c)
                || c == '.' || c == '-' || c == '_'
                || isNonAsciiBaseChar(c)
                || _isNonAsciiDigit(c)
                || isIdeographic(c)
                || isCombiningChar(c)
                || isExtender(c);
    }

    public static boolean isLetter(char c) {
        return isAsciiBaseChar(c)
                || isNonAsciiBaseChar(c)
                || isIdeographic(c);
    }

    private static boolean isAsciiBaseChar(char c) {
        return charInRange(c, 0x0041, 0x005A)
                || charInRange(c, 0x0061, 0x007A);
    }

    private static boolean isNonAsciiBaseChar(char c) {
        return charInRange(c, 0x00C0, 0x00D6)
                || charInRange(c, 0x00D8, 0x00F6)
                || charInRange(c, 0x00F8, 0x00FF)
                || charInRange(c, 0x0100, 0x0131)
                || charInRange(c, 0x0134, 0x013E)
                || charInRange(c, 0x0141, 0x0148)
                || charInRange(c, 0x014A, 0x017E)
                || charInRange(c, 0x0180, 0x01C3)
                || charInRange(c, 0x01CD, 0x01F0)
                || charInRange(c, 0x01F4, 0x01F5)
                || charInRange(c, 0x01FA, 0x0217)
                || charInRange(c, 0x0250, 0x02A8)
                || charInRange(c, 0x02BB, 0x02C1)
                || c == 0x0386
                || charInRange(c, 0x0388, 0x038A)
                || c == 0x038C
                || charInRange(c, 0x038E, 0x03A1)
                || charInRange(c, 0x03A3, 0x03CE)
                || charInRange(c, 0x03D0, 0x03D6)
                || c == 0x03DA
                || c == 0x03DC
                || c == 0x03DE
                || c == 0x03E0
                || charInRange(c, 0x03E2, 0x03F3)
                || charInRange(c, 0x0401, 0x040C)
                || charInRange(c, 0x040E, 0x044F)
                || charInRange(c, 0x0451, 0x045C)
                || charInRange(c, 0x045E, 0x0481)
                || charInRange(c, 0x0490, 0x04C4)
                || charInRange(c, 0x04C7, 0x04C8)
                || charInRange(c, 0x04CB, 0x04CC)
                || charInRange(c, 0x04D0, 0x04EB)
                || charInRange(c, 0x04EE, 0x04F5)
                || charInRange(c, 0x04F8, 0x04F9)
                || charInRange(c, 0x0531, 0x0556)
                || c == 0x0559
                || charInRange(c, 0x0561, 0x0586)
                || charInRange(c, 0x05D0, 0x05EA)
                || charInRange(c, 0x05F0, 0x05F2)
                || charInRange(c, 0x0621, 0x063A)
                || charInRange(c, 0x0641, 0x064A)
                || charInRange(c, 0x0671, 0x06B7)
                || charInRange(c, 0x06BA, 0x06BE)
                || charInRange(c, 0x06C0, 0x06CE)
                || charInRange(c, 0x06D0, 0x06D3)
                || c == 0x06D5
                || charInRange(c, 0x06E5, 0x06E6)
                || charInRange(c, 0x0905, 0x0939)
                || c == 0x093D
                || charInRange(c, 0x0958, 0x0961)
                || charInRange(c, 0x0985, 0x098C)
                || charInRange(c, 0x098F, 0x0990)
                || charInRange(c, 0x0993, 0x09A8)
                || charInRange(c, 0x09AA, 0x09B0)
                || c == 0x09B2
                || charInRange(c, 0x09B6, 0x09B9)
                || charInRange(c, 0x09DC, 0x09DD)
                || charInRange(c, 0x09DF, 0x09E1)
                || charInRange(c, 0x09F0, 0x09F1)
                || charInRange(c, 0x0A05, 0x0A0A)
                || charInRange(c, 0x0A0F, 0x0A10)
                || charInRange(c, 0x0A13, 0x0A28)
                || charInRange(c, 0x0A2A, 0x0A30)
                || charInRange(c, 0x0A32, 0x0A33)
                || charInRange(c, 0x0A35, 0x0A36)
                || charInRange(c, 0x0A38, 0x0A39)
                || charInRange(c, 0x0A59, 0x0A5C)
                || c == 0x0A5E
                || charInRange(c, 0x0A72, 0x0A74)
                || charInRange(c, 0x0A85, 0x0A8B)
                || c == 0x0A8D
                || charInRange(c, 0x0A8F, 0x0A91)
                || charInRange(c, 0x0A93, 0x0AA8)
                || charInRange(c, 0x0AAA, 0x0AB0)
                || charInRange(c, 0x0AB2, 0x0AB3)
                || charInRange(c, 0x0AB5, 0x0AB9)
                || c == 0x0ABD
                || c == 0x0AE0
                || charInRange(c, 0x0B05, 0x0B0C)
                || charInRange(c, 0x0B0F, 0x0B10)
                || charInRange(c, 0x0B13, 0x0B28)
                || charInRange(c, 0x0B2A, 0x0B30)
                || charInRange(c, 0x0B32, 0x0B33)
                || charInRange(c, 0x0B36, 0x0B39)
                || c == 0x0B3D
                || charInRange(c, 0x0B5C, 0x0B5D)
                || charInRange(c, 0x0B5F, 0x0B61)
                || charInRange(c, 0x0B85, 0x0B8A)
                || charInRange(c, 0x0B8E, 0x0B90)
                || charInRange(c, 0x0B92, 0x0B95)
                || charInRange(c, 0x0B99, 0x0B9A)
                || c == 0x0B9C
                || charInRange(c, 0x0B9E, 0x0B9F)
                || charInRange(c, 0x0BA3, 0x0BA4)
                || charInRange(c, 0x0BA8, 0x0BAA)
                || charInRange(c, 0x0BAE, 0x0BB5)
                || charInRange(c, 0x0BB7, 0x0BB9)
                || charInRange(c, 0x0C05, 0x0C0C)
                || charInRange(c, 0x0C0E, 0x0C10)
                || charInRange(c, 0x0C12, 0x0C28)
                || charInRange(c, 0x0C2A, 0x0C33)
                || charInRange(c, 0x0C35, 0x0C39)
                || charInRange(c, 0x0C60, 0x0C61)
                || charInRange(c, 0x0C85, 0x0C8C)
                || charInRange(c, 0x0C8E, 0x0C90)
                || charInRange(c, 0x0C92, 0x0CA8)
                || charInRange(c, 0x0CAA, 0x0CB3)
                || charInRange(c, 0x0CB5, 0x0CB9)
                || c == 0x0CDE
                || charInRange(c, 0x0CE0, 0x0CE1)
                || charInRange(c, 0x0D05, 0x0D0C)
                || charInRange(c, 0x0D0E, 0x0D10)
                || charInRange(c, 0x0D12, 0x0D28)
                || charInRange(c, 0x0D2A, 0x0D39)
                || charInRange(c, 0x0D60, 0x0D61)
                || charInRange(c, 0x0E01, 0x0E2E)
                || c == 0x0E30
                || charInRange(c, 0x0E32, 0x0E33)
                || charInRange(c, 0x0E40, 0x0E45)
                || charInRange(c, 0x0E81, 0x0E82)
                || c == 0x0E84
                || charInRange(c, 0x0E87, 0x0E88)
                || c == 0x0E8A
                || c == 0x0E8D
                || charInRange(c, 0x0E94, 0x0E97)
                || charInRange(c, 0x0E99, 0x0E9F)
                || charInRange(c, 0x0EA1, 0x0EA3)
                || c == 0x0EA5
                || c == 0x0EA7
                || charInRange(c, 0x0EAA, 0x0EAB)
                || charInRange(c, 0x0EAD, 0x0EAE)
                || c == 0x0EB0
                || charInRange(c, 0x0EB2, 0x0EB3)
                || c == 0x0EBD
                || charInRange(c, 0x0EC0, 0x0EC4)
                || charInRange(c, 0x0F40, 0x0F47)
                || charInRange(c, 0x0F49, 0x0F69)
                || charInRange(c, 0x10A0, 0x10C5)
                || charInRange(c, 0x10D0, 0x10F6)
                || c == 0x1100
                || charInRange(c, 0x1102, 0x1103)
                || charInRange(c, 0x1105, 0x1107)
                || c == 0x1109
                || charInRange(c, 0x110B, 0x110C)
                || charInRange(c, 0x110E, 0x1112)
                || c == 0x113C
                || c == 0x113E
                || c == 0x1140
                || c == 0x114C
                || c == 0x114E
                || c == 0x1150
                || charInRange(c, 0x1154, 0x1155)
                || c == 0x1159
                || charInRange(c, 0x115F, 0x1161)
                || c == 0x1163
                || c == 0x1165
                || c == 0x1167
                || c == 0x1169
                || charInRange(c, 0x116D, 0x116E)
                || charInRange(c, 0x1172, 0x1173)
                || c == 0x1175
                || c == 0x119E
                || c == 0x11A8
                || c == 0x11AB
                || charInRange(c, 0x11AE, 0x11AF)
                || charInRange(c, 0x11B7, 0x11B8)
                || c == 0x11BA
                || charInRange(c, 0x11BC, 0x11C2)
                || c == 0x11EB
                || c == 0x11F0
                || c == 0x11F9
                || charInRange(c, 0x1E00, 0x1E9B)
                || charInRange(c, 0x1EA0, 0x1EF9)
                || charInRange(c, 0x1F00, 0x1F15)
                || charInRange(c, 0x1F18, 0x1F1D)
                || charInRange(c, 0x1F20, 0x1F45)
                || charInRange(c, 0x1F48, 0x1F4D)
                || charInRange(c, 0x1F50, 0x1F57)
                || c == 0x1F59
                || c == 0x1F5B
                || c == 0x1F5D
                || charInRange(c, 0x1F5F, 0x1F7D)
                || charInRange(c, 0x1F80, 0x1FB4)
                || charInRange(c, 0x1FB6, 0x1FBC)
                || c == 0x1FBE
                || charInRange(c, 0x1FC2, 0x1FC4)
                || charInRange(c, 0x1FC6, 0x1FCC)
                || charInRange(c, 0x1FD0, 0x1FD3)
                || charInRange(c, 0x1FD6, 0x1FDB)
                || charInRange(c, 0x1FE0, 0x1FEC)
                || charInRange(c, 0x1FF2, 0x1FF4)
                || charInRange(c, 0x1FF6, 0x1FFC)
                || c == 0x2126
                || charInRange(c, 0x212A, 0x212B)
                || c == 0x212E
                || charInRange(c, 0x2180, 0x2182)
                || charInRange(c, 0x3041, 0x3094)
                || charInRange(c, 0x30A1, 0x30FA)
                || charInRange(c, 0x3105, 0x312C)
                || charInRange(c, 0xAC00, 0xD7A3);
    }

    public static boolean isIdeographic(char c) {
        return charInRange(c, 0x4E00, 0x9FA5)
                || c == 0x3007
                || charInRange(c, 0x3021, 0x3029);
    }

    public static boolean isCombiningChar(char c) {
        return charInRange(c, 0x0300, 0x0345)
                || charInRange(c, 0x0360, 0x0361)
                || charInRange(c, 0x0483, 0x0486)
                || charInRange(c, 0x0591, 0x05A1)
                || charInRange(c, 0x05A3, 0x05B9)
                || charInRange(c, 0x05BB, 0x05BD)
                || c == 0x05BF
                || charInRange(c, 0x05C1, 0x05C2)
                || c == 0x05C4
                || charInRange(c, 0x064B, 0x0652)
                || c == 0x0670
                || charInRange(c, 0x06D6, 0x06DC)
                || charInRange(c, 0x06DD, 0x06DF)
                || charInRange(c, 0x06E0, 0x06E4)
                || charInRange(c, 0x06E7, 0x06E8)
                || charInRange(c, 0x06EA, 0x06ED)
                || charInRange(c, 0x0901, 0x0903)
                || c == 0x093C
                || charInRange(c, 0x093E, 0x094C)
                || c == 0x094D
                || charInRange(c, 0x0951, 0x0954)
                || charInRange(c, 0x0962, 0x0963)
                || charInRange(c, 0x0981, 0x0983)
                || c == 0x09BC
                || c == 0x09BE
                || c == 0x09BF
                || charInRange(c, 0x09C0, 0x09C4)
                || charInRange(c, 0x09C7, 0x09C8)
                || charInRange(c, 0x09CB, 0x09CD)
                || c == 0x09D7
                || charInRange(c, 0x09E2, 0x09E3)
                || c == 0x0A02
                || c == 0x0A3C
                || c == 0x0A3E
                || c == 0x0A3F
                || charInRange(c, 0x0A40, 0x0A42)
                || charInRange(c, 0x0A47, 0x0A48)
                || charInRange(c, 0x0A4B, 0x0A4D)
                || charInRange(c, 0x0A70, 0x0A71)
                || charInRange(c, 0x0A81, 0x0A83)
                || c == 0x0ABC
                || charInRange(c, 0x0ABE, 0x0AC5)
                || charInRange(c, 0x0AC7, 0x0AC9)
                || charInRange(c, 0x0ACB, 0x0ACD)
                || charInRange(c, 0x0B01, 0x0B03)
                || c == 0x0B3C
                || charInRange(c, 0x0B3E, 0x0B43)
                || charInRange(c, 0x0B47, 0x0B48)
                || charInRange(c, 0x0B4B, 0x0B4D)
                || charInRange(c, 0x0B56, 0x0B57)
                || charInRange(c, 0x0B82, 0x0B83)
                || charInRange(c, 0x0BBE, 0x0BC2)
                || charInRange(c, 0x0BC6, 0x0BC8)
                || charInRange(c, 0x0BCA, 0x0BCD)
                || c == 0x0BD7
                || charInRange(c, 0x0C01, 0x0C03)
                || charInRange(c, 0x0C3E, 0x0C44)
                || charInRange(c, 0x0C46, 0x0C48)
                || charInRange(c, 0x0C4A, 0x0C4D)
                || charInRange(c, 0x0C55, 0x0C56)
                || charInRange(c, 0x0C82, 0x0C83)
                || charInRange(c, 0x0CBE, 0x0CC4)
                || charInRange(c, 0x0CC6, 0x0CC8)
                || charInRange(c, 0x0CCA, 0x0CCD)
                || charInRange(c, 0x0CD5, 0x0CD6)
                || charInRange(c, 0x0D02, 0x0D03)
                || charInRange(c, 0x0D3E, 0x0D43)
                || charInRange(c, 0x0D46, 0x0D48)
                || charInRange(c, 0x0D4A, 0x0D4D)
                || c == 0x0D57
                || c == 0x0E31
                || charInRange(c, 0x0E34, 0x0E3A)
                || charInRange(c, 0x0E47, 0x0E4E)
                || c == 0x0EB1
                || charInRange(c, 0x0EB4, 0x0EB9)
                || charInRange(c, 0x0EBB, 0x0EBC)
                || charInRange(c, 0x0EC8, 0x0ECD)
                || charInRange(c, 0x0F18, 0x0F19)
                || c == 0x0F35
                || c == 0x0F37
                || c == 0x0F39
                || c == 0x0F3E
                || c == 0x0F3F
                || charInRange(c, 0x0F71, 0x0F84)
                || charInRange(c, 0x0F86, 0x0F8B)
                || charInRange(c, 0x0F90, 0x0F95)
                || c == 0x0F97
                || charInRange(c, 0x0F99, 0x0FAD)
                || charInRange(c, 0x0FB1, 0x0FB7)
                || c == 0x0FB9
                || charInRange(c, 0x20D0, 0x20DC)
                || c == 0x20E1
                || charInRange(c, 0x302A, 0x302F)
                || c == 0x3099
                || c == 0x309A;
    }

    public static boolean isDigit(char c) {
        return _isAsciiDigit(c)
                || _isNonAsciiDigit(c);
    }

    private static boolean _isAsciiDigit(char c) {
        return charInRange(c, 0x0030, 0x0039);
    }

    private static boolean _isNonAsciiDigit(char c) {
        return charInRange(c, 0x0660, 0x0669)
                || charInRange(c, 0x06F0, 0x06F9)
                || charInRange(c, 0x0966, 0x096F)
                || charInRange(c, 0x09E6, 0x09EF)
                || charInRange(c, 0x0A66, 0x0A6F)
                || charInRange(c, 0x0AE6, 0x0AEF)
                || charInRange(c, 0x0B66, 0x0B6F)
                || charInRange(c, 0x0BE7, 0x0BEF)
                || charInRange(c, 0x0C66, 0x0C6F)
                || charInRange(c, 0x0CE6, 0x0CEF)
                || charInRange(c, 0x0D66, 0x0D6F)
                || charInRange(c, 0x0E50, 0x0E59)
                || charInRange(c, 0x0ED0, 0x0ED9)
                || charInRange(c, 0x0F20, 0x0F29);
    }

    public static boolean isExtender(char c) {
        return c == 0x00B7
                || c == 0x02D0
                || c == 0x02D1
                || c == 0x0387
                || c == 0x0640
                || c == 0x0E46
                || c == 0x0EC6
                || c == 0x3005
                || charInRange(c, 0x3031, 0x3035)
                || charInRange(c, 0x309D, 0x309E)
                || charInRange(c, 0x30FC, 0x30FE);
    }

    private static boolean charInRange(char c, int start, int end) {
        return c >= start && c <= end;
    }
}
