package com.terminalbuffer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the attributes of a cell in the terminal buffer.
 * Holds foreground color, background color, and a set of style flags.
 */
public record CellAttributes(Color foreground, Color background, Set<Style> styles) {

    public static final CellAttributes DEFAULT = new CellAttributes(Color.DEFAULT, Color.DEFAULT, EnumSet.noneOf(Style.class));

    public CellAttributes(Color foreground, Color background, Set<Style> styles) {
        Objects.requireNonNull(foreground, "foreground must not be null");
        Objects.requireNonNull(background, "background must not be null");
        Objects.requireNonNull(styles, "styles must not be null");
        this.foreground = foreground;
        this.background = background;
        this.styles = styles.isEmpty()
                ? EnumSet.noneOf(Style.class)
                : EnumSet.copyOf(styles);
    }

    @Override
    public Set<Style> styles() {
        return Collections.unmodifiableSet(styles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CellAttributes(Color foreground1, Color background1, Set<Style> styles1))) return false;
        return foreground == foreground1
                && background == background1
                && styles.equals(styles1);
    }

    @Override
    public String toString() {
        return "CellAttributes{" +
                "fg=" + foreground +
                ", bg=" + background +
                ", styles=" + styles +
                '}';
    }
}

