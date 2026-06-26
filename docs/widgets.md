# Widgets

[Back to index](index.md)

Built-in Sections in `com.tonic.vellum.widget`. All extend [Section](sections.md) and use
only public Section API.

## LabelSection

A single line of text with optional alignment.

```java
LabelSection title = new LabelSection("Vellum").alignment(Alignment.CENTER);
```

| Method | Description |
|---|---|
| `LabelSection setText(String)` | Replace the text. |
| `LabelSection alignment(Alignment)` | `LEFT`, `CENTER`, or `RIGHT`. |
| `LabelSection style(Style)` | Text style. |
| `String text()` | Current text. |

## TextSection

Multiple lines, drawn top-down. Lines split on newline, with optional word wrapping.

| Method | Description |
|---|---|
| `TextSection setText(String)` | Replace the text (split on newline). |
| `TextSection style(Style)` | Text style. |
| `TextSection wrap(boolean)` | Width-aware word wrapping (default off). |

## StatusBar

A single-row bar, reverse-video by default.

| Method | Description |
|---|---|
| `StatusBar setText(String)` | Replace the text. |
| `StatusBar style(Style)` | Bar style. |

## TextInput

A single-line editable field. Supports insert, Backspace, Delete, caret movement
(Left/Right/Home/End), and horizontal scrolling when the text exceeds the width. Enter
fires `onSubmit`; edits fire `onChange`. The framework positions the hardware cursor at the
caret while this field is focused (see [Sections](sections.md) on `cursor()`).

```java
TextInput search = new TextInput()
        .placeholder("search...")
        .onChange(query -> filter(query))
        .onSubmit(query -> run(query));
```

| Method | Description |
|---|---|
| `TextInput setText(String)` | Replace the text. |
| `String text()` | Current text. |
| `TextInput placeholder(String)` | Text shown when empty and unfocused. |
| `TextInput style(Style)` | Text style. |
| `TextInput placeholderStyle(Style)` | Placeholder style. |
| `TextInput onSubmit(Consumer<String>)` | Called with the text on Enter. |
| `TextInput onChange(Consumer<String>)` | Called with the text after every edit. |

Do not pair a printable quit key with a focused `TextInput` (the quit key would be typed).
Use a non-printable quit such as Ctrl-C or Escape (see [App](app.md)).

## MenuSection

A vertical, arrow-navigable list with a selection. Up/Down move the selection; Enter fires
the callback. The selected row is reverse when focused and dimmed-reverse when parked.

```java
MenuSection menu = new MenuSection("Logs", "Metrics", "Config")
        .onSelect(index -> detail.select(index));
```

| Method | Description |
|---|---|
| `MenuSection onSelect(Consumer<Integer>)` | Called with the index on Enter. |
| `int selectedIndex()` | Current selection. |

## ScrollSection

Vertically scrollable lines. Up/Down scroll by one, Page Up/Down by a screen, Home/End jump
to the ends. Optional follow-tail keeps the view pinned to the bottom as lines are added;
scrolling up turns it off, End turns it back on.

| Method | Description |
|---|---|
| `ScrollSection followTail(boolean)` | Pin to the bottom on append. |
| `ScrollSection setLines(List<String>)` | Replace all lines. |
| `ScrollSection style(Style)` | Text style. |
| `ScrollSection wrap(boolean)` | Word-wrap; scrolling then operates over display lines. |
| `int lineCount()` | Number of logical lines. |
| `int scrollTop()` | Index of the first visible line. |

Subclasses append lines with the protected `appendLine(String)`.

## LogSection

A `ScrollSection` that follows the tail by default, with a public append for feeding lines
(typically from a background thread via `App.post`).

```java
app.post(() -> logs.append(line));
```

| Method | Description |
|---|---|
| `LogSection append(String)` | Append a line. UI-thread-only. |

## BorderSection

Wraps a single child, draws a border and optional title, and insets the child. The frame
uses the focused style when the wrapped child is on the focus path, otherwise the
unfocused style.

```java
Section bordered = BorderSection.around(menu, "Menu").focusedStyle(Style.BOLD);
// or, as a Section convenience:
Section same = menu.bordered("Menu");
```

| Method | Description |
|---|---|
| `static BorderSection around(Section)` | Wrap a child. |
| `static BorderSection around(Section, String title)` | Wrap with a title. |
| `BorderSection title(String)` | Set the title. |
| `BorderSection focusedStyle(Style)` | Frame style when the child is focused. |
| `BorderSection unfocusedStyle(Style)` | Frame style otherwise. |

`Section.bordered()` and `Section.bordered(String)` are shorthands that return a Section.

## TabHost

Holds N children, renders one active tab plus a one-row tab bar, and swaps instantly.
Child instances are always retained; switching fires `onUnmount` on the outgoing child and
`onMount` on the incoming one. Left/Right switch tabs when the active content leaves them
unhandled. As a [FocusContainer](focus-and-input.md), the active content is on the focus
path while the tab host is focused, so content receives keys first.

```java
TabHost detail = new TabHost()
        .add("Logs", logs)
        .add("Metrics", metrics)
        .add("Config", config);
menu.onSelect(detail::select);
```

| Method | Description |
|---|---|
| `TabHost add(String title, Section content)` | Add a tab. |
| `TabHost showTabBar(boolean)` | Show or hide the tab bar (default shown). |
| `void select(int index)` | Switch to a tab by index. |
| `void select(String title)` | Switch to a tab by title. |
| `int active()` | Active tab index. |
| `int count()` | Number of tabs. |

## Alignment

`com.tonic.vellum.widget.Alignment`: `LEFT`, `CENTER`, `RIGHT`.

See [Rendering](rendering.md) for `Style` and `Color`.
