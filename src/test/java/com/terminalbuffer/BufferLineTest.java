package com.terminalbuffer;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class BufferLineTest {

    @Test
    void newLineShouldHaveCorrectWidth() {
        BufferLine line = new BufferLine(80);
        assertEquals(80, line.getWidth());
    }

    @Test
    void newLineShouldBeFilledWithEmptyCells() {
        BufferLine line = new BufferLine(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(Cell.EMPTY, line.getCell(i));
            assertTrue(line.getCell(i).isEmpty());
        }
    }

    @Test
    void shouldRejectZeroWidth() {
        assertThrows(IllegalArgumentException.class, () -> new BufferLine(0));
    }

    @Test
    void shouldRejectNegativeWidth() {
        assertThrows(IllegalArgumentException.class, () -> new BufferLine(-1));
    }

    // --- setCell / getCell ---

    @Test
    void setCellAndGetCellShouldRoundTrip() {
        BufferLine line = new BufferLine(10);
        Cell cell = new Cell('A', CellAttributes.DEFAULT);
        line.setCell(3, cell);
        assertEquals(cell, line.getCell(3));
    }

    @Test
    void setCellShouldNotAffectOtherCells() {
        BufferLine line = new BufferLine(5);
        line.setCell(2, new Cell('X', CellAttributes.DEFAULT));
        assertEquals(Cell.EMPTY, line.getCell(0));
        assertEquals(Cell.EMPTY, line.getCell(1));
        assertEquals(Cell.EMPTY, line.getCell(3));
        assertEquals(Cell.EMPTY, line.getCell(4));
    }

    @Test
    void getCellShouldRejectNegativeIndex() {
        BufferLine line = new BufferLine(10);
        assertThrows(IndexOutOfBoundsException.class, () -> line.getCell(-1));
    }

    @Test
    void getCellShouldRejectIndexAtWidth() {
        BufferLine line = new BufferLine(10);
        assertThrows(IndexOutOfBoundsException.class, () -> line.getCell(10));
    }

    @Test
    void setCellShouldRejectNullCell() {
        BufferLine line = new BufferLine(10);
        assertThrows(NullPointerException.class, () -> line.setCell(0, null));
    }

    // --- writeString ---

    @Test
    void writeStringShouldWriteCharactersWithAttributes() {
        BufferLine line = new BufferLine(10);
        CellAttributes attrs = new CellAttributes(Color.RED, Color.DEFAULT, EnumSet.noneOf(Style.class));
        int written = line.writeString(0, "Hello", attrs);

        assertEquals(5, written);
        assertEquals('H', line.getCell(0).character());
        assertEquals('e', line.getCell(1).character());
        assertEquals('l', line.getCell(2).character());
        assertEquals('l', line.getCell(3).character());
        assertEquals('o', line.getCell(4).character());
        assertEquals(attrs, line.getCell(0).attributes());
    }

    @Test
    void writeStringShouldStartAtGivenColumn() {
        BufferLine line = new BufferLine(10);
        line.writeString(5, "AB", CellAttributes.DEFAULT);
        assertEquals(Cell.EMPTY, line.getCell(4));
        assertEquals('A', line.getCell(5).character());
        assertEquals('B', line.getCell(6).character());
        assertEquals(Cell.EMPTY, line.getCell(7));
    }

    @Test
    void writeStringShouldTruncateAtLineWidth() {
        BufferLine line = new BufferLine(5);
        int written = line.writeString(3, "ABCDE", CellAttributes.DEFAULT);

        assertEquals(2, written);
        assertEquals('A', line.getCell(3).character());
        assertEquals('B', line.getCell(4).character());
    }

    @Test
    void writeStringShouldOverwriteExistingContent() {
        BufferLine line = new BufferLine(10);
        line.writeString(0, "AAAAA", CellAttributes.DEFAULT);
        line.writeString(1, "BB", CellAttributes.DEFAULT);
        assertEquals('A', line.getCell(0).character());
        assertEquals('B', line.getCell(1).character());
        assertEquals('B', line.getCell(2).character());
        assertEquals('A', line.getCell(3).character());
    }

    @Test
    void writeEmptyStringShouldWriteNothing() {
        BufferLine line = new BufferLine(10);
        int written = line.writeString(0, "", CellAttributes.DEFAULT);
        assertEquals(0, written);
        assertEquals(Cell.EMPTY, line.getCell(0));
    }

    @Test
    void writeStringShouldRejectNullText() {
        BufferLine line = new BufferLine(10);
        assertThrows(NullPointerException.class, () -> line.writeString(0, null, CellAttributes.DEFAULT));
    }

    @Test
    void writeStringShouldRejectNullAttributes() {
        BufferLine line = new BufferLine(10);
        assertThrows(NullPointerException.class, () -> line.writeString(0, "A", null));
    }

    // --- insertCells ---

    @Test
    void insertCellsShouldShiftContentRight() {
        BufferLine line = new BufferLine(10);
        line.writeString(0, "ABCDE", CellAttributes.DEFAULT);
        line.insertCells(2, 3);

        assertEquals('A', line.getCell(0).character());
        assertEquals('B', line.getCell(1).character());
        // positions 2-4 are now empty (inserted gap)
        assertTrue(line.getCell(2).isEmpty());
        assertTrue(line.getCell(3).isEmpty());
        assertTrue(line.getCell(4).isEmpty());
        // original C, D, E shifted to positions 5, 6, 7
        assertEquals('C', line.getCell(5).character());
        assertEquals('D', line.getCell(6).character());
        assertEquals('E', line.getCell(7).character());
    }

    @Test
    void insertCellsShouldDiscardOverflowingCells() {
        BufferLine line = new BufferLine(5);
        line.writeString(0, "ABCDE", CellAttributes.DEFAULT);
        line.insertCells(1, 2);

        assertEquals('A', line.getCell(0).character());
        assertTrue(line.getCell(1).isEmpty());
        assertTrue(line.getCell(2).isEmpty());
        assertEquals('B', line.getCell(3).character());
        assertEquals('C', line.getCell(4).character());
        // D, E are lost
    }

    @Test
    void insertZeroCellsShouldDoNothing() {
        BufferLine line = new BufferLine(5);
        line.writeString(0, "ABCDE", CellAttributes.DEFAULT);
        line.insertCells(2, 0);
        assertEquals("ABCDE", line.getContentAsString());
    }

    @Test
    void insertCellsShouldRejectNegativeColumn() {
        BufferLine line = new BufferLine(10);
        assertThrows(IndexOutOfBoundsException.class, () -> line.insertCells(-1, 1));
    }

    // --- fill ---

    @Test
    void fillShouldSetAllCells() {
        BufferLine line = new BufferLine(5);
        CellAttributes attrs = new CellAttributes(Color.GREEN, Color.BLACK, EnumSet.of(Style.BOLD));
        line.fill('X', attrs);
        for (int i = 0; i < 5; i++) {
            assertEquals('X', line.getCell(i).character());
            assertEquals(attrs, line.getCell(i).attributes());
        }
    }

    @Test
    void fillWithNullCharShouldFillWithEmpty() {
        BufferLine line = new BufferLine(5);
        line.writeString(0, "ABCDE", CellAttributes.DEFAULT);
        line.fill('\0', CellAttributes.DEFAULT);
        for (int i = 0; i < 5; i++) {
            assertTrue(line.getCell(i).isEmpty());
        }
    }

    @Test
    void fillShouldRejectNullAttributes() {
        BufferLine line = new BufferLine(5);
        assertThrows(NullPointerException.class, () -> line.fill('X', null));
    }

    // --- clear ---

    @Test
    void clearShouldResetAllCellsToEmpty() {
        BufferLine line = new BufferLine(5);
        line.writeString(0, "Hello", CellAttributes.DEFAULT);
        line.clear();
        for (int i = 0; i < 5; i++) {
            assertEquals(Cell.EMPTY, line.getCell(i));
        }
    }

    // --- getContentAsString ---

    @Test
    void getContentAsStringShouldReturnWrittenText() {
        BufferLine line = new BufferLine(10);
        line.writeString(0, "Hello", CellAttributes.DEFAULT);
        assertEquals("Hello     ", line.getContentAsString());
    }

    @Test
    void getContentAsStringShouldReplaceEmptyCellsWithSpaces() {
        BufferLine line = new BufferLine(5);
        assertEquals("     ", line.getContentAsString());
    }

    @Test
    void getContentAsStringShouldHaveLengthEqualToWidth() {
        BufferLine line = new BufferLine(80);
        assertEquals(80, line.getContentAsString().length());
    }

    @Test
    void getContentAsStringShouldReflectPartialWrites() {
        BufferLine line = new BufferLine(10);
        line.writeString(3, "AB", CellAttributes.DEFAULT);
        assertEquals("   AB     ", line.getContentAsString());
    }

    // --- edge cases ---

    @Test
    void widthOneLine() {
        BufferLine line = new BufferLine(1);
        assertEquals(1, line.getWidth());
        line.setCell(0, new Cell('X', CellAttributes.DEFAULT));
        assertEquals('X', line.getCell(0).character());
    }

    @Test
    void writeStringAtLastColumn() {
        BufferLine line = new BufferLine(5);
        int written = line.writeString(4, "XY", CellAttributes.DEFAULT);
        assertEquals(1, written);
        assertEquals('X', line.getCell(4).character());
    }

    @Test
    void insertCellsAtEndOfLine() {
        BufferLine line = new BufferLine(5);
        line.writeString(0, "ABCDE", CellAttributes.DEFAULT);
        line.insertCells(4, 2);
        assertEquals('A', line.getCell(0).character());
        assertEquals('B', line.getCell(1).character());
        assertEquals('C', line.getCell(2).character());
        assertEquals('D', line.getCell(3).character());
        assertTrue(line.getCell(4).isEmpty());
    }

    // --- Wide character support ---

    @Test
    void setCellWideShouldOccupyTwoCells() {
        BufferLine line = new BufferLine(10);
        Cell wide = new Cell('中', CellAttributes.DEFAULT, 2);
        line.setCell(3, wide);

        assertEquals('中', line.getCell(3).character());
        assertEquals(2, line.getCell(3).width());
        assertTrue(line.getCell(4).isContinuation());
    }

    @Test
    void wideCellAtEndOfLineShouldBeReplacedWithEmpty() {
        BufferLine line = new BufferLine(5);
        Cell wide = new Cell('中', CellAttributes.DEFAULT, 2);
        line.setCell(4, wide); // doesn't fit — only 1 cell left
        assertTrue(line.getCell(4).isEmpty());
    }

    @Test
    void overwritingContinuationShouldClearWideChar() {
        BufferLine line = new BufferLine(10);
        Cell wide = new Cell('中', CellAttributes.DEFAULT, 2);
        line.setCell(3, wide);

        // Overwrite the continuation cell at position 4
        line.setCell(4, new Cell('X', CellAttributes.DEFAULT));

        // The left half (position 3) should be cleared
        assertTrue(line.getCell(3).isEmpty());
        assertEquals('X', line.getCell(4).character());
    }

    @Test
    void overwritingWideCharLeftHalfShouldClearContinuation() {
        BufferLine line = new BufferLine(10);
        Cell wide = new Cell('中', CellAttributes.DEFAULT, 2);
        line.setCell(3, wide);

        // Overwrite the left half at position 3
        line.setCell(3, new Cell('Y', CellAttributes.DEFAULT));

        assertEquals('Y', line.getCell(3).character());
        // The continuation at position 4 should be cleared
        assertTrue(line.getCell(4).isEmpty());
    }

    @Test
    void getContentAsStringShouldSkipContinuationCells() {
        BufferLine line = new BufferLine(6);
        line.writeString(0, "A", CellAttributes.DEFAULT);
        Cell wide = new Cell('中', CellAttributes.DEFAULT, 2);
        line.setCell(1, wide);
        line.writeString(3, "B", CellAttributes.DEFAULT);

        String content = line.getContentAsString();
        // A + 中 + B + spaces (2 continuation-free empties)
        assertEquals("A中B  ", content);
    }

    @Test
    void adjacentWideCharsShouldWorkCorrectly() {
        BufferLine line = new BufferLine(6);
        Cell wide1 = new Cell('中', CellAttributes.DEFAULT, 2);
        Cell wide2 = new Cell('文', CellAttributes.DEFAULT, 2);
        line.setCell(0, wide1);
        line.setCell(2, wide2);

        assertEquals('中', line.getCell(0).character());
        assertTrue(line.getCell(1).isContinuation());
        assertEquals('文', line.getCell(2).character());
        assertTrue(line.getCell(3).isContinuation());

        assertEquals("中文  ", line.getContentAsString());
    }

    @Test
    void wideCharOverwritingAnotherWideCharShouldCleanUp() {
        BufferLine line = new BufferLine(6);
        Cell wide1 = new Cell('中', CellAttributes.DEFAULT, 2);
        line.setCell(1, wide1); // occupies cells 1-2

        Cell wide2 = new Cell('文', CellAttributes.DEFAULT, 2);
        line.setCell(2, wide2); // overlaps continuation of first wide char

        // Cell 1 (was left half of wide1) should be cleared
        assertTrue(line.getCell(1).isEmpty());
        assertEquals('文', line.getCell(2).character());
        assertTrue(line.getCell(3).isContinuation());
    }
}

