package com.terminalbuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the visible screen area of the terminal — a fixed-height grid of {@link BufferLine}s.
 * Provides line-level operations (get, clear, fill) and scrolling.
 */
public class Screen {

    private final int width;
    private final int height;
    private final List<BufferLine> lines;

    public Screen(int width, int height) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive, got: " + width);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive, got: " + height);
        }
        this.width = width;
        this.height = height;
        this.lines = new ArrayList<>(height);
        for (int i = 0; i < height; i++) {
            lines.add(new BufferLine(width));
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BufferLine getLine(int row) {
        validateRow(row);
        return lines.get(row);
    }

    /**
     * Removes the top line from the screen and appends a new empty line at the bottom.
     *
     * @return the line that was removed from the top (to be pushed into scrollback)
     */
    public BufferLine scroll() {
        BufferLine removed = lines.removeFirst();
        lines.add(new BufferLine(width));
        return removed;
    }

    /**
     * Clears all lines on the screen.
     */
    public void clear() {
        for (BufferLine line : lines) {
            line.clear();
        }
    }

    private void validateRow(int row) {
        if (row < 0 || row >= height) {
            throw new IndexOutOfBoundsException("row " + row + " out of bounds for height " + height);
        }
    }
}

