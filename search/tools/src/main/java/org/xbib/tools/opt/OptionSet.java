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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.xbib.util.Objects.ensureNotNull;

/**
 * Representation of a group of detected command line options, their arguments, and non-option arguments.
 *
 */
public class OptionSet {
    private final List<OptionSpec<?>> detectedSpecs;
    private final Map<String, AbstractOptionSpec<?>> detectedOptions;
    private final Map<AbstractOptionSpec<?>, List<String>> optionsToArguments;
    private final List<String> nonOptionArguments;
    private final Map<String, List<?>> defaultValues;

    /*
     * Package-private because clients don't create these.
     */
    OptionSet(Map<String, List<?>> defaults) {
        detectedSpecs = new ArrayList<OptionSpec<?>>();
        detectedOptions = new HashMap<String, AbstractOptionSpec<?>>();
        optionsToArguments = new IdentityHashMap<AbstractOptionSpec<?>, List<String>>();
        nonOptionArguments = new ArrayList<String>();
        defaultValues = new HashMap<String, List<?>>(defaults);
    }

    /**
     * Tells whether any options were detected.
     *
     * @return {@code true} if any options were detected
     */
    public boolean hasOptions() {
        return !detectedOptions.isEmpty();
    }

    /**
     * Tells whether the given option was detected.
     *
     * @param option the option to search for
     * @return {@code true} if the option was detected
     * @see #has(OptionSpec)
     */
    public boolean has(String option) {
        return detectedOptions.containsKey(option);
    }

    /**
     * Tells whether the given option was detected.
     * <p/>
     * <p>This method recognizes only instances of options returned from the fluent interface methods.</p>
     * <p/>
     * <p>Specifying a {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[])} default argument value}
     * for an option does not cause this method to return {@code true} if the option was not detected on the command
     * line.</p>
     *
     * @param option the option to search for
     * @return {@code true} if the option was detected
     * @see #has(String)
     */
    public boolean has(OptionSpec<?> option) {
        return optionsToArguments.containsKey(option);
    }

    /**
     * Tells whether there are any arguments associated with the given option.
     *
     * @param option the option to search for
     * @return {@code true} if the option was detected and at least one argument was detected for the option
     * @see #hasArgument(OptionSpec)
     */
    public boolean hasArgument(String option) {
        AbstractOptionSpec<?> spec = detectedOptions.get(option);
        return spec != null && hasArgument(spec);
    }

    /**
     * Tells whether there are any arguments associated with the given option.
     * <p/>
     * <p>This method recognizes only instances of options returned from the fluent interface methods.</p>
     * <p/>
     * <p>Specifying a {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[]) default argument value}
     * for an option does not cause this method to return {@code true} if the option was not detected on the command
     * line, or if the option can take an optional argument but did not have one on the command line.</p>
     *
     * @param option the option to search for
     * @return {@code true} if the option was detected and at least one argument was detected for the option
     * @throws NullPointerException if {@code option} is {@code null}
     * @see #hasArgument(String)
     */
    public boolean hasArgument(OptionSpec<?> option) {
        ensureNotNull(option);

        List<String> values = optionsToArguments.get(option);
        return values != null && !values.isEmpty();
    }

    /**
     * Gives the argument associated with the given option.  If the option was given an argument type, the argument
     * will take on that type; otherwise, it will be a {@link String}.
     * <p/>
     * <p>Specifying a {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[]) default argument value}
     * for an option will cause this method to return that default value even if the option was not detected on the
     * command line, or if the option can take an optional argument but did not have one on the command line.</p>
     *
     * @param option the option to search for
     * @return the argument of the given option; {@code null} if no argument is present, or that option was not
     *         detected
     * @throws NullPointerException if {@code option} is {@code null}
     * @throws OptionException      if more than one argument was detected for the option
     */
    public Object valueOf(String option) {
        ensureNotNull(option);

        AbstractOptionSpec<?> spec = detectedOptions.get(option);
        if (spec == null) {
            List<?> defaults = defaultValuesFor(option);
            return defaults.isEmpty() ? null : defaults.get(0);
        }

        return valueOf(spec);
    }

    /**
     * Gives the argument associated with the given option.
     * <p/>
     * <p>This method recognizes only instances of options returned from the fluent interface methods.</p>
     *
     * @param <V>    represents the type of the arguments the given option accepts
     * @param option the option to search for
     * @return the argument of the given option; {@code null} if no argument is present, or that option was not
     *         detected
     * @throws OptionException      if more than one argument was detected for the option
     * @throws NullPointerException if {@code option} is {@code null}
     * @throws ClassCastException   if the arguments of this option are not of the expected type
     */
    public <V> V valueOf(OptionSpec<V> option) {
        ensureNotNull(option);

        List<V> values = valuesOf(option);
        switch (values.size()) {
            case 0:
                return null;
            case 1:
                return values.get(0);
            default:
                throw new MultipleArgumentsForOptionException(option.options());
        }
    }

    /**
     * <p>Gives any arguments associated with the given option.  If the option was given an argument type, the
     * arguments will take on that type; otherwise, they will be {@link String}s.</p>
     *
     * @param option the option to search for
     * @return the arguments associated with the option, as a list of objects of the type given to the arguments; an
     *         empty list if no such arguments are present, or if the option was not detected
     * @throws NullPointerException if {@code option} is {@code null}
     */
    public List<?> valuesOf(String option) {
        ensureNotNull(option);

        AbstractOptionSpec<?> spec = detectedOptions.get(option);
        return spec == null ? defaultValuesFor(option) : valuesOf(spec);
    }

    /**
     * <p>Gives any arguments associated with the given option.  If the option was given an argument type, the
     * arguments will take on that type; otherwise, they will be {@link String}s.</p>
     * <p/>
     * <p>This method recognizes only instances of options returned from the fluent interface methods.</p>
     *
     * @param <V>    represents the type of the arguments the given option accepts
     * @param option the option to search for
     * @return the arguments associated with the option; an empty list if no such arguments are present, or if the
     *         option was not detected
     * @throws NullPointerException if {@code option} is {@code null}
     * @throws OptionException      if there is a problem converting the option's arguments to the desired type; for
     *                              example, if the type does not implement a correct conversion constructor or method
     */
    public <V> List<V> valuesOf(OptionSpec<V> option) {
        ensureNotNull(option);

        List<String> values = optionsToArguments.get(option);
        if (values == null || values.isEmpty()) {
            return defaultValueFor(option);
        }

        AbstractOptionSpec<V> spec = (AbstractOptionSpec<V>) option;
        List<V> convertedValues = new ArrayList<V>();
        for (String each : values) {
            convertedValues.add(spec.convert(each));
        }

        return unmodifiableList(convertedValues);
    }

    /**
     * Gives the set of options that were detected, in the form of {@linkplain OptionSpec}s, in the order in which the
     * options were found on the command line.
     *
     * @return the set of detected command line options
     */
    public List<OptionSpec<?>> specs() {
        return unmodifiableList(detectedSpecs);
    }

    /**
     * @return the detected non-option arguments
     */
    public List<String> nonOptionArguments() {
        return unmodifiableList(nonOptionArguments);
    }

    void add(AbstractOptionSpec<?> spec) {
        addWithArgument(spec, null);
    }

    void addWithArgument(AbstractOptionSpec<?> spec, String argument) {
        detectedSpecs.add(spec);

        for (String each : spec.options()) {
            detectedOptions.put(each, spec);
        }

        List<String> optionArguments = optionsToArguments.get(spec);

        if (optionArguments == null) {
            optionArguments = new ArrayList<String>();
            optionsToArguments.put(spec, optionArguments);
        }

        if (argument != null) {
            optionArguments.add(argument);
        }
    }

    void addNonOptionArgument(String argument) {
        nonOptionArguments.add(argument);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (that == null || !getClass().equals(that.getClass())) {
            return false;
        }

        OptionSet other = (OptionSet) that;
        Map<AbstractOptionSpec<?>, List<String>> thisOptionsToArguments =
                new HashMap<AbstractOptionSpec<?>, List<String>>(optionsToArguments);
        Map<AbstractOptionSpec<?>, List<String>> otherOptionsToArguments =
                new HashMap<AbstractOptionSpec<?>, List<String>>(other.optionsToArguments);
        return detectedOptions.equals(other.detectedOptions)
                && thisOptionsToArguments.equals(otherOptionsToArguments)
                && nonOptionArguments.equals(other.nonOptionArguments());
    }

    @Override
    public int hashCode() {
        Map<AbstractOptionSpec<?>, List<String>> thisOptionsToArguments =
                new HashMap<AbstractOptionSpec<?>, List<String>>(optionsToArguments);
        return detectedOptions.hashCode()
                ^ thisOptionsToArguments.hashCode()
                ^ nonOptionArguments.hashCode();
    }

    private <V> List<V> defaultValuesFor(String option) {
        if (defaultValues.containsKey(option)) {
            return (List<V>) defaultValues.get(option);
        }

        return emptyList();
    }

    private <V> List<V> defaultValueFor(OptionSpec<V> option) {
        return defaultValuesFor(option.options().iterator().next());
    }
}
