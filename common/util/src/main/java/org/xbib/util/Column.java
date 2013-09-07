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
package org.xbib.util;

import java.text.BreakIterator;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static java.lang.System.getProperty;
import static java.text.BreakIterator.DONE;
import static org.xbib.util.Strings.repeat;

public class Column {
    static final Comparator<Column> BY_HEIGHT = new Comparator<Column>() {
        public int compare(Column first, Column second) {
            if (first.height() < second.height()) {
                return -1;
            }
            return first.height() == second.height() ? 0 : 1;
        }
    };

    private final String header;
    private final List<String> data;
    private final int width;
    private int height;

    Column(String header, int width) {
        this.header = header;
        this.width = Math.max(width, header.length());
        data = new LinkedList<String>();
        height = 0;
    }

    int addCells(Object cellCandidate) {
        int originalHeight = height;

        String source = String.valueOf(cellCandidate).trim();
        for (String eachPiece : source.split(getProperty("line.separator"))) {
            processNextEmbeddedLine(eachPiece);
        }

        return height - originalHeight;
    }

    private void processNextEmbeddedLine(String line) {
        BreakIterator words = BreakIterator.getLineInstance(Locale.US);
        words.setText(line);

        StringBuilder nextCell = new StringBuilder();

        int start = words.first();
        for (int end = words.next(); end != DONE; start = end, end = words.next()) {
            nextCell = processNextWord(line, nextCell, start, end);
        }

        if (nextCell.length() > 0) {
            addCell(nextCell.toString());
        }
    }

    private StringBuilder processNextWord(String source, StringBuilder nextCell, int start, int end) {
        StringBuilder augmented = nextCell;

        String word = source.substring(start, end);
        if (augmented.length() + word.length() > width) {
            addCell(augmented.toString());
            augmented = new StringBuilder("  ").append(word);
        } else {
            augmented.append(word);
        }

        return augmented;
    }

    void addCell(String newCell) {
        data.add(newCell);
        ++height;
    }

    void writeHeaderOn(StringBuilder buffer, boolean appendSpace) {
        buffer.append(header).append(repeat(' ', width - header.length()));

        if (appendSpace) {
            buffer.append(' ');
        }
    }

    void writeSeparatorOn(StringBuilder buffer, boolean appendSpace) {
        buffer.append(repeat('-', header.length())).append(repeat(' ', width - header.length()));
        if (appendSpace) {
            buffer.append(' ');
        }
    }

    void writeCellOn(int index, StringBuilder buffer, boolean appendSpace) {
        if (index < data.size()) {
            String item = data.get(index);

            buffer.append(item).append(repeat(' ', width - item.length()));
            if (appendSpace) {
                buffer.append(' ');
            }
        }
    }

    int height() {
        return height;
    }
}
