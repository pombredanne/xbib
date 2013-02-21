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
package org.xbib.io.sequential;

import java.io.InputStream;
import java.io.InputStreamReader;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class CharStreamTest {

    private static final Logger logger = LoggerFactory.getLogger(CharStreamTest.class.getName());

    int dataCount = 0;
    int unitCount = 0;
    int recordCount = 0;
    int groupCount= 0;
    int fileCount = 0;

    private void incDataCount() {
        dataCount++;
    }
    private void incUnitCount() {
        unitCount++;
    }

    private void incRecordCount() {
        recordCount++;
    }

    private void incGroupCount() {
        groupCount++;
    }

    private void incFileCount() {
        fileCount++;
    }


    @Test
    public void testStream() throws Exception {

        CharStreamListener listener = new CharStreamListener() {
            @Override
            public void data(String data) {
                incDataCount();
            }

            @Override
            public void markUnit() {
                incUnitCount();
            }

            @Override
            public void markRecord() {
                incRecordCount();
            }

            @Override
            public void markGroup() {
               incGroupCount();
            }

            @Override
            public void markFile() {
                incFileCount();
            }
        };

        InputStream in = getClass().getResourceAsStream("sequential.groupstream");
        CharStream stream = CharStreamFactory.getInstance().newStream(new InputStreamReader(in), 8192, listener);

        try {
            while (stream.ready()) {
                stream.readData();
            }
        } finally {
            stream.close();
        }
        logger.info("data = {} unit = {} record = {} group = {} file = {}",
                dataCount, unitCount, recordCount, groupCount, fileCount);
        
        assertEquals(dataCount, 380);
        assertEquals(unitCount, 23);
        assertEquals(recordCount, 356);
        assertEquals(groupCount, 10);
        assertEquals(fileCount, 1);
        
    }
}
