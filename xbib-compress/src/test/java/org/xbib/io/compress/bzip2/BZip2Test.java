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
package org.xbib.io.compress.bzip2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

public class BZip2Test {

    @Test
    public void testBZip2HelloWorld() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bzip2OutputStream zOut = new Bzip2OutputStream(out);
        ObjectOutputStream objOut = new ObjectOutputStream(zOut);
        String helloWorld = "Hello World!";
        objOut.writeObject(helloWorld);
        zOut.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Bzip2InputStream zIn = new Bzip2InputStream(in);
        ObjectInputStream objIn = new ObjectInputStream(zIn);
        assertEquals("Hello World!", objIn.readObject());
    }

}
