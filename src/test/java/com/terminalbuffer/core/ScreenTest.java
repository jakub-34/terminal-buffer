package com.terminalbuffer.core;

import com.terminalbuffer.model.BufferLine;
import com.terminalbuffer.model.Cell;
import com.terminalbuffer.model.CellAttributes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScreenTest {

    @Test
    void shouldCreateWithCorrectDimensions() {
        Screen screen = new Screen(80, 24);
        assertEquals(80, screen.getWidth());
        assertEquals(24, screen.getHeight());
    }

    @Test
    void shouldRejectInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new Screen(0, 24));
        assertThrows(IllegalArgumentException.class, () -> new Screen(80, 0));
        assertThrows(IllegalArgumentException.class, () -> new Screen(-1, 24));
        assertThrows(IllegalArgumentException.class, () -> new Screen(80, -1));
    }

    @Test
    void allLinesShouldBeEmptyInitially() {
        Screen screen = new Screen(5, 3);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 5; c++) {
                assertTrue(screen.getLine(r).getCell(c).isEmpty());
            }
        }
    }

    @Test
    void getLineShouldRejectOutOfBounds() {
        Screen screen = new Screen(5, 3);
        assertThrows(IndexOutOfBoundsException.class, () -> screen.getLine(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> screen.getLine(3));
    }

    @Test
    void scrollShouldRemoveTopLineAndAddEmptyBottom() {
        Screen screen = new Screen(3, 2);
        screen.getLine(0).writeString(0, "AAA", CellAttributes.DEFAULT);
        screen.getLine(1).writeString(0, "BBB", CellAttributes.DEFAULT);

        BufferLine removed = screen.scroll();

        assertEquals("AAA", removed.getContentAsString());
        assertEquals("BBB", screen.getLine(0).getContentAsString());
        assertEquals("   ", screen.getLine(1).getContentAsString());
    }

    @Test
    void clearShouldResetAllLines() {
        Screen screen = new Screen(5, 3);
        screen.getLine(0).writeString(0, "Hello", CellAttributes.DEFAULT);
        screen.getLine(1).writeString(0, "World", CellAttributes.DEFAULT);

        screen.clear();

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 5; c++) {
                assertEquals(Cell.EMPTY, screen.getLine(r).getCell(c));
            }
        }
    }

    @Test
    void multipleScrollsShouldWorkCorrectly() {
        Screen screen = new Screen(2, 2);
        screen.getLine(0).writeString(0, "AA", CellAttributes.DEFAULT);
        screen.getLine(1).writeString(0, "BB", CellAttributes.DEFAULT);

        BufferLine first = screen.scroll();
        assertEquals("AA", first.getContentAsString());

        screen.getLine(1).writeString(0, "CC", CellAttributes.DEFAULT);
        BufferLine second = screen.scroll();
        assertEquals("BB", second.getContentAsString());

        assertEquals("CC", screen.getLine(0).getContentAsString());
        assertEquals("  ", screen.getLine(1).getContentAsString());
    }

    // --- Resize ---

    @Test
    void resizeShouldRejectInvalidDimensions() {
        Screen screen = new Screen(5, 3);
        assertThrows(IllegalArgumentException.class, () -> screen.resize(0, 3));
        assertThrows(IllegalArgumentException.class, () -> screen.resize(5, 0));
    }

    @Test
    void resizeGrowWidthShouldPadWithEmptyCells() {
        Screen screen = new Screen(3, 2);
        screen.getLine(0).writeString(0, "ABC", CellAttributes.DEFAULT);

        screen.resize(5, 2);

        assertEquals(5, screen.getWidth());
        assertEquals("ABC  ", screen.getLine(0).getContentAsString());
    }

    @Test
    void resizeShrinkWidthShouldTruncateContent() {
        Screen screen = new Screen(5, 2);
        screen.getLine(0).writeString(0, "ABCDE", CellAttributes.DEFAULT);

        screen.resize(3, 2);

        assertEquals(3, screen.getWidth());
        assertEquals("ABC", screen.getLine(0).getContentAsString());
    }

    @Test
    void resizeGrowHeightShouldAddEmptyLines() {
        Screen screen = new Screen(3, 2);
        screen.getLine(0).writeString(0, "AAA", CellAttributes.DEFAULT);
        screen.getLine(1).writeString(0, "BBB", CellAttributes.DEFAULT);

        List<BufferLine> removed = screen.resize(3, 4);

        assertTrue(removed.isEmpty());
        assertEquals(4, screen.getHeight());
        assertEquals("AAA", screen.getLine(0).getContentAsString());
        assertEquals("BBB", screen.getLine(1).getContentAsString());
        assertEquals("   ", screen.getLine(2).getContentAsString());
        assertEquals("   ", screen.getLine(3).getContentAsString());
    }

    @Test
    void resizeShrinkHeightShouldReturnRemovedTopLines() {
        Screen screen = new Screen(3, 4);
        screen.getLine(0).writeString(0, "AAA", CellAttributes.DEFAULT);
        screen.getLine(1).writeString(0, "BBB", CellAttributes.DEFAULT);
        screen.getLine(2).writeString(0, "CCC", CellAttributes.DEFAULT);
        screen.getLine(3).writeString(0, "DDD", CellAttributes.DEFAULT);

        List<BufferLine> removed = screen.resize(3, 2);

        assertEquals(2, removed.size());
        assertEquals("AAA", removed.get(0).getContentAsString());
        assertEquals("BBB", removed.get(1).getContentAsString());
        assertEquals(2, screen.getHeight());
        assertEquals("CCC", screen.getLine(0).getContentAsString());
        assertEquals("DDD", screen.getLine(1).getContentAsString());
    }

    @Test
    void resizeSameWidthAndHeightShouldDoNothing() {
        Screen screen = new Screen(5, 3);
        screen.getLine(0).writeString(0, "Hello", CellAttributes.DEFAULT);

        List<BufferLine> removed = screen.resize(5, 3);

        assertTrue(removed.isEmpty());
        assertEquals("Hello", screen.getLine(0).getContentAsString());
    }

    @Test
    void resizeShouldHandleWideCharAtTruncationBoundary() {
        Screen screen = new Screen(6, 1);
        Cell wide = new Cell('中', CellAttributes.DEFAULT, 2);
        screen.getLine(0).setCell(4, wide); // occupies cols 4-5

        // Shrink to width 5 — wide char at col 4 would lose its continuation
        screen.resize(5, 1);

        // The wide char that no longer fits should be replaced with empty
        assertTrue(screen.getLine(0).getCell(4).isEmpty());
    }
}

