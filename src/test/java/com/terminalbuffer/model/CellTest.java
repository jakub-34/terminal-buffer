package com.terminalbuffer.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    @Test
    void emptyCellShouldHaveNullCharAndDefaultAttributes() {
        Cell cell = Cell.EMPTY;
        assertEquals('\0', cell.character());
        assertEquals(CellAttributes.DEFAULT, cell.attributes());
    }

    @Test
    void emptyCellShouldBeEmpty() {
        assertTrue(Cell.EMPTY.isEmpty());
    }

    @Test
    void cellWithCharacterShouldNotBeEmpty() {
        Cell cell = new Cell('A', CellAttributes.DEFAULT);
        assertFalse(cell.isEmpty());
        assertEquals('A', cell.character());
    }

    @Test
    void cellShouldStoreAttributes() {
        CellAttributes attrs = new CellAttributes(Color.RED, Color.BLUE, EnumSet.of(Style.BOLD));
        Cell cell = new Cell('X', attrs);
        assertEquals('X', cell.character());
        assertEquals(attrs, cell.attributes());
    }

    @Test
    void equalCellsShouldBeEqual() {
        CellAttributes attrs = new CellAttributes(Color.GREEN, Color.DEFAULT, EnumSet.of(Style.ITALIC));
        Cell a = new Cell('Z', attrs);
        Cell b = new Cell('Z', attrs);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void cellsWithDifferentCharactersShouldNotBeEqual() {
        Cell a = new Cell('A', CellAttributes.DEFAULT);
        Cell b = new Cell('B', CellAttributes.DEFAULT);
        assertNotEquals(a, b);
    }

    @Test
    void cellsWithDifferentAttributesShouldNotBeEqual() {
        CellAttributes attrs1 = new CellAttributes(Color.RED, Color.DEFAULT, EnumSet.noneOf(Style.class));
        CellAttributes attrs2 = new CellAttributes(Color.BLUE, Color.DEFAULT, EnumSet.noneOf(Style.class));
        Cell a = new Cell('A', attrs1);
        Cell b = new Cell('A', attrs2);
        assertNotEquals(a, b);
    }

    @Test
    void shouldRejectNullAttributes() {
        assertThrows(NullPointerException.class, () -> new Cell('A', null));
    }

    @Test
    void spaceCharacterShouldNotBeEmpty() {
        Cell cell = new Cell(' ', CellAttributes.DEFAULT);
        assertFalse(cell.isEmpty());
        assertEquals(' ', cell.character());
    }

    @Test
    void toStringShouldContainCharacterAndAttributes() {
        Cell cell = new Cell('H', CellAttributes.DEFAULT);
        String str = cell.toString();
        assertTrue(str.contains("H"));
    }

    // --- Width ---

    @Test
    void defaultWidthShouldBeOne() {
        Cell cell = new Cell('A', CellAttributes.DEFAULT);
        assertEquals(1, cell.width());
        assertFalse(cell.isWide());
        assertFalse(cell.isContinuation());
    }

    @Test
    void emptyCellShouldHaveWidthOne() {
        assertEquals(1, Cell.EMPTY.width());
        assertFalse(Cell.EMPTY.isContinuation());
    }

    @Test
    void wideCellShouldHaveWidthTwo() {
        Cell cell = new Cell('中', CellAttributes.DEFAULT, 2);
        assertEquals(2, cell.width());
        assertTrue(cell.isWide());
        assertFalse(cell.isContinuation());
        assertFalse(cell.isEmpty());
    }

    @Test
    void continuationCellShouldHaveWidthZero() {
        Cell cell = Cell.CONTINUATION;
        assertEquals(0, cell.width());
        assertTrue(cell.isContinuation());
        assertFalse(cell.isWide());
        assertFalse(cell.isEmpty()); // continuation is not "empty" in the normal sense
    }

    @Test
    void shouldRejectInvalidWidth() {
        assertThrows(IllegalArgumentException.class, () -> new Cell('A', CellAttributes.DEFAULT, -1));
        assertThrows(IllegalArgumentException.class, () -> new Cell('A', CellAttributes.DEFAULT, 3));
    }

    @Test
    void cellsWithDifferentWidthsShouldNotBeEqual() {
        Cell normal = new Cell('A', CellAttributes.DEFAULT, 1);
        Cell wide = new Cell('A', CellAttributes.DEFAULT, 2);
        assertNotEquals(normal, wide);
    }
}

