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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The International Standard Serial Number (ISSN) is a unique
 * eight-digit number used to identify a print or electronic periodical
 * publication. The ISSN system was adopted as international standard
 * ISO 3297 in 1975. The ISO subcommittee TC 46/SC 9 is responsible
 * for the standard.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ISSN implements StandardNumber {

    private String original;
    private String lexicalForm;
    private String value;
    private String printable;
    private boolean valid;

    /**
     * Creates a new ISSN object.
     *
     * @param value 
     */
    public ISSN(String value) throws InvalidStandardNumberException {
        this.original = value;
        this.lexicalForm = purify(value);
        parse();
    }

    @Override
    public String getOriginal() {
        return original;
    }

    @Override
    public int compareTo(Object obj) {
        return value != null ? value.compareTo(((ISSN) obj).getStandardNumberValue()) : -1;
    }

    /**
     * Get the acronym of this standard number.
     *
     * @return the acronym
     */
    @Override
    public String getAcronym() {
        return "ISSN";
    }

    /**
     * Check if standard number is valid
     *
     * @return true if ISSN is valid
     */
    @Override
    public boolean isValid() {
        return valid;
    }

    /**
     * Returns the value representation of the standard number
     * @return value
     * @throws InvalidStandardNumberException
     */
    @Override
    public String getStandardNumberValue() {
        return value;
    }

    /**
     * Returns the printable representation of the standard number
     * @return the printable representation
     * @throws InvalidStandardNumberException
     */
    @Override
    public String getStandardNumberPrintableRepresentation() throws InvalidStandardNumberException {
        if (printable == null) {
            this.printable = isValid() ? value.substring(0, 4) + "-" + value.substring(4, 8) : "";
        }
        return printable;
    }

    private final static Pattern ISSN_PATTERN = Pattern.compile("[\\p{Digit}xX\\-]+");

    /**
     * Helper method for cleaning values such as "ISBN 3-9803350-5-4 kart. : DM 24.00"
     */
    private String purify(String s) {
        Matcher m = ISSN_PATTERN.matcher(s);
        return m.find() ? s.substring(m.start(), m.end()).replaceAll("-", "") : "";
    }

    private void parse() throws InvalidStandardNumberException {
        String s = lexicalForm;
        if (s.length() != 8) {
            throw new InvalidStandardNumberException();
        }
        int sum = 0;
        for (int i = 0; i < 7; i++) {
            sum += ((8 - i) * (s.charAt(i) - '0'));
        }
        int mod = 11 - (sum % 11);
        char p = (mod == 10) ? 'x' : (char) ('0' + mod);
        this.valid = p == Character.toLowerCase(s.charAt(7));
        if (!valid) {
            throw new InvalidStandardNumberException(lexicalForm);            
        }
        this.value = s ;
    }
}
