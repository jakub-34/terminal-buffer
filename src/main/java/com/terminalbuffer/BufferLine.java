package com.terminalbuffer;

import java.util.Arrays;

/**
 * Represents a single line in the terminal buffer.
 * A mutable, fixed-width array of {@link Cell} objects.
 */
public class BufferLine {

    private final Cell[] cells;

    public BufferLine(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive, got: " + width);
        }
        cells = new Cell[width];
        Arrays.fill(cells, Cell.EMPTY);
    }

    public int getWidth() {
        return cells.length;
    }

    public Cell getCell(int col) {
        validateColumn(col);
        return cells[col];
    }

    public void setCell(int col, Cell cell) {
        validateColumn(col);
        if (cell == null) {
            throw new NullPointerException("cell must not be null");
        }
        cells[col] = cell;
    }

    /**
     * Writes a string starting at the given column, overwriting existing cells.
     * Characters that would exceed the line width are truncated.
     *
     * @return the number of characters actually written
     */
    public int writeString(int col, String text, CellAttributes attrs) {
        if (text == null) {
            throw new NullPointerException("text must not be null");
        }
        if (attrs == null) {
            throw new NullPointerException("attrs must not be null");
        }
        int written = 0;
        for (int i = 0; i < text.length() && col + i < cells.length; i++) {
            cells[col + i] = new Cell(text.charAt(i), attrs);
            written++;
        }
        return written;
    }

    /**
     * Inserts cells at the given column, shifting existing cells to the right.
     * Cells that are shifted past the line width are discarded.
     */
    public void insertCells(int col, int count) {
        validateColumn(col);
        if (count <= 0) {
            return;
        }
        // Shift right: start from the end to avoid overwriting
        for (int i = cells.length - 1; i >= col + count; i--) {
            cells[i] = cells[i - count];
        }
        // Fill the gap with empty cells
        int fillEnd = Math.min(col + count, cells.length);
        for (int i = col; i < fillEnd; i++) {
            cells[i] = Cell.EMPTY;
        }
    }

    /**
     * Fills the entire line with the given character and attributes.
     */
    public void fill(char c, CellAttributes attrs) {
        if (attrs == null) {
            throw new NullPointerException("attrs must not be null");
        }
        Cell cell = new Cell(c, attrs);
        Arrays.fill(cells, cell);
    }

    /**
     * Clears the entire line (fills with empty cells).
     */
    public void clear() {
        Arrays.fill(cells, Cell.EMPTY);
    }

    /**
     * Returns the line content as a string.
     * Empty cells ({@code '\0'}) are replaced with spaces.
     */
    public String getContentAsString() {
        StringBuilder sb = new StringBuilder(cells.length);
        for (Cell cell : cells) {
            sb.append(cell.isEmpty() ? ' ' : cell.character());
        }
        return sb.toString();
    }

    private void validateColumn(int col) {
        if (col < 0 || col >= cells.length) {
            throw new IndexOutOfBoundsException(
                    "column " + col + " out of bounds for width " + cells.length);
        }
    }
}

