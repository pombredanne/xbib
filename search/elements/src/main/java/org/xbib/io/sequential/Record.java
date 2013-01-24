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

import java.io.Serializable;

/**
 * A separable record for structured data.
 * Also known as control characters group 0 ("C0"), ASCII-1967
 * defines units, records, groups and files as separable hierarchically
 * organized data structures. The structures are separeted not by protocol,
 * but by embedded separator codes.
 * Originally, these codes were used to simulate punch card data on magnetic
 * tape. Trailing blanks on tape could be saved by using separator characters
 * instead.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class Record extends AbstractSeparable implements Serializable {

    public Record(String str) {
        super(str);
    }

    @Override
    public final char getSeparator() {
        return RS;
    }
}
