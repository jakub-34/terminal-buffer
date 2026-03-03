package com.terminalbuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the visible screen area of the terminal — a fixed-height grid of {@link BufferLine}s.
 * Provides line-level operations (get, clear, fill) and scrolling.
 */
public class Screen {

    private int width;
    private int height;
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

    /**
     * Resizes the screen to the given dimensions.
     * <p>
     * Width change: each existing line is copied into a new line of the new width.
     * Characters beyond the new width are truncated; extra space is filled with empty cells.
     * <p>
     * Height change: if shrinking, excess bottom lines are removed (returned for scrollback).
     * If growing, new empty lines are appended at the bottom.
     *
     * @return lines removed from the top when shrinking height (empty list if growing or same)
     */
    public List<BufferLine> resize(int newWidth, int newHeight) {
        if (newWidth <= 0) {
            throw new IllegalArgumentException("newWidth must be positive, got: " + newWidth);
        }
        if (newHeight <= 0) {
            throw new IllegalArgumentException("newHeight must be positive, got: " + newHeight);
        }

        List<BufferLine> removedLines = new ArrayList<>();

        // Resize width of existing lines
        if (newWidth != this.width) {
            for (int i = 0; i < lines.size(); i++) {
                lines.set(i, copyLineToWidth(lines.get(i), newWidth));
            }
        }

        // Adjust height
        if (newHeight < lines.size()) {
            // Shrink: remove excess lines from the top (they go to scrollback)
            int excess = lines.size() - newHeight;
            for (int i = 0; i < excess; i++) {
                removedLines.add(lines.removeFirst());
            }
        } else if (newHeight > lines.size()) {
            // Grow: add empty lines at the bottom
            int extra = newHeight - lines.size();
            for (int i = 0; i < extra; i++) {
                lines.add(new BufferLine(newWidth));
            }
        }

        this.width = newWidth;
        this.height = newHeight;
        return removedLines;
    }

    private static BufferLine copyLineToWidth(BufferLine source, int newWidth) {
        BufferLine newLine = new BufferLine(newWidth);
        int copyWidth = Math.min(source.getWidth(), newWidth);
        for (int c = 0; c < copyWidth; c++) {
            Cell cell = source.getCell(c);
            // If a wide char's continuation would be cut off, replace with empty
            if (cell.isWide() && c + 1 >= newWidth) {
                newLine.setCell(c, Cell.EMPTY);
            } else if (cell.isContinuation() && (c == 0 || !source.getCell(c - 1).isWide())) {
                // Orphaned continuation cell
                newLine.setCell(c, Cell.EMPTY);
            } else {
                newLine.setCell(c, cell);
            }
        }
        return newLine;
    }

    private void validateRow(int row) {
        if (row < 0 || row >= height) {
            throw new IndexOutOfBoundsException("row " + row + " out of bounds for height " + height);
        }
    }
}

