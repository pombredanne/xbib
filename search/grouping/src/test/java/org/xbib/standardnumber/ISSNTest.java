/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.standardnumber;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test serial number recognition
 *
 */
public class ISSNTest extends Assert {

    @Test
    public void testCorrectISSN() {
        try {
            ISSN issn = new ISSN("2193-5777");
            String s = issn.getStandardNumberValue();
            assertEquals(s, "21935777");
        } catch (InvalidStandardNumberException s) {
            assertTrue(Boolean.FALSE);
        }
    }
    
    @Test
    public void testCorrectISSN2() {
        try {
            ISSN issn = new ISSN("0044-2410");
            String s = issn.getStandardNumberValue();
            assertEquals(s, "00442410");
        } catch (InvalidStandardNumberException s) {
            assertTrue(Boolean.FALSE);
        }
    }

    @Test
    public void testCorrectISSN3() {
        try {
            ISSN issn = new ISSN("1934-791X");
            String s = issn.getStandardNumberValue();
            assertEquals(s, "1934791X");
        } catch (InvalidStandardNumberException s) {
            assertTrue(Boolean.FALSE);
        }
    }    
    
    @Test
    public void testWrongISSN() {
        try {
            ISSN issn = new ISSN("0949-3051"); // correct:  0949-3050 
            String s = issn.getStandardNumberValue();
        } catch (InvalidStandardNumberException s) {
            assertTrue(Boolean.TRUE);
            return;
        }
        assertTrue(Boolean.FALSE);
    }

    @Test
    public void testDirtyISSN() throws Exception {
        ISSN issn = new ISSN("ISSN 0936-0204");
        String s = issn.getStandardNumberValue();
        assertEquals("09360204", s);
    }

}
