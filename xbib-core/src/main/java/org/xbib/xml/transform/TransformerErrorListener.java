/* 
 * Carrot2 project.
 *
 * Copyright (C) 2002-2008, Dawid Weiss, Stanisław Osiński.
 * Portions (C) Contributors listed in "carrot2.CONTRIBUTORS" file.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
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
