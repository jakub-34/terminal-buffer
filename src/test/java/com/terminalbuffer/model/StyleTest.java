package com.terminalbuffer.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StyleTest {

    @Test
    void shouldHaveThreeValues() {
        assertEquals(3, Style.values().length);
    }

    @Test
    void shouldContainBoldItalicUnderline() {
        assertNotNull(Style.BOLD);
        assertNotNull(Style.ITALIC);
        assertNotNull(Style.UNDERLINE);
    }

    @Test
    void valueOfShouldRoundTrip() {
        for (Style style : Style.values()) {
            assertEquals(style, Style.valueOf(style.name()));
        }
    }

    @Test
    void enumSetShouldSupportCombinations() {
        Set<Style> boldItalic = EnumSet.of(Style.BOLD, Style.ITALIC);
        assertTrue(boldItalic.contains(Style.BOLD));
        assertTrue(boldItalic.contains(Style.ITALIC));
        assertFalse(boldItalic.contains(Style.UNDERLINE));
        assertEquals(2, boldItalic.size());
    }

    @Test
    void enumSetAllOfShouldContainAllStyles() {
        Set<Style> all = EnumSet.allOf(Style.class);
        assertEquals(3, all.size());
        assertTrue(all.contains(Style.BOLD));
        assertTrue(all.contains(Style.ITALIC));
        assertTrue(all.contains(Style.UNDERLINE));
    }

    @Test
    void emptyEnumSetShouldHaveNoStyles() {
        Set<Style> none = EnumSet.noneOf(Style.class);
        assertTrue(none.isEmpty());
        assertFalse(none.contains(Style.BOLD));
    }
}

