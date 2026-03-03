package com.terminalbuffer;

import java.util.List;

/**
 * Core terminal text buffer. Coordinates a {@link Screen}, {@link Scrollback},
 * {@link Cursor}, and current text attributes.
 */
public class TerminalBuffer {

    private final Screen screen;
    private final Scrollback scrollback;
    private final Cursor cursor;
    private CellAttributes currentAttributes;

    public TerminalBuffer(int width, int height) {
        this(width, height, 1000);
    }

    public TerminalBuffer(int width, int height, int maxScrollbackSize) {
        this.screen = new Screen(width, height);
        this.scrollback = new Scrollback(maxScrollbackSize);
        this.cursor = new Cursor(height, width);
        this.currentAttributes = CellAttributes.DEFAULT;
    }

    // --- Dimensions ---

    public int getWidth() {
        return screen.getWidth();
    }

    public int getHeight() {
        return screen.getHeight();
    }

    public int getMaxScrollbackSize() {
        return scrollback.getMaxSize();
    }

    // --- Attributes ---

    public CellAttributes getCurrentAttributes() {
        return currentAttributes;
    }

    public void setCurrentAttributes(CellAttributes attributes) {
        if (attributes == null) {
            throw new NullPointerException("attributes must not be null");
        }
        this.currentAttributes = attributes;
    }

    // --- Cursor ---

    public int getCursorRow() {
        return cursor.getRow();
    }

    public int getCursorCol() {
        return cursor.getCol();
    }

    public void setCursorPosition(int row, int col) {
        cursor.setPosition(row, col);
    }

    public void moveCursorUp(int n) {
        cursor.moveUp(n);
    }

    public void moveCursorDown(int n) {
        cursor.moveDown(n);
    }

    public void moveCursorLeft(int n) {
        cursor.moveLeft(n);
    }

    public void moveCursorRight(int n) {
        cursor.moveRight(n);
    }

    // --- Editing ---

    /**
     * Writes a single character at the cursor position with current attributes.
     * Advances the cursor to the right. Wraps to the next line at end of line.
     * Scrolls the screen if the cursor moves past the last row.
     */
    public void writeChar(char c) {
        screen.getLine(cursor.getRow()).setCell(cursor.getCol(), new Cell(c, currentAttributes));
        if (cursor.advance()) {
            scrollback.addLine(screen.scroll());
        }
    }

    /**
     * Writes a string starting at the cursor position with current attributes.
     * Each character is written via {@link #writeChar(char)}.
     */
    public void writeString(String text) {
        if (text == null) {
            throw new NullPointerException("text must not be null");
        }
        for (int i = 0; i < text.length(); i++) {
            writeChar(text.charAt(i));
        }
    }

    /**
     * Writes a string at the cursor position, overriding the current line content.
     * Uses {@link BufferLine#writeString} directly and advances the cursor.
     */
    public void write(String text) {
        if (text == null) {
            throw new NullPointerException("text must not be null");
        }
        int written = screen.getLine(cursor.getRow()).writeString(cursor.getCol(), text, currentAttributes);
        cursor.setPosition(cursor.getRow(), cursor.getCol() + written);
    }

    /**
     * Inserts text at the cursor position, shifting existing content to the right.
     * Characters pushed past line width are lost. Advances the cursor.
     */
    public void insert(String text) {
        if (text == null) {
            throw new NullPointerException("text must not be null");
        }
        BufferLine line = screen.getLine(cursor.getRow());
        line.insertCells(cursor.getCol(), text.length());
        int written = line.writeString(cursor.getCol(), text, currentAttributes);
        cursor.setPosition(cursor.getRow(), cursor.getCol() + written);
    }

    /**
     * Fills a screen row with the given character using current attributes.
     */
    public void fillLine(int row) {
        fillLine(row, ' ');
    }

    /**
     * Fills a screen row with the given character using current attributes.
     */
    public void fillLine(int row, char c) {
        screen.getLine(row).fill(c, currentAttributes);
    }

    /**
     * Inserts an empty line at the bottom of the screen.
     * The top screen line is pushed into scrollback.
     */
    public void insertEmptyLine() {
        scrollback.addLine(screen.scroll());
    }

    /**
     * Clears the entire screen. Resets cursor to (0, 0).
     */
    public void clearScreen() {
        screen.clear();
        cursor.reset();
    }

    /**
     * Clears the screen and scrollback. Resets cursor to (0, 0).
     */
    public void clearAll() {
        clearScreen();
        scrollback.clear();
    }

    // --- Content Access ---

    /**
     * Returns the character at the given screen position.
     * Empty cells return {@code '\0'}.
     */
    public char getCharAt(int row, int col) {
        return screen.getLine(row).getCell(col).character();
    }

    /**
     * Returns the attributes at the given screen position.
     */
    public CellAttributes getAttributesAt(int row, int col) {
        return screen.getLine(row).getCell(col).attributes();
    }

    /**
     * Returns the screen line at the given row.
     */
    public BufferLine getLine(int row) {
        return screen.getLine(row);
    }

    /**
     * Returns the content of a single screen line as a string.
     */
    public String getLineAsString(int row) {
        return screen.getLine(row).getContentAsString();
    }

    /**
     * Returns the entire screen content as a string, with lines joined by newlines.
     */
    public String getScreenContent() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < screen.getHeight(); r++) {
            if (r > 0) {
                sb.append('\n');
            }
            sb.append(screen.getLine(r).getContentAsString());
        }
        return sb.toString();
    }

    /**
     * Returns the entire content (scrollback + screen) as a string,
     * with lines joined by newlines.
     */
    public String getAllContent() {
        StringBuilder sb = new StringBuilder();
        List<BufferLine> scrollbackLines = scrollback.getLines();
        for (int i = 0; i < scrollbackLines.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(scrollbackLines.get(i).getContentAsString());
        }
        for (int r = 0; r < screen.getHeight(); r++) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append(screen.getLine(r).getContentAsString());
        }
        return sb.toString();
    }

    /**
     * Returns the character at the given position in scrollback.
     *
     * @param index line index within scrollback (0 = oldest)
     * @param col   column position
     */
    public char getScrollbackCharAt(int index, int col) {
        return scrollback.getLine(index).getCell(col).character();
    }

    /**
     * Returns the attributes at the given position in scrollback.
     *
     * @param index line index within scrollback (0 = oldest)
     * @param col   column position
     */
    public CellAttributes getScrollbackAttributesAt(int index, int col) {
        return scrollback.getLine(index).getCell(col).attributes();
    }

    /**
     * Returns a scrollback line as a string.
     *
     * @param index line index within scrollback (0 = oldest)
     */
    public String getScrollbackLineAsString(int index) {
        return scrollback.getLine(index).getContentAsString();
    }

    // --- Scrollback ---

    public List<BufferLine> getScrollback() {
        return scrollback.getLines();
    }

    public int getScrollbackSize() {
        return scrollback.size();
    }
}

