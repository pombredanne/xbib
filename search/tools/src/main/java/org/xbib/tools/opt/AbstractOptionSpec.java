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
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableCollection;
import static org.xbib.util.Strings.EMPTY;

/**
 * @param <V> represents the type of the arguments this option accepts
 */
abstract class AbstractOptionSpec<V> implements OptionSpec<V>, OptionDescriptor {
    private final List<String> options = new ArrayList<String>();
    private final String description;

    protected AbstractOptionSpec(String option) {
        this(singletonList(option), EMPTY);
    }

    protected AbstractOptionSpec(Collection<String> options, String description) {
        arrangeOptions(options);

        this.description = description;
    }

    public final Collection<String> options() {
        return unmodifiableCollection(options);
    }

    public final List<V> values(OptionSet detectedOptions) {
        return detectedOptions.valuesOf(this);
    }

    public final V value(OptionSet detectedOptions) {
        return detectedOptions.valueOf(this);
    }

    public String description() {
        return description;
    }

    protected abstract V convert(String argument);

    abstract void handleOption(OptionParser parser, ArgumentList arguments, OptionSet detectedOptions,
                               String detectedArgument);

    private void arrangeOptions(Collection<String> unarranged) {
        if (unarranged.size() == 1) {
            options.addAll(unarranged);
            return;
        }

        List<String> shortOptions = new ArrayList<String>();
        List<String> longOptions = new ArrayList<String>();

        for (String each : unarranged) {
            if (each.length() == 1) {
                shortOptions.add(each);
            } else {
                longOptions.add(each);
            }
        }

        sort(shortOptions);
        sort(longOptions);

        options.addAll(shortOptions);
        options.addAll(longOptions);
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof AbstractOptionSpec<?>)) {
            return false;
        }

        AbstractOptionSpec<?> other = (AbstractOptionSpec<?>) that;
        return options.equals(other.options);
    }

    @Override
    public int hashCode() {
        return options.hashCode();
    }

    @Override
    public String toString() {
        return options.toString();
    }
}
