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
package org.xbib.analyzer.elements.marc;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.MARCContext;
import org.xbib.elements.marc.MARCElement;
import org.xbib.marc.FieldCollection;

public class GeneralInformation extends MARCElement {
    private final static GeneralInformation instance = new GeneralInformation();
    
    public static MARCElement getInstance() {
        return instance;
    }

    /**
     * Example "991118d19612006xx z||p|r ||| 0||||0ger c"
     */
    @Override
    public void fields(ElementBuilder<FieldCollection, String, MARCElement, MARCContext> builder,
                       FieldCollection fields, String value) {
        String date1 = value.substring(7,11);
        builder.context().resource().add("date1", check(date1));
        String date2 = value.substring(11,15);
        builder.context().resource().add("date2", check(date2));
    }

    // check for valid date, else fill with 9999
    private String check(String date) {
        try {
            int d = Integer.parseInt(date);
            if (d < 1650) { // "0000"
                return "9999";
            }
        } catch (Exception e) {
            return "9999";
        }
        return date;
    }
}
