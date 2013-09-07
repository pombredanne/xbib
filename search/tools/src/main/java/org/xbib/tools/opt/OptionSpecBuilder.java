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

/**
 * Allows callers to specify whether a given option accepts arguments (required or optional).
 * <p/>
 * <p>Instances are returned from {@link OptionParser#accepts(String)} to allow the formation of parser directives as
 * sentences in a "fluent interface" language.  For example:</p>
 * <p/>
 * <pre><code>
 *   OptionParser parser = new OptionParser();
 *   parser.accepts( "c" ).<strong>withRequiredArg()</strong>.ofType( Integer.class );
 * </code></pre>
 * <p/>
 * <p>If no methods are invoked on an instance of this class, then that instance's option will accept no argument.</p>
 * <p/>
 * <p>Note that you should not use the fluent interface clauses in a way that would defeat the typing of option
 * arguments:</p>
 * <p/>
 * <pre><code>
 *   OptionParser parser = new OptionParser();
 *   ArgumentAcceptingOptionSpec&lt;String&gt; optionC =
 *       parser.accepts( "c" ).withRequiredArg();
 *   <strong>optionC.ofType( Integer.class );  // DON'T THROW AWAY THE TYPE!</strong>
 * <p/>
 *   String value = parser.parse( "-c", "2" ).valueOf( optionC );  // ClassCastException
 * </code></pre>
 *
 */
public class OptionSpecBuilder extends NoArgumentOptionSpec {
    private final OptionParser parser;

    OptionSpecBuilder(OptionParser parser, Collection<String> options, String description) {
        super(options, description);

        this.parser = parser;
        attachToParser();
    }

    private void attachToParser() {
        parser.recognize(this);
    }

    /**
     * Informs an option parser that this builder's option requires an argument.
     *
     * @return a specification for the option
     */
    public ArgumentAcceptingOptionSpec<String> withRequiredArg() {
        ArgumentAcceptingOptionSpec<String> newSpec =
                new RequiredArgumentOptionSpec<String>(options(), description());
        parser.recognize(newSpec);

        return newSpec;
    }

    /**
     * Informs an option parser that this builder's option accepts an optional argument.
     *
     * @return a specification for the option
     */
    public ArgumentAcceptingOptionSpec<String> withOptionalArg() {
        ArgumentAcceptingOptionSpec<String> newSpec =
                new OptionalArgumentOptionSpec<String>(options(), description());
        parser.recognize(newSpec);

        return newSpec;
    }
}
