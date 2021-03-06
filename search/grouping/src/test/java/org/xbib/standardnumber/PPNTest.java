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

public class PPNTest extends Assert {
    
    @Test
    public void testPPN1() throws Exception {
        PPN ppn = new PPN("641379617");
        String s = ppn.getStandardNumberValue();
        assertEquals(s, "641379617");
    }

    @Test
    public void testPPN2() throws Exception {
        PPN ppn = new PPN("101115658X");
        String s = ppn.getStandardNumberValue();
        assertEquals(s, "101115658X");
    }

}
