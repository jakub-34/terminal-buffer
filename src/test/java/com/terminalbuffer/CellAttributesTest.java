package com.terminalbuffer;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CellAttributesTest {

    @Test
    void defaultAttributesShouldHaveDefaultColorsAndNoStyles() {
        CellAttributes attrs = CellAttributes.DEFAULT;
        assertEquals(Color.DEFAULT, attrs.foreground());
        assertEquals(Color.DEFAULT, attrs.background());
        assertTrue(attrs.styles().isEmpty());
    }

    @Test
    void shouldStoreCustomForegroundAndBackground() {
        CellAttributes attrs = new CellAttributes(Color.RED, Color.BLUE, EnumSet.noneOf(Style.class));
        assertEquals(Color.RED, attrs.foreground());
        assertEquals(Color.BLUE, attrs.background());
    }

    @Test
    void shouldStoreStyles() {
        Set<Style> styles = EnumSet.of(Style.BOLD, Style.UNDERLINE);
        CellAttributes attrs = new CellAttributes(Color.DEFAULT, Color.DEFAULT, styles);
        assertTrue(attrs.styles().contains(Style.BOLD));
        assertTrue(attrs.styles().contains(Style.UNDERLINE));
        assertFalse(attrs.styles().contains(Style.ITALIC));
        assertEquals(2, attrs.styles().size());
    }

    @Test
    void shouldDefensivelyCopyStyles() {
        EnumSet<Style> styles = EnumSet.of(Style.BOLD);
        CellAttributes attrs = new CellAttributes(Color.DEFAULT, Color.DEFAULT, styles);

        // mutate the original set
        styles.add(Style.ITALIC);

        // attributes should not be affected
        assertFalse(attrs.styles().contains(Style.ITALIC));
        assertEquals(1, attrs.styles().size());
    }

    @Test
    void stylesGetterShouldReturnUnmodifiableSet() {
        CellAttributes attrs = new CellAttributes(Color.DEFAULT, Color.DEFAULT, EnumSet.of(Style.BOLD));
        assertThrows(UnsupportedOperationException.class, () -> attrs.styles().add(Style.ITALIC));
    }

    @Test
    void equalAttributesShouldBeEqual() {
        CellAttributes a = new CellAttributes(Color.RED, Color.GREEN, EnumSet.of(Style.BOLD));
        CellAttributes b = new CellAttributes(Color.RED, Color.GREEN, EnumSet.of(Style.BOLD));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentForegroundShouldNotBeEqual() {
        CellAttributes a = new CellAttributes(Color.RED, Color.DEFAULT, EnumSet.noneOf(Style.class));
        CellAttributes b = new CellAttributes(Color.BLUE, Color.DEFAULT, EnumSet.noneOf(Style.class));
        assertNotEquals(a, b);
    }

    @Test
    void differentBackgroundShouldNotBeEqual() {
        CellAttributes a = new CellAttributes(Color.DEFAULT, Color.RED, EnumSet.noneOf(Style.class));
        CellAttributes b = new CellAttributes(Color.DEFAULT, Color.BLUE, EnumSet.noneOf(Style.class));
        assertNotEquals(a, b);
    }

    @Test
    void differentStylesShouldNotBeEqual() {
        CellAttributes a = new CellAttributes(Color.DEFAULT, Color.DEFAULT, EnumSet.of(Style.BOLD));
        CellAttributes b = new CellAttributes(Color.DEFAULT, Color.DEFAULT, EnumSet.of(Style.ITALIC));
        assertNotEquals(a, b);
    }

    @Test
    void shouldNotEqualNull() {
        assertNotEquals(null, CellAttributes.DEFAULT);
    }

    @Test
    void shouldNotEqualDifferentType() {
        assertNotEquals("not attributes", CellAttributes.DEFAULT);
    }

    @Test
    void shouldRejectNullForeground() {
        assertThrows(NullPointerException.class,
                () -> new CellAttributes(null, Color.DEFAULT, EnumSet.noneOf(Style.class)));
    }

    @Test
    void shouldRejectNullBackground() {
        assertThrows(NullPointerException.class,
                () -> new CellAttributes(Color.DEFAULT, null, EnumSet.noneOf(Style.class)));
    }

    @Test
    void shouldRejectNullStyles() {
        assertThrows(NullPointerException.class,
                () -> new CellAttributes(Color.DEFAULT, Color.DEFAULT, null));
    }

    @Test
    void toStringShouldContainAllFields() {
        CellAttributes attrs = new CellAttributes(Color.RED, Color.BLUE, EnumSet.of(Style.BOLD));
        String str = attrs.toString();
        assertTrue(str.contains("RED"));
        assertTrue(str.contains("BLUE"));
        assertTrue(str.contains("BOLD"));
    }
}

