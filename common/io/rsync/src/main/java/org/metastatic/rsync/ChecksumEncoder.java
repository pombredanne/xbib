
package org.metastatic.rsync;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * The base class of objects that encode (externalize) checksum pairs
 * to byte streams.
 */
public abstract class ChecksumEncoder {

    /**
     * Property prefix for checksum encoders.
     */
    public static final String PROPERTY = "jarsync.checksumEncoder.";

    /**
     * The configuration object.
     */
    protected Configuration config;

    /**
     * The output stream being written to.
     */
    protected OutputStream out;

    public ChecksumEncoder(Configuration config, OutputStream out) {
        this.config = config;
        this.out = out;
    }

    /**
     * Gets an instance of a checksum encoder for the specified
     * encoding.
     *
     * @param encoding The encoding name.
     * @param config   The configuration object.
     * @param out      The output stream.
     * @throws NullPointerException     If any parameter is null.
     * @throws IllegalArgumentException If the specified encoding cannot
     *                                  be found, or if any of the arguments are inappropriate.
     */
    public static ChecksumEncoder getInstance(String encoding,
                                              Configuration config,
                                              OutputStream out) {
        if (encoding == null || config == null || out == null) {
            throw new NullPointerException();
        }
        if (encoding.length() == 0) {
            throw new IllegalArgumentException();
        }
        try {
            Class clazz = Class.forName(System.getProperty(PROPERTY + encoding));
            if (!ChecksumEncoder.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(clazz.getName() +
                        ": not a subclass of " +
                        ChecksumEncoder.class.getName());
            }
            Constructor c = clazz.getConstructor(new Class[]{Configuration.class,
                    OutputStream.class});
            return (ChecksumEncoder) c.newInstance(new Object[]{config, out});
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException("class not found: " +
                    cnfe.getMessage());
        } catch (NoSuchMethodException nsme) {
            throw new IllegalArgumentException("subclass has no constructor");
        } catch (InvocationTargetException ite) {
            throw new IllegalArgumentException(ite.getMessage());
        } catch (InstantiationException ie) {
            throw new IllegalArgumentException(ie.getMessage());
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException(iae.getMessage());
        }
    }

    /**
     * Encodes a list of checksums to the output stream.
     *
     * @param sums The sums to write.
     * @throws java.io.IOException      If an I/O error occurs.
     * @throws NullPointerException     If any element of the list is null.
     * @throws IllegalArgumentException If any element of the list is
     *                                  not a {@link org.metastatic.rsync.ChecksumPair}.
     */
    public void write(List<ChecksumPair> sums) throws IOException {
        for (ChecksumPair sum : sums) {
            write(sum);
        }
    }

    /**
     * Encodes a checksum pair to the output stream.
     *
     * @param pair The pair to write.
     * @throws java.io.IOException If an I/O error occurs.
     */
    public abstract void write(ChecksumPair pair) throws IOException;

}
