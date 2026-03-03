package com.terminalbuffer;

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
}

