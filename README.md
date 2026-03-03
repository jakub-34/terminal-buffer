# Terminal Buffer

A terminal text buffer library in Java — the core data structure that terminal emulators use to store and manipulate displayed text.

## Overview

When a shell sends output, the terminal emulator updates a buffer, and the UI renders it. This library implements that buffer: a grid of character cells, each with a character, foreground/background color, and style flags.

### Architecture

```
TerminalBuffer  ←  facade / coordinator
├── Screen      ←  visible grid of BufferLines (width × height)
├── Scrollback  ←  history of lines scrolled off the top
└── Cursor      ←  current write position

BufferLine      ←  fixed-width array of Cells
Cell            ←  character + CellAttributes + display width
CellAttributes  ←  foreground Color + background Color + Set<Style>
```

## Features

- **Configurable dimensions** — width, height, max scrollback size
- **Text attributes** — 17 colors (default + 16 ANSI), bold/italic/underline styles
- **Cursor** — get/set position, move up/down/left/right with boundary clamping
- **Editing** — write (override), write with wrapping, insert (shift right), fill line
- **Screen management** — insert empty line, clear screen, clear all (screen + scrollback)
- **Content access** — get char/attributes at position, get line/screen/all content as string
- **Wide characters** — CJK ideographs occupy 2 cells with automatic continuation cell handling
- **Resize** — change dimensions at runtime, excess lines pushed to scrollback

## Build & Test

```bash
./gradlew build
./gradlew test
```

Requires Java 21+. No external dependencies (JUnit 5 for tests only).

## Usage Example

```java
TerminalBuffer buf = new TerminalBuffer(80, 24);

// Set text attributes
buf.setCurrentAttributes(new CellAttributes(
    Color.GREEN, Color.BLACK, EnumSet.of(Style.BOLD)
));

// Write text (char-by-char with wrapping)
buf.writeString("Hello, terminal!");

// Move cursor and override content
buf.setCursorPosition(5, 0);
buf.write("Line 5 content");

// Insert text (shifts existing content right)
buf.setCursorPosition(5, 5);
buf.insert("NEW");

// Read content back
char ch = buf.getCharAt(0, 0);               // 'H'
String line = buf.getLineAsString(0);         // "Hello, terminal!..."
String screen = buf.getScreenContent();       // all 24 lines joined by \n
String all = buf.getAllContent();              // scrollback + screen

// Resize
buf.resize(120, 40);
```

## Design Decisions

- **SOLID** — `Cursor`, `Screen`, `Scrollback` are separate classes with single responsibilities; `TerminalBuffer` is a thin coordinator
- **Immutable value objects** — `Cell`, `CellAttributes` are Java records; defensive copies protect internal state
- **Cursor clamping** — out-of-bounds cursor positions are clamped (not exceptions), matching real terminal behavior
- **Resize strategy** — truncate/pad (not reflow); excess top lines go to scrollback
- **Wide char detection** — `Character.isIdeographic()` heuristic covers CJK; emoji would need a full East Asian Width table

## Potential Improvements

- Unicode East Asian Width table for accurate wide char detection (emoji, etc.)
- Reflow on resize instead of truncation
- Surrogate pair support for characters outside BMP
- Tab expansion and other control character handling
- Line dirty tracking for efficient UI rendering

