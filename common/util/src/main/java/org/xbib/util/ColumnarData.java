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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.System.getProperty;
import static java.util.Collections.max;

import static org.xbib.util.Column.BY_HEIGHT;
import static org.xbib.util.Strings.EMPTY;

/**
 * <p>A means to display data in a text grid.</p>
 *
 */
public class ColumnarData {
    private static final String LINE_SEPARATOR = getProperty("line.separator");
    private static final int TOTAL_WIDTH = 80;

    private final ColumnWidthCalculator widthCalculator;
    private final List<Column> columns;
    private final String[] headers;

    /**
     * Creates a new grid with the given column headers.
     *
     * @param headers column headers
     */
    public ColumnarData(String... headers) {
        this.headers = headers.clone();
        widthCalculator = new ColumnWidthCalculator();
        columns = new LinkedList<Column>();

        clear();
    }

    /**
     * Adds a row to the grid.  The data will fall under the corresponding headers.
     * There can be fewer elements in the row than headers.  Any data in columns outside
     * of the number of headers will not be added to the grid.
     *
     * @param rowData row data to add
     */
    public void addRow(Object... rowData) {
        int[] numberOfCellsAddedAt = addRowCells(rowData);
        addPaddingCells(numberOfCellsAddedAt);
    }

    /**
     * Gives a string that represents the data formatted in columns.
     *
     * @return the formatted grid
     */
    public String format() {
        StringBuilder buffer = new StringBuilder();

        writeHeadersOn(buffer);
        writeSeparatorsOn(buffer);
        writeRowsOn(buffer);

        return buffer.toString();
    }

    /**
     * Removes all data from the grid, but preserves the headers.
     */
    public final void clear() {
        columns.clear();

        int desiredColumnWidth = widthCalculator.calculate(TOTAL_WIDTH, headers.length);
        for (String each : headers) {
            columns.add(new Column(each, desiredColumnWidth));
        }
    }

    private void writeHeadersOn(StringBuilder buffer) {
        for (Iterator<Column> iter = columns.iterator(); iter.hasNext(); ) {
            iter.next().writeHeaderOn(buffer, iter.hasNext());
        }

        buffer.append(LINE_SEPARATOR);
    }

    private void writeSeparatorsOn(StringBuilder buffer) {
        for (Iterator<Column> iter = columns.iterator(); iter.hasNext(); ) {
            iter.next().writeSeparatorOn(buffer, iter.hasNext());
        }

        buffer.append(LINE_SEPARATOR);
    }

    private void writeRowsOn(StringBuilder buffer) {
        int maxHeight = max(columns, BY_HEIGHT).height();

        for (int i = 0; i < maxHeight; ++i) {
            writeRowOn(buffer, i);
        }
    }

    private void writeRowOn(StringBuilder buffer, int rowIndex) {
        for (Iterator<Column> iter = columns.iterator(); iter.hasNext(); ) {
            iter.next().writeCellOn(rowIndex, buffer, iter.hasNext());
        }

        buffer.append(LINE_SEPARATOR);
    }

    private int arrayMax(int[] numbers) {
        int maximum = MIN_VALUE;

        for (int each : numbers) {
            maximum = Math.max(maximum, each);
        }

        return maximum;
    }

    private int[] addRowCells(Object... rowData) {
        int[] cellsAddedAt = new int[rowData.length];

        Iterator<Column> iter = columns.iterator();
        for (int i = 0; iter.hasNext() && i < rowData.length; ++i) {
            cellsAddedAt[i] = iter.next().addCells(rowData[i]);
        }

        return cellsAddedAt;
    }

    private void addPaddingCells(int... numberOfCellsAddedAt) {
        int maxHeight = arrayMax(numberOfCellsAddedAt);

        Iterator<Column> iter = columns.iterator();
        for (int i = 0; iter.hasNext() && i < numberOfCellsAddedAt.length; ++i) {
            addPaddingCellsForColumn(iter.next(), maxHeight, numberOfCellsAddedAt[i]);
        }
    }

    private void addPaddingCellsForColumn(Column column, int maxHeight, int numberOfCellsAdded) {
        for (int i = 0; i < maxHeight - numberOfCellsAdded; ++i) {
            column.addCell(EMPTY);
        }
    }
}
