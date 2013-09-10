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
 * ISBN converter and formatter class Copyright 2000-2005  by Openly Informatics, Inc.
 * http://www.openly.com/ This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version. This
 * library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details. You should have received a copy of the GNU
 * Lesser General Public License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA
 *
 * @see <a href="http://www.s.org/standards/home/s/international/html/usm12.htm">The ISBN Users' Manual</a>
 * @see <a href="http://www.ietf.org/html.charters/OLD/urn-charter.html">The IETF URN Charter</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces">The IANA URN assignments</a>
 * @see <a href="http://www.isbn-international.org/download/List%20of%20Ranges.pdf">ISBN prefix list</a>
 */
public class ISBN extends AbstractStandardBookNumber {

    /**
     * Create a new ISBN object. ISBN-13 is preferred as value.
     *
     * @param lexicalForm the lexical form of the ISBN candidate string
     * Parse a lexical form of this datatype to a value
     */
    public ISBN(String lexicalForm) throws InvalidStandardNumberException {
        super(lexicalForm, true, false);
    }

    public ISBN(String lexicalForm, boolean asEAN) throws InvalidStandardNumberException {
        super(lexicalForm, asEAN, false);
    }

    public ISBN(String lexicalForm, boolean asEAN, boolean createChecksum) throws InvalidStandardNumberException {
        super(lexicalForm, asEAN, createChecksum);
    }


    @Override
    public String getAcronym() {
        return "ISBN";
    }

}
