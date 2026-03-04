package com.terminalbuffer.core;

import com.terminalbuffer.model.BufferLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores lines that have scrolled off the top of the screen.
 * Maintains a maximum size — oldest lines are discarded when the limit is exceeded.
 */
public class Scrollback {

    private final int maxSize;
    private final List<BufferLine> lines;

    public Scrollback(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize must not be negative, got: " + maxSize);
        }
        this.maxSize = maxSize;
        this.lines = new ArrayList<>();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int size() {
        return lines.size();
    }

    public BufferLine getLine(int index) {
        if (index < 0 || index >= lines.size()) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds for size " + lines.size());
        }
        return lines.get(index);
    }

    /**
     * Adds a line to the end of the scrollback.
     * If the scrollback exceeds the maximum size, the oldest line is removed.
     */
    public void addLine(BufferLine line) {
        if (line == null) {
            throw new NullPointerException("line must not be null");
        }
        lines.add(line);
        trim();
    }

    public void clear() {
        lines.clear();
    }

    public List<BufferLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    private void trim() {
        while (lines.size() > maxSize) {
            lines.removeFirst();
        }
    }
}

