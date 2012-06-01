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
package org.xbib.marc.addons;

import org.xbib.tools.hbz.AlephSeq2MarcXML;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.testng.annotations.Test;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;

public class ConcurrentAlephSequentialTest {

    public void testConcurrent() throws InterruptedException, ExecutionException {
        URI[] uris = Arrays.asList(
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.aa.seq.no.marc"),
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.ab.seq.no.marc"),
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.ac.seq.no.marc"),
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.ad.seq.no.marc"),
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.ae.seq.no.marc"),
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.af.seq.no.marc"),
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.ag.seq.no.marc"),
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.ah.seq.no.marc"),
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.ai.seq.no.marc"),
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.aj.seq.no.marc"),
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.ak.seq.no.marc"),
                URI.create("file:///Users/joerg/Projects/hbz/MARC21_BVB/m0.20111130.sys.al.seq.no.marc")).toArray(new URI[0]);
        ImporterFactory factory = new ImporterFactory() {

            @Override
            public Importer newImporter() {
                return new AlephSeq2MarcXML();
            }
        };
        ImportService service = new ImportService().setThreads(8).setFactory(factory).run(uris);
    }
}
