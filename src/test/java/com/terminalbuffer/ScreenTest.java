package com.terminalbuffer;

import org.junit.jupiter.api.Test;

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
}

