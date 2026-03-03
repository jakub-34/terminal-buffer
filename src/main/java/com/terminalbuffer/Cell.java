package com.terminalbuffer;

/**
 * Represents a single character cell in the terminal buffer.
 * Holds a character (or '\0' for empty) and its visual attributes.
 * Immutable value object.
 */
public record Cell(char character, CellAttributes attributes) {

    public static final Cell EMPTY = new Cell('\0', CellAttributes.DEFAULT);

    public Cell {
        if (attributes == null) {
            throw new NullPointerException("attributes must not be null");
        }
    }

    public boolean isEmpty() {
        return character == '\0';
    }
}

