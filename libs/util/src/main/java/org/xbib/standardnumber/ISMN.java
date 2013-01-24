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
package org.xbib.standardnumber;

/**
 * International Standard Music Number
 *
 * The International Standard Music Number (ISMN) is a unique number
 * for the identification of all printed music publications from
 * all over the world, whether available for sale, hire or gratis --
 * whether a part, a score, or an element in a multi-media kit.
 *
 * The ISMN is designed to rationalize the processing and handling of
 * printed music and the respective bibliographical data for
 * publishing houses, the music trade and libraries.
 *
 * As of 1 January 2008 the ISMN consists of 13 digits starting with 979-0
 * Existing 10-digit ISMNs are prefixed by 979-
 * The leading M- of the 10-digit ISMNs will be replaced by 0- (zero)
 * The resulting 13-digit number will be identical with the
 * EAN-13 number that is currently encoded in the bar code.
 *
 * ISO Standard 10957 gives the basic rules of the ISMN system.
 *
 * The thirteen-digit number allows a billion items each to carry a different number.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ISMN implements StandardNumber {

    private String original;
    private String lexicalForm;
    private String value;
    private String eanvalue;
    private boolean isValid;
    private boolean createWithChecksum;
    private boolean eanPreferred;

    public ISMN(String value) throws InvalidStandardNumberException {
        this(value, true, false);
    }

    public ISMN(String value, boolean eanPreferred) throws InvalidStandardNumberException {
        this(value, eanPreferred, false);
    }

    public ISMN(String value, boolean eanPreferred, boolean createWithChecksum)
            throws InvalidStandardNumberException {
        this.original = value;
        this.eanPreferred = eanPreferred;
        this.createWithChecksum = createWithChecksum;
        parse(value);
    }

    public String getValue() {
        return value;
    }

    public String getAcronym() {
        return "ISMN";
    }

    public String getOriginal() {
        return original;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getStandardNumberValue() throws InvalidStandardNumberException {
        return isValid ? eanPreferred ? eanvalue : value : null;
    }

    public String getStandardNumberPrintableRepresentation() throws InvalidStandardNumberException {
        return eanPreferred ? "979-0-" + hyphenate(eanvalue.substring(4)) : "M-" + hyphenate(value.substring(1));
    }

    public int compareTo(Object o) {
        int i = -1;
        if (value != null) {
            i = value.compareTo(((ISMN) o).getValue());
        }
        return i;
    }

    /**
     * Clean for ISNM parser
     * @param s the unclean string
     *
     * @return a clean string
     */
    private String purify(String s) {
        String clean = s;
        if (clean.startsWith("ISMN")) {
            clean = clean.substring(Math.min(clean.length(), 5));
        }
        int pos = clean.indexOf(' ');
        clean = pos > 0 ? clean.substring(0, pos) : clean;
        clean = clean.replaceAll("[^\\p{Digit}M]", "");
        return clean;
    }

    /**
     * Parse input and recognize ISMN
     *
     * @param value the input string for ISMN recognition
     * @throws InvalidStandardNumberException
     */
    private void parse(String value) throws InvalidStandardNumberException {
        this.isValid = false;
        // purify
        this.lexicalForm = purify(value);
        String s = lexicalForm;
        // remove hyphens
        s = s.replaceAll("-", "");
        if (s.length() < 10) {
            throw new InvalidStandardNumberException("ISMN too short");
        }
        // old ISMN = M is first char and 10 digits?
        if (s.charAt(0) == 'M' && s.length() == 10) {
            if (createCheckDigit(s.substring(0, 9)).equals(s.substring(9))) {
                this.value = s;
                this.eanvalue = "9790" + s.substring(1);
                this.isValid = true;
                return;
            }
            if (createWithChecksum) {
                s = s.substring(0, 9);
            } else {
                throw new InvalidStandardNumberException("bad checksum");
            }
        }
        // new ISMN = prefix "9790" and 13 digits?
        if (s.startsWith("9790") && s.length() == 13) {
            // EAN
            if (createCheckDigit(s.substring(0, 12)).equals(s.substring(12))) {
                this.value = "M" + s.substring(4);
                this.eanvalue = s;
                this.isValid = true;
                return;
            }
            // bad checksum
            if (createWithChecksum) {
                s = s.substring(0, 12);
            } else {
                throw new InvalidStandardNumberException("bad checksum");
            }
        }
        // missing checksum?
        if (s.charAt(0) == 'M' && s.length() == 9) {
            if (createWithChecksum) {                
                this.value = s + createCheckDigit(s);
                this.eanvalue = "9790" + this.value.substring(1);
                this.isValid = true;
            } else {
                throw new InvalidStandardNumberException("missing checksum");
            }
        } // new ISMN = prefix "9790" and 13 digits?
        else if (s.startsWith("9790") && s.length() == 12) {
            if (createWithChecksum) {
                this.eanvalue = s + createCheckDigit(s);
                this.value = "M" + eanvalue.substring(4);
                this.isValid = true;
            } else {
                throw new InvalidStandardNumberException("missing checksum");
            }
        }
    }

    /**
     * For correct presentation, the letter M and the 9 digits of an ISMN
     * must be divided, by hyphens, into four parts:
     * <ol>
     * <li>Part 1: Distinguishing element: Constant "M"</li>
     * <li>Part 2: The publisher identifier</li>
     * <li>Part 3: The item identifier</li>
     * <li>Part 4: The check digit</li>
     * The position of the hyphens are determined by the publisher
     * prefix range. The knowledge of the prefix ranges is necessary
     * to develop the hyphenation output program.
     *
     * The publisher prefix ranges are as follows:
     *
     * Publisher Identifier    Total numbers available
     *                         for item identificaton
     * --------------------    -----------------------
     *     000----099                 100000
     *    1000----3999                 10000
     *   40000----69999                 1000
     *  700000----899999                 100
     * 9000000----9999999                 10
     *
     *
     *                             If Number
     * Publisher Identifier    Ranges are Between          Insert Hyphens After
     * --------------------    ------------------    --------------------------------
     *     000----099              00-09             1st digit(M) 4th digit   9th digit
     *    1000----3999             10-39               "          5th digit     "
     *   40000----69999            40-69               "          6th digit     "
     *  700000----899999           70-89               "          7th digit     "
     * 9000000----9999999          90-99               "          8th digit     "
     *
     */
    public String hyphenate(String value) {
        StringBuilder sb = new StringBuilder(value);
        int pos = 3;
        int range = Integer.parseInt(value.substring(0, 2));
        if (range < 10) {
            pos = 3;
        } else if (range < 40) {
            pos = 4;
        } else if (range < 70) {
            pos = 5;
        } else if (range < 90) {
            pos = 6;
        } else if (range < 100) {
            pos = 7;
        }
        sb.insert(pos, '-');
        sb.insert(9, '-');
        return sb.toString();
    }

    /**
     * Returns a checkdigit for the ISMN string
     *
     * @param value the ISMN string
     * @return  the check digit
     *
     * @throws InvalidStandardNumberException
     */
    private String createCheckDigit(String value) throws InvalidStandardNumberException {
        char[] theChars = value.toCharArray();
        int checksum = 0;
        int weight = 3;
        int val = 0;
        for (int i = 0; i < theChars.length; i++) {
            val = theChars[i] == 'M' ? 3 : theChars[i] - '0';
            if (val < 0 || val > 9) {
                throw new InvalidStandardNumberException();
            }
            weight = (theChars.length - i) % 2 == 0 ? 1 : 3;
            checksum = checksum + (weight * val);
        }
        int c = checksum % 10;
        return String.valueOf(c > 0 ? 10 - c : 0);
    }
}
