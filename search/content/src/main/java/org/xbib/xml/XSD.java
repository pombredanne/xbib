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
package org.xbib.xml;

import org.xbib.iri.IRI;

public interface XSD {

    IRI STRING = IRI.create("xsd:string");

    IRI BOOLEAN = IRI.create("xsd:boolean");

    IRI DECIMAL = IRI.create("xsd:decimal");

    IRI FLOAT = IRI.create("xsd:float");

    IRI DOUBLE = IRI.create("xsd:double");

    IRI DURATION = IRI.create("xsd:duration");

    IRI DATETIME = IRI.create("xsd:dateTime");

    IRI TIME = IRI.create("xsd:time");

    IRI DATE = IRI.create("xsd:date");

    IRI GYEARMOTH = IRI.create("xsd:gYearMonth");

    IRI GYEAR = IRI.create("xsd:gYear");

    IRI GMONTHDAY = IRI.create("xsd:gMonthDay");

    IRI GDAY = IRI.create("xsd:gDay");

    IRI GMONTH = IRI.create("xsd:gMonth");

    IRI HEXBINARY = IRI.create("xsd:hexBinary");

    IRI BASE64BINARY = IRI.create("xsd:base64Binary");

    IRI ANYURI = IRI.create("xsd:anyURI");

    IRI QNAME = IRI.create("xsd:QName");

    IRI NOTATION = IRI.create("xsd:NOTATION");

    IRI NORMALIZEDSTRING = IRI.create("xsd:normalizedString");

    IRI TOKEN = IRI.create("xsd:token");

    IRI LANGUAGE = IRI.create("xsd:language");

    IRI NMTOKEN = IRI.create("xsd:NMTOKEN");

    IRI NMTOKENS = IRI.create("xsd:NMTOKENS");

    IRI NAME = IRI.create("xsd:Name");

    IRI NCNAME = IRI.create("xsd:NCName");

    IRI ID = IRI.create("xsd:ID");

    IRI IDREF = IRI.create("xsd:IDREF");

    IRI IDREFS = IRI.create("xsd:IDREFS");

    IRI ENTITY = IRI.create("xsd:ENTITY");

    IRI ENTITIES = IRI.create("xsd:ENTITIES");

    IRI INTEGER = IRI.create("xsd:integer");

    IRI NONPOSITIVEINTEGER = IRI.create("xsd:nonPositiveInteger");

    IRI NEGATIVEINTEGER = IRI.create("xsd:negativeInteger");

    IRI LONG = IRI.create("xsd:long");

    IRI INT = IRI.create("xsd:int");

    IRI SHORT = IRI.create("xsd:short");

    IRI BYTE = IRI.create("xsd:byte");

    IRI NONNEGATIVEINTEGER = IRI.create("xsd:nonNegativeInteger");

    IRI UNSIGNEDLONG = IRI.create("xsd:unsignedLong");

    IRI UNSIGNEDINT = IRI.create("xsd:unsignedInt");

    IRI UNSIGNEDSHORT = IRI.create("xsd:unsignedShort");

    IRI UNSIGNEDBYTE = IRI.create("xsd:unsignedByte");

    IRI POSITIVEINTEGER = IRI.create("xsd:positiveInteger");

}
