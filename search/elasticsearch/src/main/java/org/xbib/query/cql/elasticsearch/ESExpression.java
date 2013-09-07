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
package org.xbib.query.cql.elasticsearch;

/**
 * Elasticsearch expression
 *
 */
public class ESExpression implements Node {

    private Operator op;

    private Node[] args;

    private TokenType type;

    private boolean visible;

    /**
     * Constructor for folding an expression.
     * Folding takes place when expressions are constructed on the stack
     * to unwrap binary operations into n-ary operations.
     *
     * @param expr the expression
     * @param arg the extra argument
     */
    public ESExpression(ESExpression expr, Node arg) {        
        this.op = expr.getOperator();
        Node[] exprargs = expr.getArgs();
        this.args = new Node[exprargs.length + 1];
        // our organization of the argument list is reverse,
        // the latest arg is at position 0
        this.args[0] = arg;
        System.arraycopy(exprargs, 0, this.args, 1, exprargs.length);
        this.visible = true;
        for (Node node : args) {
            if (node instanceof ESName) {
                this.visible = visible && node.isVisible();
                this.type = node.getType();
            }
        }
    }
    
    public ESExpression(Operator op, Node... args) {
        this.op = op;
        this.args = args;
        this.type = TokenType.EXPRESSION;
        this.visible = false;
        for (Node arg : args) {
            if (arg instanceof ESName || arg instanceof ESExpression ) {
                this.visible = visible || arg.isVisible();
            }
        }        
    }

    public Operator getOperator() {
        return op;
    }

    public Node[] getArgs() {
        return args;
    }
    
    public Node getArg1() {
        return args[0];
    }

    public Node getArg2() {
        return args[1];
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        // only for debugging
        if (!visible) {
            return "";
        }
        StringBuilder sb = new StringBuilder(op.toString());
        sb.append('(');
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length - 1) sb.append(',');
        }
        sb.append(')');
        return sb.toString();
    }
}
