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

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is based upon the
 * ISBN converter and formatter class Copyright 2000-2005  by Openly Informatics, Inc.
 * http://www.openly.com/ This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version. This
 * library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details. You should have received a copy of the GNU
 * Lesser General Public License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA
 *
 * @author Eric Hellman Formatter based upon the Python script found at
 *         http://c0re.23.nu/c0de/misc/isbn.py Module for formating and checking ISBNs. Based con
 *         code from Nelson H. F. Beebe included in bibclean which seems to have some relation to ISBNs.
 *         See http://blogs.23.nu/c0re/stories/1416/ for further details. ISBN checking
 *         code is based on code snippet from Nikita Borisov at
 *         http://www.csclub.uwaterloo.ca/u/nborisov/projects/isbn.html
 * @author md@hudora.de
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 *
 * @see <a href="http://www.s.org/standards/home/s/international/html/usm12.htm">The ISBN Users' Manual</a>
 * @see <a href="http://www.ietf.org/html.charters/OLD/urn-charter.html">The IETF URN Charter</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces">The IANA URN assignments</a>
 * @see <a href="http://www.isbn-international.org/download/List%20of%20Ranges.pdf">ISBN prefix list</a>
 */
public abstract class AbstractStandardBookNumber implements StandardNumber {

    private static final String NUMVALUES = "0123456789X- ";
    private String original;
    private String lexicalForm;
    private String value;
    private String eanvalue;
    private boolean createWithChecksum;
    private boolean eanPreferred;
    private static List<String> ranges;

    static {
        try {
            ISBNRangeMessageConfigurator configurator = new ISBNRangeMessageConfigurator();
            ranges = configurator.getRanges();
        } catch (IOException e) {
        }
    }

    /**
     * Create a new ISBN object.
     *
     * @param value the ISBN candidate string
     * @param asEAN prefer European Article Number (EAN, ISBN-13)
     * @param createWithChecksum if the checksum must be added
     */
    public AbstractStandardBookNumber(String value, boolean asEAN, boolean createWithChecksum) throws InvalidStandardNumberException {
        this.original = value;
        this.eanPreferred = asEAN;
        this.createWithChecksum = createWithChecksum;
        this.lexicalForm = purify(value);
        parse(lexicalForm);
    }

    @Override
    public String getOriginal() {
        return original;
    }

    public String getValue() {
        return value;
    }

    /**
     * Comparable interface
     *
     * @param obj the object to be compared with
     *
     * @return -1 if value = null, 0 if values are equal, 1 if value is greater
     * than the value in obj
     */
    @Override
    public int compareTo(Object obj) {
        return value != null ?
             value.compareTo(((AbstractStandardBookNumber) obj).getValue()): -1;
    }

    /**
     * Get the acronym of this standard number.
     *
     * @return the acronym
     */
    @Override
    public abstract String getAcronym();

    /**
     * Returns true if a valid standard book number has beenn recognized
     *
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValid() {
        return eanPreferred ? eanvalue != null : value != null;
    }

    /**
     * Get the normalized value of this standard book number
     * 
     * @return the value of this standard book number
     */
    @Override
    public String getStandardNumberValue() {
        return eanPreferred ? eanvalue : value;
    }

    /**
     * Get printable representation of this standard book number
     *
     * @return ISBN-13, with (fixed) check digit
     */
    @Override
    public String getStandardNumberPrintableRepresentation() throws InvalidStandardNumberException {
        return eanPreferred ? fix(eanvalue) : fix("978-" + value);
    }

    /**
     * Get country and publisher code
     * '978' = old bookland ISBNs with old publisher codes
     * '979' = new bookland ISBNs with new publisher codes assigned, different from old codes(!)
     * @return the country/publisher code from ISBN
     * @throws InvalidStandardNumberException
     */
    public String getCountryAndPublisherCode() throws InvalidStandardNumberException {
        // we don't care about the wrong checksum when we fix the value
        String code = eanvalue != null
                ? fix(eanvalue) : fix("978" + value);
        String s = code.substring(4);
        int pos1 = s.indexOf('-');
        if (pos1 <= 0) {
            return null;
        }
        String pubCode = s.substring(pos1 + 1);
        int pos2 = pubCode.indexOf('-');
        if (pos2 <= 0) {
            return null;
        }
        String r = code.substring(0, pos1 + pos2 + 5);
        return r;
    }

    /**
     * Returns a ISBN check digit for the first 9 digits in a string
     *
     * @param s the ISBN
     * @return check digit
     *
     * @throws InvalidStandardNumberException
     */
    private String createCheckDigit10(String isbn) throws InvalidStandardNumberException {
        char[] theChars = isbn.toCharArray();
        int checksum = 0;
        int weight = 10;
        int i;
        int val;
        for (i = 0; (i < theChars.length) && (weight > 1); i++) {
            val = NUMVALUES.indexOf(theChars[i]);
            if ((val >= 0) && (val < 10)) {
                checksum = checksum + (weight * val);
                weight--;
            } else {
                throw new InvalidStandardNumberException();
            }
        }
        if ((checksum % 11) == 0) {
            return "0";
        }
        return NUMVALUES.substring(11 - (checksum % 11), 12 - (checksum % 11));
    }

    /**
     * Returns an ISBN check digit for the first 12 digits in a string
     *
     * @param isbn
     * @return check digit
     *
     * @throws InvalidStandardNumberException
     */
    private String createCheckDigit13(String isbn) throws InvalidStandardNumberException {
        char[] theChars = isbn.toCharArray();
        int checksum13 = 0;
        int weight13 = 1;
        int i;
        int val;
        for (i = 0; i < 12; i++) {
            val = NUMVALUES.indexOf(theChars[i]);
            if ((val >= 0) && (val < 12)) {
                checksum13 = checksum13 + (weight13 * val);
                weight13 = (weight13 + 2) % 4;
            } else {
                throw new InvalidStandardNumberException();
            }
        }
        if ((checksum13 % 10) == 0) {
            return "0";
        }
        return NUMVALUES.substring(10 - (checksum13 % 10), 11 - (checksum13 % 10));
    }
    private final static Pattern ISBN_PATTERN = Pattern.compile("[\\p{Digit}xX\\-]+");

    /**
     * Helper method for cleaning values such as "ISBN 3-9803350-5-4 kart. : DM 24.00"
     */
    private String purify(String s) {
        Matcher m = ISBN_PATTERN.matcher(s);
        return m.find() ? s.substring(m.start(), m.end()).replaceAll("-", "") : ""; // no valid ISBN characters at all
    }

    private String fix(String isbn) {
        if (isbn == null) {
            return null;
        }
        for (int i = 0; i < ranges.size(); i += 2) {
            if (isInRange(isbn, ranges.get(i), ranges.get(i + 1)) == 0) {
                return hyphenate(ranges.get(i), isbn);
            }
        }
        return isbn;
    }

    private String hyphenate(String prefix, String isbn) {
        StringBuilder sb = new StringBuilder(prefix.substring(0, 4)); // '978-', '979-'
        prefix = prefix.substring(4);
        isbn = isbn.substring(3); // 978, 979
        int i = 0;
        int j = 0;
        while (i < prefix.length()) {
            char ch = prefix.charAt(i++);
            if (ch == '-') {
                sb.append('-'); // set first hyphen
            } else {
                sb.append(isbn.charAt(j++));
            }
        }
        sb.append('-'); // set second hyphen
        while (j < (isbn.length() - 1)) {
            sb.append(isbn.charAt(j++));
        }
        sb.append('-'); // set third hyphen
        sb.append(isbn.charAt(isbn.length() - 1));
        return sb.toString();
    }

    /**
     * Check if ISBN is within a given value range
     * @param the ISBN
     * @param begin
     * @param end
     * @return
     */
    private int isInRange(String isbn, String begin, String end) {
        String b = begin.replaceAll("-", "");
        int blen = b.length();
        int c = isbn.substring(0, blen).compareTo(b);
        if (c < 0) {
            return -1;
        }
        String e = end.replaceAll("-", "");
        int elen = e.length();
        c = e.compareTo(isbn.substring(0, elen));
        if (c < 0) {
            return 1;
        }
        return 0;
    }

    private void parse(String value) throws InvalidStandardNumberException {
        String s = value;
        char[] theChars = s.toUpperCase().toCharArray();
        int i;
        int val = 0;
        if (value.length() < 9) {
            throw new InvalidStandardNumberException("ISBN too short");
        }
        if (theChars.length == 10) {
            // ISBN-10
            int checksum = 0;
            int weight = 10;
            for (i = 0; weight > 0; i++) {
                val = NUMVALUES.indexOf(theChars[i]);
                if (val >= 0) {
                    if ((val == 10) && (weight != 1)) {
                        throw new InvalidStandardNumberException("bad X symbol"); // bad place for X
                    }
                    checksum += (weight * val);
                    weight--;
                } else {
                    throw new InvalidStandardNumberException("invalid char"); // invalid char
                }
            }
            if ((checksum % 11) != 0) {
                if (createWithChecksum) {
                    this.value = s.substring(0, 9) + createCheckDigit10(s.substring(0, 9));
                } else {
                    throw new InvalidStandardNumberException("bad checksum"); // bad checksum
                }
            } else {
                this.value = s;
            }
            if (eanPreferred) {
                this.eanvalue = "978" + value.substring(0, 9) + createCheckDigit13("978" + value.substring(0, 9));
            }

        } else if (theChars.length == 13) {
            if (!s.startsWith("978") && !s.startsWith("979")) {
                throw new InvalidStandardNumberException("bad prefix, must be 978 or 979: " + s);
            }
            int checksum13 = 0;
            int weight13 = 1;
            for (i = 0; i < 13; i++) {
                val = NUMVALUES.indexOf(theChars[i]);
                if (val >= 0) {
                    if (val == 10) {
                        throw new InvalidStandardNumberException("bad symbol: " + s);
                    }
                    checksum13 += (weight13 * val);
                    weight13 = (weight13 + 2) % 4;
                } else {
                    throw new InvalidStandardNumberException("invalid char"); // invalid char
                }
            }
            // set value
            if ((checksum13 % 10) != 0) {
                if (eanPreferred && createWithChecksum) {
                    // with checksum
                    this.eanvalue = s.substring(0, 12) + createCheckDigit13(s.substring(0, 12));
                } else {
                    throw new InvalidStandardNumberException("bad checksum"); // bad checksum
                }
            } else {
                // correct checksum
                this.eanvalue = s;
            }
            if (!eanPreferred && (eanvalue.startsWith("978") || eanvalue.startsWith("979"))) {
                // create 10-digit from 13-digit
                this.value = eanvalue.substring(3, 12) + createCheckDigit10(eanvalue.substring(3, 12));
            } else {
                // 10 digit version not available - not an error
                this.value = null;
            }
        } else if (theChars.length == 9) {
            // repair ISBN-10 ?
            if (createWithChecksum && value != null) {
                // create 978 from 10-digit without checksum
                this.eanvalue = "978" + value.substring(0, 9) + createCheckDigit13("978" + value.substring(0, 9));
                this.value = s.substring(0, 9) + createCheckDigit10(s.substring(0, 9));
            } else {
                throw new InvalidStandardNumberException("invalid"); // 9-digit
            }
        } else if (theChars.length == 12) {
            // repair ISBN-13 ?
            if (!s.startsWith("978") && !s.startsWith("979")) {
                throw new InvalidStandardNumberException("bad prefix");
            }
            if (createWithChecksum && value != null) {
                // create 978 from 10-digit
                this.eanvalue = "978" + value.substring(0, 9) + createCheckDigit13("978" + value.substring(0, 9));
                this.value = s.substring(3, 12) + createCheckDigit10(s.substring(3, 12));
            } else {
                throw new InvalidStandardNumberException("invalid"); // 12-digit
            }
        } else {
            throw new InvalidStandardNumberException("invalid"); // e.g. 11-digit
        }
    }
}
