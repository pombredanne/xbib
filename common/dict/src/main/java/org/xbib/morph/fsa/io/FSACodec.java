package org.xbib.morph.fsa.io;

import org.xbib.morph.fsa.FSA;
import org.xbib.morph.fsa.FSAFlags;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * All FSA serializers to binary formats will implement this interface.
 */
public interface FSACodec {

    public <T extends InputStream> FSA read(T in) throws IOException;

    /**
     * Serialize a finite state automaton to an output stream.
     */
    public <T extends OutputStream> T write(FSA fsa, T os) throws IOException;

    /**
     * Returns the set of flags supported by the serializer (and the output automaton).
     */
    public Set<FSAFlags> getFlags();

    /**
     * Supports built-in filler separator. Only if {@link #getFlags()} returns
     * {@link FSAFlags#SEPARATORS}.
     */
    public FSACodec withFiller(byte filler);

    /**
     * Supports built-in annotation separator. Only if {@link #getFlags()} returns
     * {@link FSAFlags#SEPARATORS}.
     */
    public FSACodec withAnnotationSeparator(byte annotationSeparator);

    /**
     * Supports built-in right language count on nodes, speeding up perfect hash counts.
     * Only if {@link #getFlags()} returns {@link FSAFlags#NUMBERS}.
     */
    public FSACodec withNumbers();


}
