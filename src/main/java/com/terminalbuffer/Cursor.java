package com.terminalbuffer;

/**
 * Tracks the cursor position within the terminal screen.
 * The cursor is clamped to stay within the given screen bounds.
 */
public class Cursor {

    private int row;
    private int col;
    private int maxRow;
    private int maxCol;

    public Cursor(int screenHeight, int screenWidth) {
        if (screenHeight <= 0) {
            throw new IllegalArgumentException("screenHeight must be positive, got: " + screenHeight);
        }
        if (screenWidth <= 0) {
            throw new IllegalArgumentException("screenWidth must be positive, got: " + screenWidth);
        }
        this.maxRow = screenHeight - 1;
        this.maxCol = screenWidth - 1;
        this.row = 0;
        this.col = 0;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPosition(int row, int col) {
        this.row = clamp(row, 0, maxRow);
        this.col = clamp(col, 0, maxCol);
    }

    public void moveUp(int n) {
        setPosition(row - n, col);
    }

    public void moveDown(int n) {
        setPosition(row + n, col);
    }

    public void moveLeft(int n) {
        setPosition(row, col - n);
    }

    public void moveRight(int n) {
        setPosition(row, col + n);
    }

    /**
     * Advances the cursor one position to the right.
     *
     * @return true if the cursor wrapped past the last row (scroll needed),
     *         false otherwise
     */
    public boolean advance() {
        col++;
        if (col > maxCol) {
            col = 0;
            row++;
            if (row > maxRow) {
                row = maxRow;
                return true;
            }
        }
        return false;
    }

    /**
     * Advances the cursor by {@code n} positions to the right.
     *
     * @return the number of scrolls needed (0 or more)
     */
    public int advanceBy(int n) {
        int scrolls = 0;
        for (int i = 0; i < n; i++) {
            if (advance()) {
                scrolls++;
            }
        }
        return scrolls;
    }

    public void reset() {
        row = 0;
        col = 0;
    }

    /**
     * Updates the screen bounds. Clamps the cursor to the new bounds.
     */
    public void updateBounds(int screenHeight, int screenWidth) {
        if (screenHeight <= 0) {
            throw new IllegalArgumentException("screenHeight must be positive, got: " + screenHeight);
        }
        if (screenWidth <= 0) {
            throw new IllegalArgumentException("screenWidth must be positive, got: " + screenWidth);
        }
        this.maxRow = screenHeight - 1;
        this.maxCol = screenWidth - 1;
        this.row = clamp(this.row, 0, this.maxRow);
        this.col = clamp(this.col, 0, this.maxCol);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
