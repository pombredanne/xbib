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
 * Describes options that an option parser recognizes, in ways that might be useful to {@linkplain HelpFormatter
 * help screens}.
 *
 */
public interface OptionDescriptor {
    /**
     * A set of options that are mutually synonymous.
     *
     * @return synonymous options
     */
    Collection<String> options();

    /**
     * Description of this option's purpose.
     *
     * @return a description for the option
     */
    String description();

    /**
     * What values will the option take if none are specified on the command line?
     *
     * @return any default values for the option
     */
    List<?> defaultValues();

    /**
     * Is this option {@linkplain ArgumentAcceptingOptionSpec#required() required} on a command line?
     *
     * @return whether the option is required
     */
    boolean isRequired();

    /**
     * Does this option {@linkplain ArgumentAcceptingOptionSpec accept arguments}?
     *
     * @return whether the option accepts arguments
     */
    boolean acceptsArguments();

    /**
     * Does this option {@linkplain OptionSpecBuilder#withRequiredArg() require an argument}?
     *
     * @return whether the option requires an argument
     */
    boolean requiresArgument();

    /**
     * Gives a short {@linkplain ArgumentAcceptingOptionSpec#describedAs(String) description} of the option's argument.
     *
     * @return a description for the option's argument
     */
    String argumentDescription();

    /**
     * Gives an indication of the {@linkplain ArgumentAcceptingOptionSpec#ofType(Class) expected type} of the option's
     * argument.
     *
     * @return a description for the option's argument type
     */
    String argumentTypeIndicator();
}
