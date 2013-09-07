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

import org.xbib.util.ColumnarData;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.tools.opt.ParserRules.DOUBLE_HYPHEN;
import static org.xbib.tools.opt.ParserRules.HYPHEN;
import static org.xbib.util.Classes.shortNameOf;
import static org.xbib.util.Strings.surround;

class BuiltinHelpFormatter implements HelpFormatter {
    private ColumnarData grid;

    public String format(Map<String, ? extends OptionDescriptor> options) {
        if (options.isEmpty()) {
            return "No options specified";
        }

        grid = new ColumnarData(optionHeader(options), "Description");

        Comparator<OptionDescriptor> comparator =
                new Comparator<OptionDescriptor>() {
                    public int compare(OptionDescriptor first, OptionDescriptor second) {
                        return first.options().iterator().next().compareTo(second.options().iterator().next());
                    }
                };

        Set<OptionDescriptor> sorted = new TreeSet<OptionDescriptor>(comparator);
        sorted.addAll(options.values());

        for (OptionDescriptor each : sorted) {
            addHelpLineFor(each);
        }

        return grid.format();
    }

    private String optionHeader(Map<String, ? extends OptionDescriptor> options) {
        for (OptionDescriptor each : options.values()) {
            if (each.isRequired()) {
                return "Option (* = required)";
            }
        }

        return "Option";
    }

    private void addHelpLineFor(OptionDescriptor descriptor) {
        if (descriptor.acceptsArguments()) {
            if (descriptor.requiresArgument()) {
                addHelpLineWithArgument(descriptor, '<', '>');
            } else {
                addHelpLineWithArgument(descriptor, '[', ']');
            }
        } else {
            addHelpLineFor(descriptor, "");
        }
    }

    void addHelpLineFor(OptionDescriptor descriptor, String additionalInfo) {
        grid.addRow(createOptionDisplay(descriptor) + additionalInfo, createDescriptionDisplay(descriptor));
    }

    private void addHelpLineWithArgument(OptionDescriptor descriptor, char begin, char end) {
        String argDescription = descriptor.argumentDescription();
        String typeIndicator = typeIndicator(descriptor);
        StringBuilder collector = new StringBuilder();

        if (typeIndicator.length() > 0) {
            collector.append(typeIndicator);

            if (argDescription.length() > 0) {
                collector.append(": ").append(argDescription);
            }
        } else if (argDescription.length() > 0) {
            collector.append(argDescription);
        }

        String helpLine = collector.length() == 0
                ? ""
                : ' ' + surround(collector.toString(), begin, end);
        addHelpLineFor(descriptor, helpLine);
    }

    private String createOptionDisplay(OptionDescriptor descriptor) {
        StringBuilder buffer = new StringBuilder(descriptor.isRequired() ? "* " : "");

        for (Iterator<String> iter = descriptor.options().iterator(); iter.hasNext(); ) {
            String option = iter.next();
            buffer.append(option.length() > 1 ? DOUBLE_HYPHEN : HYPHEN);
            buffer.append(option);

            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }

        return buffer.toString();
    }

    private String createDescriptionDisplay(OptionDescriptor descriptor) {
        List<?> defaultValues = descriptor.defaultValues();
        if (defaultValues.isEmpty()) {
            return descriptor.description();
        }

        String defaultValuesDisplay = createDefaultValuesDisplay(defaultValues);
        return descriptor.description() + ' ' + surround("default: " + defaultValuesDisplay, '(', ')');
    }

    private String createDefaultValuesDisplay(List<?> defaultValues) {
        return defaultValues.size() == 1 ? defaultValues.get(0).toString() : defaultValues.toString();
    }

    private static String typeIndicator(OptionDescriptor descriptor) {
        String indicator = descriptor.argumentTypeIndicator();
        return indicator == null || String.class.getName().equals(indicator)
                ? ""
                : shortNameOf(indicator);
    }
}
