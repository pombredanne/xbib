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
package org.xbib.tools.opt;

import java.util.Collection;
import java.util.List;

/**
 * Describes options that an option parser recognizes.
 * <p/>
 * <p>Instances of this interface are returned by the "fluent interface" methods to allow retrieval of option arguments
 * in a type-safe manner.  Here's an example:</p>
 * <p/>
 * <pre><code>
 *     OptionParser parser = new OptionParser();
 *     <strong>OptionSpec&lt;Integer&gt;</strong> count =
 *         parser.accepts( "count" ).withRequiredArg().ofType( Integer.class );
 *     OptionSet options = parser.parse( "--count", "2" );
 *     assert options.has( count );
 *     int countValue = options.valueOf( count );
 *     assert countValue == count.value( options );
 *     List&lt;Integer&gt; countValues = options.valuesOf( count );
 *     assert countValues.equals( count.values( options ) );
 * </code></pre>
 *
 * @param <V> represents the type of the arguments this option accepts
 */
public interface OptionSpec<V> {
    /**
     * Gives any arguments associated with the given option in the given set of detected options.
     * <p/>
     * <p>Specifying a {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[]) default argument value}
     * for this option will cause this method to return that default value even if this option was not detected on the
     * command line, or if this option can take an optional argument but did not have one on the command line.</p>
     *
     * @param detectedOptions the detected options to search in
     * @return the arguments associated with this option; an empty list if no such arguments are present, or if this
     *         option was not detected
     * @throws OptionException      if there is a problem converting this option's arguments to the desired type; for
     *                              example, if the type does not implement a correct conversion constructor or method
     * @throws NullPointerException if {@code detectedOptions} is {@code null}
     * @see OptionSet#valuesOf(OptionSpec)
     */
    List<V> values(OptionSet detectedOptions);

    /**
     * Gives the argument associated with the given option in the given set of detected options.
     * <p/>
     * <p>Specifying a {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[]) default argument value}
     * for this option will cause this method to return that default value even if this option was not detected on the
     * command line, or if this option can take an optional argument but did not have one on the command line.</p>
     *
     * @param detectedOptions the detected options to search in
     * @return the argument of the this option; {@code null} if no argument is present, or that option was not detected
     * @throws OptionException      if more than one argument was detected for the option
     * @throws NullPointerException if {@code detectedOptions} is {@code null}
     * @throws ClassCastException   if the arguments of this option are not of the expected type
     * @see OptionSet#valueOf(OptionSpec)
     */
    V value(OptionSet detectedOptions);

    /**
     * @return the string representations of this option
     */
    Collection<String> options();
}
