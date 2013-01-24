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
package org.xbib.query.cql;

import java.util.HashMap;
import java.util.Map;

/**
 *  Abstract syntax tree of CQL - boolean operator enumeration
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public enum BooleanOperator {

    AND("and"),
    OR("or"),
    NOT("not"),
    PROX("prox");
    /** token/operator map */
    private static Map<String, BooleanOperator> tokenMap;
    /** operator/token map */
    private static Map<BooleanOperator, String> opMap;
    private String token;

    /**
     * Creates a new Operator object.
     *
     * @param token the operator token
     */
    private BooleanOperator(String token) {
        this.token = token;
        map(token, this);
    }

    /**
     * Map token to operator
     *
     * @param token the token
     * @param op the operator
     */
    private static void map(String token, BooleanOperator op) {
        if (tokenMap == null) {
            tokenMap = new HashMap();
        }
        tokenMap.put(token, op);
        if (opMap == null) {
            opMap = new HashMap();
        }
        opMap.put(op, token);
    }

    /**
     * Get token
     *
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * Get operator for token
     *
     * @param token the token
     *
     * @return the operator
     */
    static BooleanOperator forToken(Object token) {
        return tokenMap.get(token.toString().toLowerCase());
    }

    /**
     * Get token for operator
     *
     * @param op the operator
     *
     * @return the token
     */
    static String forOperator(BooleanOperator op) {
        return opMap.get(op);
    }

    /**
     * Write operator representation
     *
     * @return the operator token
     */
    @Override
    public String toString() {
        return token;
    }
}
