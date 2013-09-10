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
package org.xbib.strings.encode;

import java.util.LinkedHashMap;

/**
 * A word-boundary aware entropy encoder.
 *
 * Each character at word beginning or end of string is kept as upper case, the rest
 * will collabse into lower case. This helps preserving words.
 *
 * The reason for preserving that kind of information is that errors are very rare at boundaries
 * and the too eager dropping of single frequence character  are compensated.
 *
 * There are also lone characters forming a word in a journal title, e.g.
 * "Physical Review A", Physical Review B" which are preserved this way.
 *
 * <p/>
 * Inspired by:
 * <p/>
 * Character coding for bibliographical record control.
 * E. J. Yannakoudakis, F. H. Ayres and J. A. W. Huggill.
 * Computer Centre, University of Bradford, 1980
 * <p/>
 * Yannakoudakis, E. J. Derived search keys for bibliographic
 * retrieval. SIGIR Forum 17, 4 (Jun. 1983), 220-237.
 *
 */
public class WordBoundaryEntropyEncoder implements StringEncoder {

    /**
     * Encode a string by a simple entropy-based method.
     * <p/>
     * Strategy: count characters in lower-case string,
     * select only characters with a frequency of 1,
     * drop space characters.
     *
     * @param s
     * @return encoded string
     * @throws org.xbib.strings.encode.EncoderException
     */
    public String encode(String s) throws EncoderException {
        LinkedHashMap<Character, Integer> freq = new LinkedHashMap();
        char[] chars = s.toLowerCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            // first character, character after space = first character of a word, and final character
            if (i == 0 || (i > 0 && chars[i-1] == ' ') || i+1 == chars.length) {
                ch = Character.toUpperCase(ch);
            }
            freq.put(ch, freq.containsKey(ch) ? freq.get(ch) + 1 : 0);
        }
        StringBuilder sb = new StringBuilder();
        for (char ch : freq.keySet()) {
            if (Character.isWhitespace(ch)) {
                continue;
            }
            if (freq.get(ch) < 2) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
