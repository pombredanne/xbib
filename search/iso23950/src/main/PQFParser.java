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
package org.xbib.io.iso23950.pqf;

import java.io.Reader;

/**

from Mikey Taylor, IndexData:

"Prefix Query Format (PQF), also known as
Prefix Query Notation (PQN) was defined in 1995, as part of the YAZ
toolkit, and has since become the de facto standard representation of
RPN queries."

 From: http://www.indexdata.com/yaz/doc/tools.tkl#PQF

 The grammar of the PQF is as follows:

     query ::= top-set query-struct.

     top-set ::= [ '@attrset' string ]

     query-struct ::= attr-spec | simple | complex | '@term' term-type query

     attr-spec ::= '@attr' [ string ] string query-struct

     complex ::= operator query-struct query-struct.

     operator ::= '@and' | '@or' | '@not' | '@prox' proximity.

     simple ::= result-set | term.

     result-set ::= '@set' string.

     term ::= string.

     proximity ::= exclusion distance ordered relation which-code unit-code.

     exclusion ::= '1' | '0' | 'void'.

     distance ::= integer.

     ordered ::= '1' | '0'.

     relation ::= integer.

     which-code ::= 'known' | 'private' | integer.

     unit-code ::= integer.

     term-type ::= 'general' | 'numeric' | 'string' | 'oid' | 'datetime' | 'null'.
    

 You will note that the syntax above is a fairly faithful representation of RPN, 
 except for the Attribute, which has been moved a step away from the term, 
 allowing you to associate one or more attributes with an entire query structure. 
 The parser will automatically apply the given attributes to each term as required.

 The @attr operator is followed by an attribute specification (attr-spec above). 
 The specification consists of an optional attribute set, an attribute 
 type-value pair and a sub-query. The attribute type-value pair is packed 
 in one string: an attribute type, an equals sign, and an attribute value, 
 like this: @attr 1=1003. The type is always an integer but the value may be 
 either an integer or a string (if it doesn't start with a digit character). 
 A string attribute-value is encoded as a Type-1 ``complex'' attribute with 
 the list of values containing the single string specified, and including 
 no semantic indicators. 

*/


public class PQFParser implements PQFTokens {
    private int yyss = 100;
    private int yytok;
    private int yysp = 0;
    private int[] yyst;
    protected int yyerrno = (-1);
    private Object[] yysv;
    private Object yyrv;

    public boolean parse() {
        int yyn = 0;
        yysp = 0;
        yyst = new int[yyss];
        yysv = new Object[yyss];
        yytok = (lexer.getToken()
                 );
    loop:
        for (;;) {
            switch (yyn) {
                case 0:
                    yyst[yysp] = 0;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 33:
                    yyn = yys0();
                    continue;

                case 1:
                    yyst[yysp] = 1;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 34:
                    switch (yytok) {
                        case ENDINPUT:
                            yyn = 66;
                            continue;
                    }
                    yyn = 69;
                    continue;

                case 2:
                    yyst[yysp] = 2;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 35:
                    yyn = yys2();
                    continue;

                case 3:
                    yyst[yysp] = 3;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 36:
                    yyn = yys3();
                    continue;

                case 4:
                    yyst[yysp] = 4;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 37:
                    yyn = yys4();
                    continue;

                case 5:
                    yyst[yysp] = 5;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 38:
                    switch (yytok) {
                        case CHARSTRING1:
                            yyn = 15;
                            continue;
                        case INTEGER:
                            yyn = 16;
                            continue;
                    }
                    yyn = 69;
                    continue;

                case 6:
                    yyst[yysp] = 6;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 39:
                    switch (yytok) {
                        case CHARSTRING1:
                            yyn = 17;
                            continue;
                    }
                    yyn = 69;
                    continue;

                case 7:
                    yyst[yysp] = 7;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 40:
                    yyn = yys7();
                    continue;

                case 8:
                    yyst[yysp] = 8;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 41:
                    yyn = yys8();
                    continue;

                case 9:
                    yyst[yysp] = 9;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 42:
                    yyn = yys9();
                    continue;

                case 10:
                    yyst[yysp] = 10;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 43:
                    yyn = yys10();
                    continue;

                case 11:
                    yyst[yysp] = 11;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 44:
                    switch (yytok) {
                        case CHARSTRING1:
                            yyn = 20;
                            continue;
                    }
                    yyn = 69;
                    continue;

                case 12:
                    yyst[yysp] = 12;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 45:
                    switch (yytok) {
                        case TERMTYPE:
                            yyn = 21;
                            continue;
                    }
                    yyn = 69;
                    continue;

                case 13:
                    yyst[yysp] = 13;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 46:
                    yyn = yys13();
                    continue;

                case 14:
                    yyst[yysp] = 14;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 47:
                    yyn = yys14();
                    continue;

                case 15:
                    yyst[yysp] = 15;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 48:
                    switch (yytok) {
                        case INTEGER:
                            yyn = 16;
                            continue;
                    }
                    yyn = 69;
                    continue;

                case 16:
                    yyst[yysp] = 16;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 49:
                    switch (yytok) {
                        case EQUALS:
                            yyn = 25;
                            continue;
                    }
                    yyn = 69;
                    continue;

                case 17:
                    yyst[yysp] = 17;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 50:
                    yyn = yys17();
                    continue;

                case 18:
                    yyst[yysp] = 18;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 51:
                    yyn = yys18();
                    continue;

                case 19:
                    yyst[yysp] = 19;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 52:
                    yyn = yys19();
                    continue;

                case 20:
                    yyst[yysp] = 20;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 53:
                    yyn = yys20();
                    continue;

                case 21:
                    yyst[yysp] = 21;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 54:
                    yyn = yys21();
                    continue;

                case 22:
                    yyst[yysp] = 22;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 55:
                    yyn = yys22();
                    continue;

                case 23:
                    yyst[yysp] = 23;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 56:
                    yyn = yys23();
                    continue;

                case 24:
                    yyst[yysp] = 24;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 57:
                    yyn = yys24();
                    continue;

                case 25:
                    yyst[yysp] = 25;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 58:
                    switch (yytok) {
                        case CHARSTRING1:
                            yyn = 31;
                            continue;
                        case INTEGER:
                            yyn = 32;
                            continue;
                    }
                    yyn = 69;
                    continue;

                case 26:
                    yyst[yysp] = 26;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 59:
                    yyn = yys26();
                    continue;

                case 27:
                    yyst[yysp] = 27;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 60:
                    yyn = yys27();
                    continue;

                case 28:
                    yyst[yysp] = 28;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 61:
                    yyn = yys28();
                    continue;

                case 29:
                    yyst[yysp] = 29;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 62:
                    yyn = yys29();
                    continue;

                case 30:
                    yyst[yysp] = 30;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 63:
                    yyn = yys30();
                    continue;

                case 31:
                    yyst[yysp] = 31;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 64:
                    yyn = yys31();
                    continue;

                case 32:
                    yyst[yysp] = 32;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 65:
                    yyn = yys32();
                    continue;

                case 66:
                    return true;
                case 67:
                    yyerror("stack overflow");
                case 68:
                    return false;
                case 69:
                    yyerror("syntax error");
                    return false;
            }
        }
    }

    protected void yyexpand() {
        int[] newyyst = new int[2*yyst.length];
        Object[] newyysv = new Object[2*yyst.length];
        for (int i=0; i<yyst.length; i++) {
            newyyst[i] = yyst[i];
            newyysv[i] = yysv[i];
        }
        yyst = newyyst;
        yysv = newyysv;
    }

    private int yys0() {
        switch (yytok) {
            case AND:
                return 4;
            case ATTR:
                return 5;
            case ATTRSET:
                return 6;
            case CHARSTRING1:
                return 7;
            case CHARSTRING2:
                return 8;
            case NOT:
                return 9;
            case OR:
                return 10;
            case SET:
                return 11;
            case TERM:
                return 12;
        }
        return 69;
    }

    private int yys2() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr8();
        }
        return 69;
    }

    private int yys3() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr2();
        }
        return 69;
    }

    private int yys4() {
        switch (yytok) {
            case AND:
                return 4;
            case ATTR:
                return 5;
            case CHARSTRING1:
                return 7;
            case CHARSTRING2:
                return 8;
            case NOT:
                return 9;
            case OR:
                return 10;
            case SET:
                return 11;
            case TERM:
                return 12;
        }
        return 69;
    }

    private int yys7() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr6();
        }
        return 69;
    }

    private int yys8() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr7();
        }
        return 69;
    }

    private int yys9() {
        switch (yytok) {
            case AND:
                return 4;
            case ATTR:
                return 5;
            case CHARSTRING1:
                return 7;
            case CHARSTRING2:
                return 8;
            case NOT:
                return 9;
            case OR:
                return 10;
            case SET:
                return 11;
            case TERM:
                return 12;
        }
        return 69;
    }

    private int yys10() {
        switch (yytok) {
            case AND:
                return 4;
            case ATTR:
                return 5;
            case CHARSTRING1:
                return 7;
            case CHARSTRING2:
                return 8;
            case NOT:
                return 9;
            case OR:
                return 10;
            case SET:
                return 11;
            case TERM:
                return 12;
        }
        return 69;
    }

    private int yys13() {
        switch (yytok) {
            case AND:
                return 4;
            case ATTR:
                return 5;
            case CHARSTRING1:
                return 7;
            case CHARSTRING2:
                return 8;
            case NOT:
                return 9;
            case OR:
                return 10;
            case SET:
                return 11;
            case TERM:
                return 12;
        }
        return 69;
    }

    private int yys14() {
        switch (yytok) {
            case AND:
                return 4;
            case ATTR:
                return 5;
            case CHARSTRING1:
                return 7;
            case CHARSTRING2:
                return 8;
            case NOT:
                return 9;
            case OR:
                return 10;
            case SET:
                return 11;
            case TERM:
                return 12;
        }
        return 69;
    }

    private int yys17() {
        switch (yytok) {
            case AND:
                return 4;
            case ATTR:
                return 5;
            case CHARSTRING1:
                return 7;
            case CHARSTRING2:
                return 8;
            case NOT:
                return 9;
            case OR:
                return 10;
            case SET:
                return 11;
            case TERM:
                return 12;
        }
        return 69;
    }

    private int yys18() {
        switch (yytok) {
            case AND:
                return 4;
            case ATTR:
                return 5;
            case CHARSTRING1:
                return 7;
            case CHARSTRING2:
                return 8;
            case NOT:
                return 9;
            case OR:
                return 10;
            case SET:
                return 11;
            case TERM:
                return 12;
        }
        return 69;
    }

    private int yys19() {
        switch (yytok) {
            case AND:
                return 4;
            case ATTR:
                return 5;
            case CHARSTRING1:
                return 7;
            case CHARSTRING2:
                return 8;
            case NOT:
                return 9;
            case OR:
                return 10;
            case SET:
                return 11;
            case TERM:
                return 12;
        }
        return 69;
    }

    private int yys20() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr5();
        }
        return 69;
    }

    private int yys21() {
        switch (yytok) {
            case AND:
                return 4;
            case ATTR:
                return 5;
            case ATTRSET:
                return 6;
            case CHARSTRING1:
                return 7;
            case CHARSTRING2:
                return 8;
            case NOT:
                return 9;
            case OR:
                return 10;
            case SET:
                return 11;
            case TERM:
                return 12;
        }
        return 69;
    }

    private int yys22() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr12();
        }
        return 69;
    }

    private int yys23() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr3();
        }
        return 69;
    }

    private int yys24() {
        switch (yytok) {
            case AND:
                return 4;
            case ATTR:
                return 5;
            case CHARSTRING1:
                return 7;
            case CHARSTRING2:
                return 8;
            case NOT:
                return 9;
            case OR:
                return 10;
            case SET:
                return 11;
            case TERM:
                return 12;
        }
        return 69;
    }

    private int yys26() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr1();
        }
        return 69;
    }

    private int yys27() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr14();
        }
        return 69;
    }

    private int yys28() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr13();
        }
        return 69;
    }

    private int yys29() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr9();
        }
        return 69;
    }

    private int yys30() {
        switch (yytok) {
            case SET:
            case OR:
            case NOT:
            case TERM:
            case CHARSTRING2:
            case ENDINPUT:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr4();
        }
        return 69;
    }

    private int yys31() {
        switch (yytok) {
            case SET:
            case OR:
            case TERM:
            case NOT:
            case CHARSTRING2:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr11();
        }
        return 69;
    }

    private int yys32() {
        switch (yytok) {
            case SET:
            case OR:
            case TERM:
            case NOT:
            case CHARSTRING2:
            case CHARSTRING1:
            case ATTR:
            case AND:
                return yyr10();
        }
        return 69;
    }

    private int yyr1() { // pqf : ATTRSET CHARSTRING1 querystruct
        {
        this.pqf = new PQF(((Query)yysv[yysp-1])); 
        yyrv = this.pqf;
    }
        yysv[yysp-=3] = yyrv;
        return yyppqf();
    }

    private int yyr2() { // pqf : querystruct
        {
        this.pqf = new PQF(((Query)yysv[yysp-1])); 
        yyrv = this.pqf;
    }
        yysv[yysp-=1] = yyrv;
        return yyppqf();
    }

    private int yyppqf() {
        switch (yyst[yysp-1]) {
            case 0: return 1;
            default: return 29;
        }
    }

    private int yyr12() { // expression : AND querystruct querystruct
        {
        yyrv = new Expression(((String)yysv[yysp-3]), ((Query)yysv[yysp-2]), ((Query)yysv[yysp-1]));
    }
        yysv[yysp-=3] = yyrv;
        return 2;
    }

    private int yyr13() { // expression : OR querystruct querystruct
        {
        yyrv = new Expression(((String)yysv[yysp-3]), ((Query)yysv[yysp-2]), ((Query)yysv[yysp-1]));
    }
        yysv[yysp-=3] = yyrv;
        return 2;
    }

    private int yyr14() { // expression : NOT querystruct querystruct
        {
        yyrv = new Expression(((String)yysv[yysp-3]), ((Query)yysv[yysp-2]), ((Query)yysv[yysp-1]));
    }
        yysv[yysp-=3] = yyrv;
        return 2;
    }

    private int yyr10() { // attrspec : INTEGER EQUALS INTEGER
        {
        yyrv = new AttrSpec(((Integer)yysv[yysp-3]), ((Integer)yysv[yysp-1]));
    }
        yysv[yysp-=3] = yyrv;
        return yypattrspec();
    }

    private int yyr11() { // attrspec : INTEGER EQUALS CHARSTRING1
        {
        yyrv = new AttrSpec(((Integer)yysv[yysp-3]), ((String)yysv[yysp-1]));
    }
        yysv[yysp-=3] = yyrv;
        return yypattrspec();
    }

    private int yypattrspec() {
        switch (yyst[yysp-1]) {
            case 5: return 14;
            default: return 24;
        }
    }

    private int yyr3() { // querystruct : ATTR attrspec querystruct
        {
        yyrv = new Query(((AttrSpec)yysv[yysp-2]), ((Query)yysv[yysp-1]));
    }
        yysv[yysp-=3] = yyrv;
        return yypquerystruct();
    }

    private int yyr4() { // querystruct : ATTR CHARSTRING1 attrspec querystruct
        {
        yyrv = new Query(((String)yysv[yysp-3]), ((AttrSpec)yysv[yysp-2]), ((Query)yysv[yysp-1]));
    }
        yysv[yysp-=4] = yyrv;
        return yypquerystruct();
    }

    private int yyr5() { // querystruct : SET CHARSTRING1
        {
        yyrv = new Query(((String)yysv[yysp-2]), ((String)yysv[yysp-1]));
    }
        yysv[yysp-=2] = yyrv;
        return yypquerystruct();
    }

    private int yyr6() { // querystruct : CHARSTRING1
        {
        yyrv = new Query(((String)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return yypquerystruct();
    }

    private int yyr7() { // querystruct : CHARSTRING2
        {
        yyrv = new Query(((String)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return yypquerystruct();
    }

    private int yyr8() { // querystruct : expression
        {
        yyrv = new Query(((Expression)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return yypquerystruct();
    }

    private int yyr9() { // querystruct : TERM TERMTYPE pqf
        {
        yyrv = new Query(((PQF)yysv[yysp-1]));
    }
        yysv[yysp-=3] = yyrv;
        return yypquerystruct();
    }

    private int yypquerystruct() {
        switch (yyst[yysp-1]) {
            case 24: return 30;
            case 19: return 28;
            case 18: return 27;
            case 17: return 26;
            case 14: return 23;
            case 13: return 22;
            case 10: return 19;
            case 9: return 18;
            case 4: return 13;
            default: return 3;
        }
    }

    protected String[] yyerrmsgs = {
    };


    private PQFLexer lexer;
        
    private PQF pqf;
        
    public PQFParser(Reader r) {
        this.lexer = new PQFLexer(r);
        lexer.nextToken();
    }

    public void yyerror(String error) {
        throw new SyntaxException("PQF error at " 
            + "[" + lexer.getLine() + "," + lexer.getColumn() +"]"
            + ": " + (yyerrno >= 0 ? yyerrmsgs[yyerrno] : error) );
    }
    
    public PQF getResult()
    {
        return pqf;
    }


}
