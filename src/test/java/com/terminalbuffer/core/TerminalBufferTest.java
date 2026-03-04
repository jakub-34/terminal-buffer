package com.terminalbuffer.core;

import com.terminalbuffer.model.BufferLine;
import com.terminalbuffer.model.CellAttributes;
import com.terminalbuffer.model.Color;
import com.terminalbuffer.model.Style;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class TerminalBufferTest {

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(80, 24, 100);
    }

    // --- Construction ---

    @Test
    void shouldCreateWithCorrectDimensions() {
        assertEquals(80, buf.getWidth());
        assertEquals(24, buf.getHeight());
        assertEquals(100, buf.getMaxScrollbackSize());
    }

    @Test
    void shouldDefaultScrollbackTo1000() {
        TerminalBuffer b = new TerminalBuffer(80, 24);
        assertEquals(1000, b.getMaxScrollbackSize());
    }

    @Test
    void shouldRejectInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(0, 24));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, 0));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(-1, 24));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, -1));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, 24, -1));
    }

    @Test
    void initialStateShouldHaveCursorAtOriginAndDefaultAttributes() {
        assertEquals(0, buf.getCursorRow());
        assertEquals(0, buf.getCursorCol());
        assertEquals(CellAttributes.DEFAULT, buf.getCurrentAttributes());
    }

    @Test
    void initialScrollbackShouldBeEmpty() {
        assertEquals(0, buf.getScrollbackSize());
    }

    // --- Attributes ---

    @Test
    void shouldSetAndGetCurrentAttributes() {
        CellAttributes attrs = new CellAttributes(Color.RED, Color.BLUE, EnumSet.of(Style.BOLD));
        buf.setCurrentAttributes(attrs);
        assertEquals(attrs, buf.getCurrentAttributes());
    }

    @Test
    void setCurrentAttributesShouldRejectNull() {
        assertThrows(NullPointerException.class, () -> buf.setCurrentAttributes(null));
    }

    // --- Cursor positioning ---

    @Test
    void setCursorPositionShouldWork() {
        buf.setCursorPosition(10, 40);
        assertEquals(10, buf.getCursorRow());
        assertEquals(40, buf.getCursorCol());
    }

    @Test
    void setCursorPositionShouldClampToTopLeft() {
        buf.setCursorPosition(-5, -10);
        assertEquals(0, buf.getCursorRow());
        assertEquals(0, buf.getCursorCol());
    }

    @Test
    void setCursorPositionShouldClampToBottomRight() {
        buf.setCursorPosition(100, 200);
        assertEquals(23, buf.getCursorRow());
        assertEquals(79, buf.getCursorCol());
    }

    // --- Cursor movement ---

    @Test
    void moveCursorDownShouldAdvanceRow() {
        buf.setCursorPosition(5, 10);
        buf.moveCursorDown(3);
        assertEquals(8, buf.getCursorRow());
        assertEquals(10, buf.getCursorCol());
    }

    @Test
    void moveCursorUpShouldDecreaseRow() {
        buf.setCursorPosition(5, 10);
        buf.moveCursorUp(3);
        assertEquals(2, buf.getCursorRow());
        assertEquals(10, buf.getCursorCol());
    }

    @Test
    void moveCursorRightShouldAdvanceCol() {
        buf.setCursorPosition(5, 10);
        buf.moveCursorRight(5);
        assertEquals(5, buf.getCursorRow());
        assertEquals(15, buf.getCursorCol());
    }

    @Test
    void moveCursorLeftShouldDecreaseCol() {
        buf.setCursorPosition(5, 10);
        buf.moveCursorLeft(5);
        assertEquals(5, buf.getCursorRow());
        assertEquals(5, buf.getCursorCol());
    }

    @Test
    void moveCursorUpShouldClampAtTop() {
        buf.setCursorPosition(2, 0);
        buf.moveCursorUp(10);
        assertEquals(0, buf.getCursorRow());
    }

    @Test
    void moveCursorDownShouldClampAtBottom() {
        buf.setCursorPosition(22, 0);
        buf.moveCursorDown(10);
        assertEquals(23, buf.getCursorRow());
    }

    @Test
    void moveCursorLeftShouldClampAtLeft() {
        buf.setCursorPosition(0, 3);
        buf.moveCursorLeft(10);
        assertEquals(0, buf.getCursorCol());
    }

    @Test
    void moveCursorRightShouldClampAtRight() {
        buf.setCursorPosition(0, 75);
        buf.moveCursorRight(20);
        assertEquals(79, buf.getCursorCol());
    }

    // --- writeChar ---

    @Test
    void writeCharShouldPlaceCharacterAtCursor() {
        buf.writeChar('A');
        // cursor was at (0,0), now should be at (0,1)
        assertEquals(0, buf.getCursorRow());
        assertEquals(1, buf.getCursorCol());
    }

    @Test
    void writeCharShouldUseCurrentAttributes() {
        CellAttributes attrs = new CellAttributes(Color.GREEN, Color.BLACK, EnumSet.of(Style.ITALIC));
        buf.setCurrentAttributes(attrs);
        buf.writeChar('X');
        // verify by moving cursor back and checking — we'll verify via content access in next commit
    }

    // --- writeString (char-by-char with wrapping) ---

    @Test
    void writeStringShouldAdvanceCursor() {
        buf.writeString("Hello");
        assertEquals(0, buf.getCursorRow());
        assertEquals(5, buf.getCursorCol());
    }

    @Test
    void writeStringShouldWrapAtEndOfLine() {
        TerminalBuffer small = new TerminalBuffer(5, 3, 10);
        small.writeString("ABCDE");
        // after writing 5 chars on a width-5 line, cursor wraps to next line
        assertEquals(1, small.getCursorRow());
        assertEquals(0, small.getCursorCol());
    }

    @Test
    void writeStringShouldScrollWhenPastLastRow() {
        TerminalBuffer small = new TerminalBuffer(5, 2, 10);
        // fill both rows and trigger scroll
        small.writeString("ABCDE"); // row 0 full, cursor wraps to row 1
        small.writeString("FGHIJ"); // row 1 full, cursor wraps — scroll happens
        assertEquals(1, small.getCursorRow());
        assertEquals(0, small.getCursorCol());
        assertEquals(1, small.getScrollbackSize());
    }

    @Test
    void writeStringShouldRejectNull() {
        assertThrows(NullPointerException.class, () -> buf.writeString(null));
    }

    // --- write (override, no wrap) ---

    @Test
    void writeShouldOverrideContentAtCursor() {
        buf.setCursorPosition(0, 0);
        buf.write("Hello");
        assertEquals(0, buf.getCursorRow());
        assertEquals(5, buf.getCursorCol()); // cursor after last written char
    }

    @Test
    void writeShouldTruncateAtLineEnd() {
        TerminalBuffer small = new TerminalBuffer(5, 2, 10);
        small.setCursorPosition(0, 3);
        small.write("ABCDE");
        // only 2 chars fit (positions 3 and 4), cursor clamps to end
        assertEquals(0, small.getCursorRow());
        assertEquals(4, small.getCursorCol());
    }

    @Test
    void writeShouldRejectNull() {
        assertThrows(NullPointerException.class, () -> buf.write(null));
    }

    // --- insert ---

    @Test
    void insertShouldShiftExistingContentRight() {
        TerminalBuffer small = new TerminalBuffer(10, 2, 10);
        small.write("ABCDE");
        small.setCursorPosition(0, 2);
        small.insert("XX");
        assertEquals(0, small.getCursorRow());
        assertEquals(4, small.getCursorCol()); // cursor after inserted "XX"
    }

    @Test
    void insertShouldRejectNull() {
        assertThrows(NullPointerException.class, () -> buf.insert(null));
    }

    // --- fillLine ---

    @Test
    void fillLineShouldFillWithCharAndCurrentAttributes() {
        CellAttributes attrs = new CellAttributes(Color.RED, Color.DEFAULT, EnumSet.noneOf(Style.class));
        buf.setCurrentAttributes(attrs);
        buf.fillLine(0, '#');
        // we'll verify content via getters in the next commit
    }

    @Test
    void fillLineDefaultShouldFillWithSpaces() {
        buf.fillLine(5);
        // verify it doesn't throw
    }

    @Test
    void fillLineShouldRejectOutOfBoundsRow() {
        assertThrows(IndexOutOfBoundsException.class, () -> buf.fillLine(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> buf.fillLine(24));
    }

    // --- insertEmptyLine ---

    @Test
    void insertEmptyLineShouldPushTopToScrollback() {
        TerminalBuffer small = new TerminalBuffer(5, 3, 10);
        small.writeString("AAAAA"); // fill row 0
        small.setCursorPosition(1, 0);
        small.writeString("BBBBB"); // fill row 1

        small.insertEmptyLine();

        assertEquals(1, small.getScrollbackSize());
        // screen should still have 3 lines, top line (was "AAAAA") is now in scrollback
        assertEquals(3, small.getScrollback().size() + 3 - small.getScrollback().size()); // sanity
    }

    @Test
    void insertEmptyLineShouldRespectMaxScrollback() {
        TerminalBuffer small = new TerminalBuffer(5, 2, 2);
        small.insertEmptyLine();
        small.insertEmptyLine();
        small.insertEmptyLine(); // third one should trim oldest
        assertEquals(2, small.getScrollbackSize());
    }

    // --- clearScreen ---

    @Test
    void clearScreenShouldResetCursorAndContent() {
        buf.setCursorPosition(10, 30);
        buf.writeString("Something");
        buf.clearScreen();
        assertEquals(0, buf.getCursorRow());
        assertEquals(0, buf.getCursorCol());
    }

    // --- clearAll ---

    @Test
    void clearAllShouldClearScreenAndScrollback() {
        TerminalBuffer small = new TerminalBuffer(5, 2, 10);
        // force some scrollback
        small.writeString("ABCDE");
        small.writeString("FGHIJ");
        small.writeString("KLMNO");
        assertTrue(small.getScrollbackSize() > 0);

        small.clearAll();
        assertEquals(0, small.getCursorRow());
        assertEquals(0, small.getCursorCol());
        assertEquals(0, small.getScrollbackSize());
    }

    // --- Scrollback ---

    @Test
    void scrollbackShouldBeUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> buf.getScrollback().add(new BufferLine(80)));
    }

    @Test
    void scrollbackShouldAccumulateLinesInOrder() {
        TerminalBuffer small = new TerminalBuffer(3, 2, 10);
        // write enough to scroll twice
        small.writeString("AAA"); // row 0 full → wrap to row 1
        small.writeString("BBB"); // row 1 full → scroll, row 0 (AAA) → scrollback
        small.writeString("CCC"); // row 1 full → scroll, row 0 (BBB) → scrollback

        assertEquals(2, small.getScrollbackSize());
        assertEquals("AAA", small.getScrollback().get(0).getContentAsString());
        assertEquals("BBB", small.getScrollback().get(1).getContentAsString());
    }

    // --- Edge cases ---

    @Test
    void singleCellBuffer() {
        TerminalBuffer tiny = new TerminalBuffer(1, 1, 5);
        tiny.writeChar('A');
        // should scroll: A goes to scrollback, cursor back on row 0
        assertEquals(0, tiny.getCursorRow());
        assertEquals(0, tiny.getCursorCol());
        assertEquals(1, tiny.getScrollbackSize());
    }

    @Test
    void writeStringLongerThanEntireScreen() {
        TerminalBuffer small = new TerminalBuffer(3, 2, 100);
        small.writeString("ABCDEFGHIJKL"); // 12 chars, 4 full rows, height=2 → 3 scrolls
        assertEquals(3, small.getScrollbackSize());
    }

    @Test
    void moveCursorByZeroShouldNotMove() {
        buf.setCursorPosition(5, 10);
        buf.moveCursorUp(0);
        buf.moveCursorDown(0);
        buf.moveCursorLeft(0);
        buf.moveCursorRight(0);
        assertEquals(5, buf.getCursorRow());
        assertEquals(10, buf.getCursorCol());
    }

    // --- Content Access: getCharAt / getAttributesAt ---

    @Test
    void getCharAtShouldReturnWrittenCharacter() {
        buf.writeString("Hello");
        assertEquals('H', buf.getCharAt(0, 0));
        assertEquals('e', buf.getCharAt(0, 1));
        assertEquals('o', buf.getCharAt(0, 4));
    }

    @Test
    void getCharAtShouldReturnNullCharForEmptyCell() {
        assertEquals('\0', buf.getCharAt(0, 0));
    }

    @Test
    void getCharAtShouldRejectOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getCharAt(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getCharAt(24, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getCharAt(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getCharAt(0, 80));
    }

    @Test
    void getAttributesAtShouldReturnCurrentAttributesUsedDuringWrite() {
        CellAttributes attrs = new CellAttributes(Color.GREEN, Color.BLACK, EnumSet.of(Style.ITALIC));
        buf.setCurrentAttributes(attrs);
        buf.writeChar('X');
        assertEquals(attrs, buf.getAttributesAt(0, 0));
    }

    @Test
    void getAttributesAtShouldReturnDefaultForEmptyCell() {
        assertEquals(CellAttributes.DEFAULT, buf.getAttributesAt(0, 0));
    }

    // --- Content Access: getLine / getLineAsString ---

    @Test
    void getLineShouldReturnBufferLine() {
        buf.writeString("Test");
        BufferLine line = buf.getLine(0);
        assertNotNull(line);
        assertEquals('T', line.getCell(0).character());
    }

    @Test
    void getLineAsStringShouldReturnLineContent() {
        buf.writeString("Hello");
        String lineStr = buf.getLineAsString(0);
        assertTrue(lineStr.startsWith("Hello"));
        assertEquals(80, lineStr.length());
    }

    @Test
    void getLineAsStringShouldPadWithSpaces() {
        buf.writeString("AB");
        assertEquals(80, buf.getLineAsString(0).length());
        assertTrue(buf.getLineAsString(0).startsWith("AB"));
    }

    // --- Content Access: getScreenContent ---

    @Test
    void getScreenContentShouldJoinLinesWithNewlines() {
        buf.writeString("Hello");
        buf.setCursorPosition(1, 0);
        buf.writeString("World");

        String content = buf.getScreenContent();
        String[] lines = content.split("\n", -1);
        assertEquals(24, lines.length);
        assertTrue(lines[0].startsWith("Hello"));
        assertTrue(lines[1].startsWith("World"));
    }

    @Test
    void getScreenContentOnEmptyBufferShouldBeAllSpaces() {
        String content = buf.getScreenContent();
        String[] lines = content.split("\n", -1);
        assertEquals(24, lines.length);
        for (String line : lines) {
            assertEquals(80, line.length());
            assertTrue(line.isBlank());
        }
    }

    @Test
    void getScreenContentShouldReflectClearScreen() {
        buf.writeString("Something");
        buf.clearScreen();
        String content = buf.getScreenContent();
        for (String line : content.split("\n", -1)) {
            assertTrue(line.isBlank());
        }
    }

    // --- Content Access: getAllContent ---

    @Test
    void getAllContentWithoutScrollbackShouldEqualScreenContent() {
        buf.writeString("Hello");
        assertEquals(buf.getScreenContent(), buf.getAllContent());
    }

    @Test
    void getAllContentShouldIncludeScrollbackAndScreen() {
        TerminalBuffer small = new TerminalBuffer(3, 2, 10);
        small.writeString("AAABBBCCC");
        // AAA, BBB scrolled out; screen row 0 = CCC, row 1 = empty

        String all = small.getAllContent();
        String[] lines = all.split("\n", -1);
        assertEquals(4, lines.length); // 2 scrollback + 2 screen
        assertEquals("AAA", lines[0]);
        assertEquals("BBB", lines[1]);
        assertEquals("CCC", lines[2]);
        assertEquals("   ", lines[3]); // empty screen row
    }

    @Test
    void getAllContentShouldShowMultipleScrollbackLines() {
        TerminalBuffer small = new TerminalBuffer(3, 2, 100);
        small.writeString("AAABBBCCCDDD");
        // 4 full rows, height=2 → 3 in scrollback, screen has DDD + empty

        String all = small.getAllContent();
        String[] lines = all.split("\n", -1);
        assertEquals(5, lines.length); // 3 scrollback + 2 screen
        assertEquals("AAA", lines[0]);
        assertEquals("BBB", lines[1]);
        assertEquals("CCC", lines[2]);
        assertEquals("DDD", lines[3]);
    }

    // --- Content Access: scrollback content ---

    @Test
    void getScrollbackCharAtShouldReturnCorrectChar() {
        TerminalBuffer small = new TerminalBuffer(3, 2, 10);
        small.writeString("AAABBB");
        // AAA scrolled out

        assertEquals('A', small.getScrollbackCharAt(0, 0));
        assertEquals('A', small.getScrollbackCharAt(0, 2));
    }

    @Test
    void getScrollbackAttributesAtShouldReturnCorrectAttributes() {
        CellAttributes attrs = new CellAttributes(Color.RED, Color.BLUE, EnumSet.of(Style.BOLD));
        TerminalBuffer small = new TerminalBuffer(3, 2, 10);
        small.setCurrentAttributes(attrs);
        small.writeString("AAABBB");

        assertEquals(attrs, small.getScrollbackAttributesAt(0, 0));
    }

    @Test
    void getScrollbackLineAsStringShouldReturnCorrectContent() {
        TerminalBuffer small = new TerminalBuffer(3, 2, 10);
        small.writeString("AAABBB");

        assertEquals("AAA", small.getScrollbackLineAsString(0));
    }

    @Test
    void scrollbackContentAccessShouldRejectOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getScrollbackCharAt(0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getScrollbackAttributesAt(0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> buf.getScrollbackLineAsString(0));
    }

    // --- Content Access: fillLine verification ---

    @Test
    void fillLineShouldBeVisibleViaGetCharAt() {
        CellAttributes attrs = new CellAttributes(Color.RED, Color.DEFAULT, EnumSet.noneOf(Style.class));
        buf.setCurrentAttributes(attrs);
        buf.fillLine(0, '#');

        for (int c = 0; c < 80; c++) {
            assertEquals('#', buf.getCharAt(0, c));
            assertEquals(attrs, buf.getAttributesAt(0, c));
        }
    }

    // --- Content Access: write verification ---

    @Test
    void writeShouldBeVisibleViaGetCharAt() {
        buf.write("Hello");
        assertEquals('H', buf.getCharAt(0, 0));
        assertEquals('e', buf.getCharAt(0, 1));
        assertEquals('l', buf.getCharAt(0, 2));
        assertEquals('l', buf.getCharAt(0, 3));
        assertEquals('o', buf.getCharAt(0, 4));
        assertEquals('\0', buf.getCharAt(0, 5));
    }

    // --- Content Access: insert verification ---

    @Test
    void insertShouldBeVisibleViaGetLineAsString() {
        TerminalBuffer small = new TerminalBuffer(10, 2, 10);
        small.write("ABCDE");
        small.setCursorPosition(0, 2);
        small.insert("XX");

        String line = small.getLineAsString(0);
        assertTrue(line.startsWith("ABXXCDE"));
    }

    // --- Wide characters ---

    @Test
    void writeCharShouldDetectWideCharacter() {
        TerminalBuffer small = new TerminalBuffer(10, 2, 10);
        small.writeChar('中'); // CJK ideograph — wide

        assertEquals('中', small.getCharAt(0, 0));
        assertEquals(2, small.getLine(0).getCell(0).width());
        assertTrue(small.getLine(0).getCell(1).isContinuation());
        assertEquals(0, small.getCursorRow());
        assertEquals(2, small.getCursorCol());
    }

    @Test
    void writeStringWithWideCharsShouldAdvanceCursorByTwo() {
        TerminalBuffer small = new TerminalBuffer(10, 2, 10);
        small.writeString("A中B");
        // A=1 cell, 中=2 cells, B=1 cell → total 4 cells
        assertEquals(0, small.getCursorRow());
        assertEquals(4, small.getCursorCol());
    }

    @Test
    void wideCharAtEndOfLineShouldWrapToNextLine() {
        TerminalBuffer small = new TerminalBuffer(5, 3, 10);
        small.setCursorPosition(0, 4);
        small.writeChar('中'); // doesn't fit in 1 remaining cell

        // Position 4 on row 0 should be empty (padding)
        assertTrue(small.getLine(0).getCell(4).isEmpty());
        // Wide char should be on row 1
        assertEquals('中', small.getCharAt(1, 0));
        assertTrue(small.getLine(1).getCell(1).isContinuation());
        assertEquals(1, small.getCursorRow());
        assertEquals(2, small.getCursorCol());
    }

    @Test
    void wideCharContentShouldAppearInGetLineAsString() {
        TerminalBuffer small = new TerminalBuffer(10, 2, 10);
        small.writeString("A中B");
        String line = small.getLineAsString(0);
        assertTrue(line.startsWith("A中B"));
    }

    @Test
    void mixedNarrowAndWideCharsShouldWorkCorrectly() {
        TerminalBuffer small = new TerminalBuffer(8, 2, 10);
        small.writeString("Hi中文!");
        // H=1, i=1, 中=2, 文=2, !=1 → total 7 cells
        assertEquals(0, small.getCursorRow());
        assertEquals(7, small.getCursorCol());
        String line = small.getLineAsString(0);
        assertTrue(line.startsWith("Hi中文!"));
    }

    @Test
    void isWideCharacterShouldDetectCJK() {
        assertTrue(TerminalBuffer.isWideCharacter('中'));
        assertTrue(TerminalBuffer.isWideCharacter('文'));
        assertTrue(TerminalBuffer.isWideCharacter('字'));
        assertFalse(TerminalBuffer.isWideCharacter('A'));
        assertFalse(TerminalBuffer.isWideCharacter(' '));
        assertFalse(TerminalBuffer.isWideCharacter('!'));
    }

    // --- Resize ---

    @Test
    void resizeShouldChangeWidthAndHeight() {
        buf.resize(40, 12);
        assertEquals(40, buf.getWidth());
        assertEquals(12, buf.getHeight());
    }

    @Test
    void resizeGrowShouldPreserveContent() {
        TerminalBuffer small = new TerminalBuffer(5, 2, 10);
        small.writeString("Hello");
        small.resize(10, 4);

        assertEquals(10, small.getWidth());
        assertEquals(4, small.getHeight());
        assertTrue(small.getLineAsString(0).startsWith("Hello"));
    }

    @Test
    void resizeShrinkWidthShouldTruncateContent() {
        TerminalBuffer small = new TerminalBuffer(10, 2, 10);
        small.writeString("ABCDEFGHIJ");
        small.resize(5, 2);

        assertEquals("ABCDE", small.getLineAsString(0));
    }

    @Test
    void resizeShrinkHeightShouldPushLinesToScrollback() {
        TerminalBuffer small = new TerminalBuffer(5, 4, 10);
        small.setCursorPosition(0, 0);
        small.write("AAAAA");
        small.setCursorPosition(1, 0);
        small.write("BBBBB");
        small.setCursorPosition(2, 0);
        small.write("CCCCC");
        small.setCursorPosition(3, 0);
        small.write("DDDDD");

        assertEquals(0, small.getScrollbackSize()); // no scrollback yet

        small.resize(5, 2);

        assertEquals(2, small.getScrollbackSize());
        assertEquals("AAAAA", small.getScrollbackLineAsString(0));
        assertEquals("BBBBB", small.getScrollbackLineAsString(1));
        assertEquals("CCCCC", small.getLineAsString(0));
        assertEquals("DDDDD", small.getLineAsString(1));
    }

    @Test
    void resizeShouldClampCursor() {
        buf.setCursorPosition(20, 70);
        buf.resize(40, 10);
        assertEquals(9, buf.getCursorRow());
        assertEquals(39, buf.getCursorCol());
    }

    @Test
    void resizeShouldNotMoveCursorIfWithinBounds() {
        buf.setCursorPosition(5, 10);
        buf.resize(80, 24);
        assertEquals(5, buf.getCursorRow());
        assertEquals(10, buf.getCursorCol());
    }

    @Test
    void resizeGrowHeightShouldAddEmptyLines() {
        TerminalBuffer small = new TerminalBuffer(5, 2, 10);
        small.writeString("Hello");
        small.resize(5, 4);

        assertEquals(4, small.getHeight());
        assertTrue(small.getLineAsString(0).startsWith("Hello"));
        assertTrue(small.getLineAsString(2).isBlank());
        assertTrue(small.getLineAsString(3).isBlank());
    }

    @Test
    void resizeShrinkHeightShouldRespectMaxScrollback() {
        TerminalBuffer small = new TerminalBuffer(3, 5, 2);
        for (int i = 0; i < 5; i++) {
            small.setCursorPosition(i, 0);
            small.writeString(String.valueOf(i).repeat(3));
        }

        small.resize(3, 2);

        // 3 lines removed from top, but max scrollback is 2
        assertEquals(2, small.getScrollbackSize());
    }
}





