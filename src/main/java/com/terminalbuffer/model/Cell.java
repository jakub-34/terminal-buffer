package com.terminalbuffer.model;

/**
 * Represents a single character cell in the terminal buffer.
 * Holds a character (or '\0' for empty), its visual attributes,
 * and a display width (1 for normal, 2 for wide, 0 for continuation).
 * Immutable value object.
 */
public record Cell(char character, CellAttributes attributes, int width) {

    /** An empty cell with default attributes and width 1. */
    public static final Cell EMPTY = new Cell('\0', CellAttributes.DEFAULT, 1);

    /** A continuation cell — the right half of a wide character. */
    public static final Cell CONTINUATION = new Cell('\0', CellAttributes.DEFAULT, 0);

    public Cell {
        if (attributes == null) {
            throw new NullPointerException("attributes must not be null");
        }
        if (width < 0 || width > 2) {
            throw new IllegalArgumentException("width must be 0, 1, or 2, got: " + width);
        }
    }

    /**
     * Convenience constructor for normal-width (1) cells.
     */
    public Cell(char character, CellAttributes attributes) {
        this(character, attributes, 1);
    }

    public boolean isEmpty() {
        return character == '\0' && width != 0;
    }

    public boolean isContinuation() {
        return width == 0;
    }

    public boolean isWide() {
        return width == 2;
    }
}
