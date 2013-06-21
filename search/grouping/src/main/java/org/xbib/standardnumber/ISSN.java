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
 * Quoted from http://www.issn.org/2-22636-All-about-ISSN.php
 *
 * The ISSN (International Standard Serial Number) is an eight-digit number
 * which identifies periodical publications as such, including electronic
 * serials.
 *
 * The ISSN is a numeric code which is used as an identifier: it has no
 * signification in itself and does not contain in itself any information
 * referring to the origin or contents of the publication.
 *
 * The ISSN takes the form of the acronym ISSN followed by two groups
 * of four digits, separated by a hyphen. The eighth character is a
 * control digit calculated according to a modulo 11 algorithm on
 * the basis of the 7 preceding digits; this eighth control character
 * may be an "X" if the result of the computing is equal to "10",
 * in order to avoid any ambiguity.
 *
 *  The ISSN is linked to a standardized form of the title of the
 *  identified serial, known as the "key title", which repeats
 *  the title of the publication, qualifying it with additional elements
 *  in order to distinguish it from other publications having identical
 *  titles.
 *
 *  If the title of the publication changes in any significant way,
 *  a new ISSN must be assigned in order to correspond to this new form
 *  of title and avoid any confusion. A serial publication whose
 *  title is modified several times in the course of its existence
 *  will be assigned each time a new ISSN, thus allowing precise
 *  identification of each form of the title : in fact it is then
 *  considered that they are different publications even if there
 *  is a logical link between them.
 *
 *  Contrary to other types of publications, the world of serial
 *  publications is particularly changeable and complex :
 *  the lifetime of a title may be extremely short; many publications
 *  may be part of a complex set of relationships, etc.
 *  These particularities themselves necessitated the introduction
 *  of the ISSN.
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
        int weight;
        for (int i = 0; i < 7; i++) {
            weight = 8 -i;
            sum += weight * (s.charAt(i) - '0');
        }
        int mod = sum % 11;
        mod = mod == 0 ? 0 : 11 - mod; 
        char p = mod == 10 ? 'X' : (char) ('0' + mod);
        this.valid = p == Character.toUpperCase(s.charAt(7));
        if (!valid) {
            throw new InvalidStandardNumberException(lexicalForm);            
        }
        this.value = s ;
    }
}
