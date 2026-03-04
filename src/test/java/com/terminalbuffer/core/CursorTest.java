package com.terminalbuffer.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CursorTest {

    @Test
    void shouldStartAtOrigin() {
        Cursor cursor = new Cursor(24, 80);
        assertEquals(0, cursor.getRow());
        assertEquals(0, cursor.getCol());
    }

    @Test
    void shouldRejectInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new Cursor(0, 80));
        assertThrows(IllegalArgumentException.class, () -> new Cursor(24, 0));
        assertThrows(IllegalArgumentException.class, () -> new Cursor(-1, 80));
        assertThrows(IllegalArgumentException.class, () -> new Cursor(24, -1));
    }

    @Test
    void setPositionShouldWork() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(10, 40);
        assertEquals(10, cursor.getRow());
        assertEquals(40, cursor.getCol());
    }

    @Test
    void setPositionShouldClampToTopLeft() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(-5, -10);
        assertEquals(0, cursor.getRow());
        assertEquals(0, cursor.getCol());
    }

    @Test
    void setPositionShouldClampToBottomRight() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(100, 200);
        assertEquals(23, cursor.getRow());
        assertEquals(79, cursor.getCol());
    }

    @Test
    void moveUpShouldDecreaseRow() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(10, 5);
        cursor.moveUp(3);
        assertEquals(7, cursor.getRow());
        assertEquals(5, cursor.getCol());
    }

    @Test
    void moveDownShouldIncreaseRow() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(10, 5);
        cursor.moveDown(3);
        assertEquals(13, cursor.getRow());
        assertEquals(5, cursor.getCol());
    }

    @Test
    void moveLeftShouldDecreaseCol() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(5, 10);
        cursor.moveLeft(4);
        assertEquals(5, cursor.getRow());
        assertEquals(6, cursor.getCol());
    }

    @Test
    void moveRightShouldIncreaseCol() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(5, 10);
        cursor.moveRight(5);
        assertEquals(5, cursor.getRow());
        assertEquals(15, cursor.getCol());
    }

    @Test
    void moveUpShouldClampAtTop() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(2, 0);
        cursor.moveUp(10);
        assertEquals(0, cursor.getRow());
    }

    @Test
    void moveDownShouldClampAtBottom() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(22, 0);
        cursor.moveDown(10);
        assertEquals(23, cursor.getRow());
    }

    @Test
    void moveLeftShouldClampAtLeft() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(0, 3);
        cursor.moveLeft(10);
        assertEquals(0, cursor.getCol());
    }

    @Test
    void moveRightShouldClampAtRight() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(0, 75);
        cursor.moveRight(20);
        assertEquals(79, cursor.getCol());
    }

    // --- advance ---

    @Test
    void advanceShouldMoveRight() {
        Cursor cursor = new Cursor(24, 80);
        boolean scrolled = cursor.advance();
        assertFalse(scrolled);
        assertEquals(0, cursor.getRow());
        assertEquals(1, cursor.getCol());
    }

    @Test
    void advanceShouldWrapToNextLine() {
        Cursor cursor = new Cursor(24, 5);
        cursor.setPosition(0, 4);
        boolean scrolled = cursor.advance();
        assertFalse(scrolled);
        assertEquals(1, cursor.getRow());
        assertEquals(0, cursor.getCol());
    }

    @Test
    void advanceShouldReturnTrueWhenScrollNeeded() {
        Cursor cursor = new Cursor(2, 3);
        cursor.setPosition(1, 2); // last cell
        boolean scrolled = cursor.advance();
        assertTrue(scrolled);
        assertEquals(1, cursor.getRow()); // stays on last row
        assertEquals(0, cursor.getCol());
    }

    // --- reset ---

    @Test
    void resetShouldMoveToOrigin() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(10, 30);
        cursor.reset();
        assertEquals(0, cursor.getRow());
        assertEquals(0, cursor.getCol());
    }

    // --- edge cases ---

    @Test
    void moveByZeroShouldNotChange() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(5, 10);
        cursor.moveUp(0);
        cursor.moveDown(0);
        cursor.moveLeft(0);
        cursor.moveRight(0);
        assertEquals(5, cursor.getRow());
        assertEquals(10, cursor.getCol());
    }

    @Test
    void singleCellCursor() {
        Cursor cursor = new Cursor(1, 1);
        assertEquals(0, cursor.getRow());
        assertEquals(0, cursor.getCol());
        boolean scrolled = cursor.advance();
        assertTrue(scrolled);
        assertEquals(0, cursor.getRow());
        assertEquals(0, cursor.getCol());
    }

    // --- advanceBy ---

    @Test
    void advanceByShouldAdvanceMultiplePositions() {
        Cursor cursor = new Cursor(24, 80);
        int scrolls = cursor.advanceBy(5);
        assertEquals(0, scrolls);
        assertEquals(0, cursor.getRow());
        assertEquals(5, cursor.getCol());
    }

    @Test
    void advanceByShouldWrapAndCountScrolls() {
        Cursor cursor = new Cursor(2, 3);
        cursor.setPosition(1, 1);
        // positions left: (1,1)->(1,2)->(scroll, 1,0)->(1,1)->(1,2)->(scroll, 1,0)
        int scrolls = cursor.advanceBy(4);
        assertEquals(1, scrolls);
        assertEquals(1, cursor.getRow());
        assertEquals(2, cursor.getCol());
    }

    @Test
    void advanceByZeroShouldDoNothing() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(5, 10);
        int scrolls = cursor.advanceBy(0);
        assertEquals(0, scrolls);
        assertEquals(5, cursor.getRow());
        assertEquals(10, cursor.getCol());
    }

    // --- updateBounds ---

    @Test
    void updateBoundsShouldClampCursor() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(20, 70);
        cursor.updateBounds(10, 40);
        assertEquals(9, cursor.getRow());
        assertEquals(39, cursor.getCol());
    }

    @Test
    void updateBoundsShouldNotMoveCursorIfWithinBounds() {
        Cursor cursor = new Cursor(24, 80);
        cursor.setPosition(5, 10);
        cursor.updateBounds(50, 100);
        assertEquals(5, cursor.getRow());
        assertEquals(10, cursor.getCol());
    }

    @Test
    void updateBoundsShouldRejectInvalidValues() {
        Cursor cursor = new Cursor(24, 80);
        assertThrows(IllegalArgumentException.class, () -> cursor.updateBounds(0, 80));
        assertThrows(IllegalArgumentException.class, () -> cursor.updateBounds(24, 0));
    }
}

