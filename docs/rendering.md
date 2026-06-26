# Rendering

[Back to index](index.md)

A Section draws by mutating cells through a [Canvas](#canvas). The framework writes those
cells into a back buffer, diffs it against the last frame, and emits only changed cells.
Sections never produce escape codes.

## Canvas

`com.tonic.vellum.Canvas` is a drawing surface clipped to a Section's bounds. Coordinates
are local: `(0,0)` is the Section's top-left. Writes outside `[0,width) x [0,height)` are
discarded.

| Method | Description |
|---|---|
| `int width()`, `int height()` | Size of the drawable area. |
| `Rect bounds()` | Local bounds, origin `(0,0)`. |
| `void put(int x, int y, char c)` | Write a character (default style). |
| `void put(int x, int y, char c, Style style)` | Write a styled character. |
| `void putCodePoint(int x, int y, int cp)` | Write a code point (incl. astral), default style. |
| `void putCodePoint(int x, int y, int cp, Style style)` | Write a styled code point. |
| `void put(int x, int y, String text)` | Write a line (default style), truncated at the right edge. |
| `void put(int x, int y, String text, Style style)` | Write a styled line, truncated. |
| `void fill(Rect area, char c, Style style)` | Fill a local rectangle. |
| `void clear()` | Fill the canvas with spaces in the default style. |
| `Canvas clip(Rect sub)` | A further-clipped sub-canvas; cannot draw outside the parent. |

## Rect

`com.tonic.vellum.geom.Rect` is an immutable rectangle in cell coordinates. Carving helpers
are pure: `take*` returns a strip; `split*` returns `{strip, remainder}`.

| Method | Description |
|---|---|
| `int x()`, `y()`, `width()`, `height()` | Components. |
| `int right()`, `int bottom()` | Exclusive edges. |
| `boolean isEmpty()` | True if zero cells. |
| `boolean contains(int px, int py)` | Point test. |
| `Rect inset(int amount)` | Shrink on all sides. |
| `Rect intersect(Rect other)` | Overlap of the two rects; empty when disjoint. |
| `Rect takeTop(int)`, `takeBottom(int)`, `takeLeft(int)`, `takeRight(int)` | A strip. |
| `Rect[] splitTop(int)`, `splitBottom(int)`, `splitLeft(int)`, `splitRight(int)` | `{strip, remainder}`. |

## Point

`com.tonic.vellum.geom.Point` is an immutable `(x, y)` in cell coordinates, returned by
`Section.cursor()` (see [Sections](sections.md)).

| Method | Description |
|---|---|
| `int x()`, `int y()` | Components. |

## Style

`com.tonic.vellum.style.Style` is an immutable styling value. Setters return new instances.

Constants: `NORMAL`, `REVERSE`, `DIM_REVERSE`, `BOLD`, `DIM`, `UNDERLINE`.

| Method | Description |
|---|---|
| `Style fg(Color)` | Foreground color. |
| `Style bg(Color)` | Background color. |
| `Style bold(boolean)` | Bold attribute. |
| `Style reverse(boolean)` | Reverse attribute. |
| `Style dim(boolean)` | Dim attribute. |
| `Style underline(boolean)` | Underline attribute. |
| `Color foreground()`, `background()` | Colors. |
| `boolean isBold()`, `isReverse()`, `isDim()`, `isUnderline()` | Attributes. |

```java
Style ok = Style.NORMAL.fg(Color.GREEN).bold(true);
```

## Color

`com.tonic.vellum.style.Color` is an immutable value: the terminal default, one of the 16
named ANSI colors, a 256-color index, or 24-bit RGB.

```
BLACK RED GREEN YELLOW BLUE MAGENTA CYAN WHITE
BRIGHT_BLACK BRIGHT_RED BRIGHT_GREEN BRIGHT_YELLOW
BRIGHT_BLUE BRIGHT_MAGENTA BRIGHT_CYAN BRIGHT_WHITE
DEFAULT
```

| Factory | Description |
|---|---|
| `Color.ansi256(int index)` | A 256-color palette index (0-255). |
| `Color.rgb(int r, int g, int b)` | 24-bit truecolor (each channel 0-255). |

Terminals without 256-color/truecolor support approximate or ignore these.

## Character width

`com.tonic.vellum.CharWidth.of(int codePoint)` (and `of(char)`) gives the display width:
0 for zero-width combining marks, 2 for East Asian wide/fullwidth glyphs and common emoji,
1 otherwise. `CharWidth.width(CharSequence)` returns the total width of a string. Drawing is
display-width aware: wide glyphs take two cells, zero-width marks are dropped, and truncation
never splits a wide glyph. Astral code points are supported; grapheme clusters (e.g. emoji
ZWJ sequences) are measured per code point, not as one cluster.
