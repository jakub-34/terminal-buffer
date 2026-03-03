package com.terminalbuffer;

/**
 * Tracks the cursor position within the terminal screen.
 * The cursor is clamped to stay within the given screen bounds.
 */
public class Cursor {

    private int row;
    private int col;
    private final int maxRow;
    private final int maxCol;

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

    public void reset() {
        row = 0;
        col = 0;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}

