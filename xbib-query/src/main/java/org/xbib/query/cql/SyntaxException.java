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

/**
 * CQL Syntax exception.
 * This exception is a runtime exception because jacc does not provide
 * anything like an exception in yyerror().
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SyntaxException extends RuntimeException {
    /**
     * Creates a new SyntaxException object.
     *
     * @param msg the message for this syntax exception
     */
    public SyntaxException(String msg) {
        super(msg);
    }

    /**
     * Creates a new SyntaxException object.
     *
     * @param t the throwable for this syntax exception
     */
    public SyntaxException(Throwable t) {
        super(t);
    }

    /**
     * Creates a new SyntaxException object.
     *
     * @param msg the message for this syntax exception
     * @param t the throwable for this syntax exception
     */
    public SyntaxException(String msg, Throwable t) {
        super(msg, t);
    }
}
