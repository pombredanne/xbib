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
package org.xbib.query;

/**
 * Query exception
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class UnterminatedQuotedStringException extends RuntimeException {
    /**
     * Creates a new QueryException object.
     */
    public UnterminatedQuotedStringException(String msg) {
        super(msg);
    }

    /**
     * Creates a new QueryException object.
     */
    public UnterminatedQuotedStringException(Throwable t) {
        super(t);
    }

    /**
     * Creates a new QueryException object.
     */
    public UnterminatedQuotedStringException(String msg, Throwable t) {
        super(msg, t);
    }
}
