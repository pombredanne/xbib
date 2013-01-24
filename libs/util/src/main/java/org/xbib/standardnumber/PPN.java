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
 * Pica Production Number
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class PPN implements StandardNumber {

    private String original;
    private String value;
    private boolean isValid;

    public PPN(String value)
            throws InvalidStandardNumberException {
        this.original = value;
        parse(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getAcronym() {
        return "PPN";
    }

    @Override
    public String getOriginal() {
        return original;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public String getStandardNumberValue() throws InvalidStandardNumberException {
        return isValid ? value : null;
    }

    @Override
    public String getStandardNumberPrintableRepresentation() throws InvalidStandardNumberException {
        return getStandardNumberValue();
    }

    @Override
    public int compareTo(Object o) {
        int i = -1;
        if (value != null) {
            i = value.compareTo(((PPN) o).getValue());
        }
        return i;
    }

    /**
     * Parse input and recognize PPN
     *
     * @param value the input string
     * @throws InvalidStandardNumberException
     */
    private void parse(String value) throws InvalidStandardNumberException {
        this.isValid = false;
        String s = value;
        if (check(s)) {
            this.value = s;
            this.isValid = true;
        } else {
            throw new InvalidStandardNumberException("bad PPN checksum");
        }
    }

    /**
     * Check if PPN is valid
     *
     * @param value the PPN
     * @return the check digit
     *
     * @throws InvalidStandardNumberException
     */
    private boolean check(String value) throws InvalidStandardNumberException {
        char[] theChars = value.toCharArray();
        int checksum = 0;
        int weight = 1;
        int val;
        if (theChars[theChars.length - 1] == 'X') {
            theChars[theChars.length - 1] = '0' + 10;
        }
        for (int i = theChars.length - 1; i >= 0; i--) {
            val = theChars[i] - '0';
            if (val < 0 || val > 10) {
                throw new InvalidStandardNumberException("not a digit in PPN");
            }
            checksum += (val * weight++);
        }
        return checksum % 11 == 0;
    }
}
