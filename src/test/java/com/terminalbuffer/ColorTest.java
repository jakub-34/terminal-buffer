package com.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorTest {

    @Test
    void shouldHaveSeventeenValues() {
        assertEquals(17, Color.values().length);
    }

    @Test
    void shouldIncludeDefault() {
        assertNotNull(Color.DEFAULT);
        assertEquals(Color.DEFAULT, Color.valueOf("DEFAULT"));
    }

    @Test
    void shouldIncludeAllStandardColors() {
        String[] expected = {
                "BLACK", "RED", "GREEN", "YELLOW",
                "BLUE", "MAGENTA", "CYAN", "WHITE"
        };
        for (String name : expected) {
            assertNotNull(Color.valueOf(name), "Missing color: " + name);
        }
    }

    @Test
    void shouldIncludeAllBrightColors() {
        String[] expected = {
                "BRIGHT_BLACK", "BRIGHT_RED", "BRIGHT_GREEN", "BRIGHT_YELLOW",
                "BRIGHT_BLUE", "BRIGHT_MAGENTA", "BRIGHT_CYAN", "BRIGHT_WHITE"
        };
        for (String name : expected) {
            assertNotNull(Color.valueOf(name), "Missing bright color: " + name);
        }
    }

    @Test
    void valueOfShouldRoundTrip() {
        for (Color color : Color.values()) {
            assertEquals(color, Color.valueOf(color.name()));
        }
    }

    @Test
    void defaultShouldBeFirstValue() {
        assertEquals(Color.DEFAULT, Color.values()[0]);
    }
}

