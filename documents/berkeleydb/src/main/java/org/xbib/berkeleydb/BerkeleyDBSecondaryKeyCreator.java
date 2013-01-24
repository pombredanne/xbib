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
package org.xbib.berkeleydb;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Create a key for a secondary database. Read secondary URI from resource.
 *
 */
public class BerkeleyDBSecondaryKeyCreator implements SecondaryKeyCreator {

    private final Pattern pattern = Pattern.compile(".*?^@prefix secondary: <(.+?)> \\.$.*",
            Pattern.DOTALL|Pattern.UNIX_LINES|Pattern.MULTILINE);

    /**
     * Create secondary key.
     * @param secondaryDatabase the secondary database
     * @param keyEntry the primary key
     * @param dataEntry the value of the primary key
     * @param resultEntry the entry for the created secondary key
     * @return true if secondary key was created
     * @throws DatabaseException
     */
    @Override
    public boolean createSecondaryKey(SecondaryDatabase secondaryDatabase,
            DatabaseEntry keyEntry, DatabaseEntry dataEntry, DatabaseEntry resultEntry) throws DatabaseException {
        try {
            String buffer = new String(dataEntry.getData(), "UTF-8").trim();
            Matcher m = pattern.matcher(buffer);
            if (m.matches()) {
                String result = m.group(1);
                resultEntry.setData(result.getBytes("UTF-8"));
                return true;
            }
        } catch (UnsupportedEncodingException ex) {
            // can't happen
        }
        return false;
    }
}
