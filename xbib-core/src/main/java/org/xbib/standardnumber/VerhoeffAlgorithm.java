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
 * @see <a href="http://en.wikipedia.org/wiki/Verhoeff_algorithm/">More Info</a>
 * @see <a href="http://en.wikipedia.org/wiki/Dihedral_group">Dihedral Group</a>
 * @see <a href="http://mathworld.wolfram.com/DihedralGroupD5.html">Dihedral
 * Group Order 10</a>
 * @author Colm Rice
 */
public class VerhoeffAlgorithm {

    // The multiplication table
    private final static int[][] d = new int[][]{
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
        {1, 2, 3, 4, 0, 6, 7, 8, 9, 5},
        {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
        {3, 4, 0, 1, 2, 8, 9, 5, 6, 7},
        {4, 0, 1, 2, 3, 9, 5, 6, 7, 8},
        {5, 9, 8, 7, 6, 0, 4, 3, 2, 1},
        {6, 5, 9, 8, 7, 1, 0, 4, 3, 2},
        {7, 6, 5, 9, 8, 2, 1, 0, 4, 3},
        {8, 7, 6, 5, 9, 3, 2, 1, 0, 4},
        {9, 8, 7, 6, 5, 4, 3, 2, 1, 0}
    };
    // The permutation table
    private final static int[][] p = new int[][]{
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
        {1, 5, 7, 6, 2, 8, 3, 0, 9, 4},
        {5, 8, 0, 3, 7, 9, 6, 1, 4, 2},
        {8, 9, 1, 6, 0, 4, 3, 5, 2, 7},
        {9, 4, 5, 3, 1, 2, 6, 8, 7, 0},
        {4, 2, 8, 6, 5, 7, 3, 9, 0, 1},
        {2, 7, 9, 3, 8, 0, 6, 4, 1, 5},
        {7, 0, 4, 6, 9, 1, 3, 2, 5, 8}
    };
    // The inverse table
    private final static int[] inv = {0, 4, 3, 2, 1, 5, 6, 7, 8, 9};

    /*
     * For a given number generates a Verhoeff digit
     *
     */
    public static String generateVerhoeff(String num) {
        int c = 0;
        int[] myArray = stringToReversedIntArray(num);
        for (int i = 0; i < myArray.length; i++) {
            c = d[c][p[((i + 1) % 8)][myArray[i]]];
        }
        return Integer.toString(inv[c]);
    }

    /*
     * Validates that an entered number is Verhoeff compliant. NB: Make sure the
     * check digit is the last one.
     */
    public static boolean validateVerhoeff(String num) {
        int c = 0;
        int[] myArray = stringToReversedIntArray(num);
        for (int i = 0; i < myArray.length; i++) {
            c = d[c][p[(i % 8)][myArray[i]]];
        }
        return (c == 0);
    }

    /*
     * Converts a string to a reversed integer array.
     */
    private static int[] stringToReversedIntArray(String num) {
        int[] myArray = new int[num.length()];
        for (int i = 0; i < num.length(); i++) {
            myArray[i] = Integer.parseInt(num.substring(i, i + 1));
        }
        myArray = reverse(myArray);
        return myArray;
    }

    /*
     * Reverses an int array
     */
    private static int[] reverse(int[] myArray) {
        int[] reversed = new int[myArray.length];
        for (int i = 0; i < myArray.length; i++) {
            reversed[i] = myArray[myArray.length - (i + 1)];
        }
        return reversed;
    }
}