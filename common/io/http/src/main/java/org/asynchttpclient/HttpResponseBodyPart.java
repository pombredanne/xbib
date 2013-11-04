/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.asynchttpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * A callback class used when an HTTP response body is received.
 */
public abstract class HttpResponseBodyPart {

    /**
     * Return length of this part in bytes.
     * 
     * @since 2.0.0
     */
    public abstract int length();
    
    /**
     * Return the response body's part bytes received.
     *
     * @return the response body's part bytes received.
     */
    public abstract byte[] getBodyPartBytes();

    /**
     * Method for accessing contents of this part via stream.
     * 
     * @since 2.0.0
     */
    public abstract InputStream readBodyPartBytes();
    
    /**
     * Write the available bytes to the {@link java.io.OutputStream}
     *
     * @param outputStream
     * @return The number of bytes written
     * @throws java.io.IOException
     */
    public abstract int writeTo(OutputStream outputStream) throws IOException;

    /**
     * Return a {@link java.nio.ByteBuffer} that wraps the actual bytes read from the response's chunk. The {@link java.nio.ByteBuffer}
     * capacity is equal to the number of bytes available.
     *
     * @return {@link java.nio.ByteBuffer}
     */
    public abstract ByteBuffer getBodyByteBuffer();

    /**
     * Return true if this is the last part.
     *
     * @return true if this is the last part.
     */
    public abstract boolean isLast();

    /**
     * Close the underlying connection once the processing has completed. Invoking that method means the
     * underlying TCP connection will be closed as soon as the processing of the response is completed. That
     * means the underlying connection will never get pooled.
     */
    public abstract void markUnderlyingConnectionAsToBeClosed();

    /**
     * Return true of the underlying connection will be closed once the response has been fully processed.
     *
     * @return true of the underlying connection will be closed once the response has been fully processed.
     */
    public abstract boolean isUnderlyingConnectionToBeClosed();

}
