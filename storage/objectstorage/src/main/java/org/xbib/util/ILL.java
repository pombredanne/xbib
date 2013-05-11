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
package org.xbib.util;

import org.xbib.standardnumber.ISIL;
import org.xbib.standardnumber.InvalidStandardNumberException;
import org.xbib.standardnumber.StandardNumber;
import org.xbib.standardnumber.VerhoeffAlgorithm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ILL implements StandardNumber {

    private final static Pattern ILL_PATTERN = Pattern.compile("[\\p{Alnum}\\-]+");
    private String original;
    private String lexicalForm;
    private boolean valid;
    private int segmentCount;
    private String isilPrefix;
    private String isilBody;
    private int year;
    private int number;
    private int counter;
    private String check;

    /**
     * Creates a new ISSN object.
     *
     * @param value
     */
    public ILL(String value) {
        this.original = value;
        this.lexicalForm = purify(original);
        if (lexicalForm == null) {
            throw new IllegalArgumentException("syntax error for ILL number: " + value);
        }
        parse();
        if (!valid && segmentCount == 5) {
            // add check symbol
            this.check = VerhoeffAlgorithm.generateVerhoeff(Integer.toString(number));
            this.lexicalForm += "-" + check;
            this.valid = true;
        }
    }

    @Override
    public String getOriginal() {
        return original;
    }

    @Override
    public int compareTo(Object obj) {
        try {
            return lexicalForm != null && obj instanceof ILL ? lexicalForm.compareTo(((ILL) obj).getStandardNumberValue()) : -1;
        } catch (InvalidStandardNumberException ex) {
            return 0;
        }
    }

    /**
     * Get the acronym of this standard number.
     *
     * @return the acronym
     */
    @Override
    public String getAcronym() {
        return "ILL";
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
     *
     * @return value
     * @throws InvalidStandardNumberException
     */
    @Override
    public String getStandardNumberValue() throws InvalidStandardNumberException {
        if (lexicalForm == null) {
            parse();
        }
        return lexicalForm;
    }

    /**
     * Returns the printable representation of the standard number
     *
     * @return the printable representation
     * @throws InvalidStandardNumberException
     */
    @Override
    public String getStandardNumberPrintableRepresentation() throws InvalidStandardNumberException {
        if (isValid()) {
            return lexicalForm;
        } else {
            throw new InvalidStandardNumberException(original);
        }
    }

    public ISIL getISIL() throws InvalidStandardNumberException {
        return new ISIL(isilPrefix + "-" + isilBody);
    }

    public int getYear() {
        return year;
    }

    public int getNumber() {
        return number;
    }

    public int getCounter() {
        return counter;
    }

    /**
     * Helper method for cleaning values
     */
    private String purify(String s) {
        Matcher m = ILL_PATTERN.matcher(s);
        return m.find() ? s.substring(m.start(), m.end()) : null;
    }

    private void parse() {
        // segment
        String[] seg = lexicalForm.split("-");
        this.segmentCount = seg.length;
        if (seg.length > 0) {
            this.isilPrefix = seg[0];
        }
        if (seg.length > 1) {
            this.isilBody = seg[1];
        }
        if (seg.length > 2) {
            this.year = Integer.parseInt(seg[2]);
        }
        if (seg.length > 3) {
            this.number = Integer.parseInt(seg[3]);
        }
        if (seg.length > 4) {
            this.counter = Integer.parseInt(seg[4]);
        }
        if (seg.length > 5) {
            this.check = seg[5].substring(0, 1);
            this.valid = check.equals(VerhoeffAlgorithm.generateVerhoeff(Integer.toString(number)));
        }
        if (seg.length > 6) {
            this.valid = false;
        }
    }
}
