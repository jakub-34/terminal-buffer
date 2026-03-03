package com.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScrollbackTest {

    @Test
    void shouldStartEmpty() {
        Scrollback sb = new Scrollback(100);
        assertEquals(0, sb.size());
        assertTrue(sb.getLines().isEmpty());
    }

    @Test
    void shouldRejectNegativeMaxSize() {
        assertThrows(IllegalArgumentException.class, () -> new Scrollback(-1));
    }

    @Test
    void shouldAllowZeroMaxSize() {
        Scrollback sb = new Scrollback(0);
        assertEquals(0, sb.getMaxSize());
        sb.addLine(new BufferLine(5));
        assertEquals(0, sb.size()); // immediately trimmed
    }

    @Test
    void addLineShouldStoreLine() {
        Scrollback sb = new Scrollback(10);
        BufferLine line = new BufferLine(5);
        line.writeString(0, "Hello", CellAttributes.DEFAULT);
        sb.addLine(line);

        assertEquals(1, sb.size());
        assertEquals("Hello", sb.getLine(0).getContentAsString());
    }

    @Test
    void addLineShouldRejectNull() {
        Scrollback sb = new Scrollback(10);
        assertThrows(NullPointerException.class, () -> sb.addLine(null));
    }

    @Test
    void shouldTrimOldestWhenMaxExceeded() {
        Scrollback sb = new Scrollback(2);
        BufferLine a = new BufferLine(3);
        a.writeString(0, "AAA", CellAttributes.DEFAULT);
        BufferLine b = new BufferLine(3);
        b.writeString(0, "BBB", CellAttributes.DEFAULT);
        BufferLine c = new BufferLine(3);
        c.writeString(0, "CCC", CellAttributes.DEFAULT);

        sb.addLine(a);
        sb.addLine(b);
        sb.addLine(c);

        assertEquals(2, sb.size());
        assertEquals("BBB", sb.getLine(0).getContentAsString());
        assertEquals("CCC", sb.getLine(1).getContentAsString());
    }

    @Test
    void clearShouldRemoveAllLines() {
        Scrollback sb = new Scrollback(10);
        sb.addLine(new BufferLine(5));
        sb.addLine(new BufferLine(5));
        sb.clear();
        assertEquals(0, sb.size());
    }

    @Test
    void getLinesShouldBeUnmodifiable() {
        Scrollback sb = new Scrollback(10);
        assertThrows(UnsupportedOperationException.class,
                () -> sb.getLines().add(new BufferLine(5)));
    }

    @Test
    void getLineShouldRejectOutOfBounds() {
        Scrollback sb = new Scrollback(10);
        assertThrows(IndexOutOfBoundsException.class, () -> sb.getLine(0));
        sb.addLine(new BufferLine(5));
        assertThrows(IndexOutOfBoundsException.class, () -> sb.getLine(1));
        assertThrows(IndexOutOfBoundsException.class, () -> sb.getLine(-1));
    }

    @Test
    void getMaxSizeShouldReturnConfiguredValue() {
        assertEquals(100, new Scrollback(100).getMaxSize());
        assertEquals(0, new Scrollback(0).getMaxSize());
    }

    @Test
    void linesPreserveInsertionOrder() {
        Scrollback sb = new Scrollback(10);
        for (int i = 0; i < 5; i++) {
            BufferLine line = new BufferLine(3);
            line.writeString(0, String.valueOf(i), CellAttributes.DEFAULT);
            sb.addLine(line);
        }
        for (int i = 0; i < 5; i++) {
            assertTrue(sb.getLine(i).getContentAsString().startsWith(String.valueOf(i)));
        }
    }
}

