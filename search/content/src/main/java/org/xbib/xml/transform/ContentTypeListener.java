/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2010, Dawid Weiss, Stanisław Osiński.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */
package org.xbib.xml.transform;

import java.io.IOException;

/**
 * A callback listener for providing information about the content type and encoding of
 * the output.
 */
public interface ContentTypeListener {

    void setContentType(String contentType, String encoding) throws IOException;
}
