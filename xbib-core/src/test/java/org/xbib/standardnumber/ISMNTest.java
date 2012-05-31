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

public class ISMNTest extends Assert {

    @Test
    public void testISMN() throws Exception {
        String value = "9790345246805";
        ISMN ismn = new ISMN(value, true);
        assertEquals(ismn.getStandardNumberValue(), "9790345246805");
        assertEquals(ismn.getStandardNumberPrintableRepresentation(), "979-0-3452-4680-5");
    }

    @Test
    public void testISMN2() throws Exception {
        String value = "ISMN 979050025192";
        ISMN ismn = new ISMN(value, true, true);
        assertEquals(ismn.getStandardNumberValue(), "9790500251927");
        assertEquals(ismn.getStandardNumberPrintableRepresentation(), "979-0-50025-192-7");
    }

    @Test
    public void testISMN3() throws Exception {
        ISMN ismn = new ISMN("M230671187", false);
        assertEquals(ismn.getStandardNumberValue(), "M230671187");
        assertEquals(ismn.getStandardNumberPrintableRepresentation(), "M-2306-7118-7");
    }

    @Test
    public void testISMN4() throws Exception {
        ISMN ismn = new ISMN("ISMN M-006-49329-6 : DM 18.00", true, true);
        assertEquals(ismn.getStandardNumberValue(), "9790006493296");
        assertEquals(ismn.getStandardNumberPrintableRepresentation(), "979-0-006-49329-6");
    }

    @Test
    public void testISMN5() throws Exception {
        ISMN ismn = new ISMN("ISMN M-50006-068-0 (Pr. nicht mitget.)", true, true);
        assertEquals(ismn.getStandardNumberValue(), "9790500060680");
        assertEquals(ismn.getStandardNumberPrintableRepresentation(), "979-0-50006-068-0");
    }

}
