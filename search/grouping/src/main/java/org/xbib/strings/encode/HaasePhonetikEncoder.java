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
package org.xbib.strings.encode;

/**
 * Ge&auml;nderter Algorithmus aus der Matching Toolbox von Rainer Schnell
 * und J&ouml;rg Reiher
 * <p/>
 * Die Kölner Phonetik wurde für den Einsatz in Namensdatenbanken wie
 * der Verwaltung eines Krankenhauses durch Martin Haase (Institut für
 * Sprachwissenschaft, Universität zu Köln) und Kai Heitmann (Insitut für
 * medizinische Statistik, Informatik und Epidemiologie, Köln)  überarbeitet.
 * M. Haase und K. Heitmann. Die Erweiterte Kölner Phonetik. 526, 2000.
 * <p/>
 * nach: Martin Wilz, Aspekte der Kodierung phonetischer Ähnlichkeiten
 * in deutschen Eigennamen, Magisterarbeit.
 * http://www.uni-koeln.de/phil-fak/phonetik/Lehre/MA-Arbeiten/magister_wilz.pdf
 *
 */
public class HaasePhonetikEncoder extends KoelnerPhonetikEncoder {

    private final static String[] HAASE_VARIATIONS_PATTERNS = {"OWN", "RB", "WSK", "A$", "O$", "SCH",
            "GLI", "EAU$", "^CH", "AUX", "EUX", "ILLE"};
    private final static String[] HAASE_VARIATIONS_REPLACEMENTS = {"AUN", "RW", "RSK", "AR", "OW", "CH",
            "LI", "O", "SCH", "O", "O", "I"};

    /**
     * @return
     */
    @Override
    protected String[] getPatterns() {
        return HAASE_VARIATIONS_PATTERNS;
    }

    /**
     * @return
     */
    @Override
    protected String[] getReplacements() {
        return HAASE_VARIATIONS_REPLACEMENTS;
    }

    /**
     * @return
     */
    @Override
    protected char getCode() {
        return '9';
    }
}
