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
package org.xbib.xml.transform;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 * An {@link ErrorListener} that reacts to errors when transforming (applying) a
 * stylesheet.
 */
public final class TransformerErrorListener implements ErrorListener {

    private static final Logger logger = LoggerFactory.getLogger(TransformerErrorListener.class.getName());
    /**
     * We store the exception internally as a workaround to xalan, which reports
     * {@link TransformerException} as {@link RuntimeException} (wrapped).
     */
    private TransformerException exception;

    /*
     *
     */
    @Override
    public void warning(TransformerException e) throws TransformerException {
        logger.warn("Warning (recoverable): {}", e.getMessage());
    }

    /*
     *
     */
    @Override
    public void error(TransformerException e) throws TransformerException {
        logger.error("Error (recoverable): {}", e.getMessage());
    }

    /**
     * Unrecoverable errors cause an exception to be rethrown.
     */
    @Override
    public void fatalError(TransformerException e) throws TransformerException {
        logger.error("Fatal error: {0}", e.getMessage());
        this.exception = e;
        throw e;
    }

    public Exception getException() {
        return exception;
    }
}
