# Widgets

[Back to index](index.md)

Built-in Sections in `com.tonic.vellum.widget`. All extend [Section](sections.md) and use
only public Section API. Two public abstract bases are available to extend:
`AbstractListSection` (scrollable selection lists) and `SingleRowSection` (a single
vertically-centered row).

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

A vertical, arrow-navigable list of strings with a selection. Up/Down move the selection
(scrolling when there are more items than rows), Page/Home/End jump, Enter fires the
callback. The selected row gets a full-row highlight: reverse when focused, dim-reverse when
parked. Built on [AbstractListSection](#list-sections).

```java
MenuSection menu = new MenuSection("Logs", "Metrics", "Config")
        .onSelect(index -> detail.select(index));
```

| Method | Description |
|---|---|
| `MenuSection onSelect(IntConsumer)` | Called with the index on Enter. |
| `int selectedIndex()` | Current selection. |
| `void select(int)` | Move the selection. |

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

## List sections

`AbstractListSection` (public abstract) is the scroll/selection engine behind the list
widgets: it scrolls to keep the selection visible, draws the full-row highlight, and handles
Up/Down/Page/Home/End and Enter, with an optional fixed header. Subclass it with `rowCount()`
and `renderRow(Canvas, int, Style)` to build a custom list.

### SelectList

A scrollable, selectable list of typed items.

```java
SelectList<User> list = new SelectList<>(users)
        .renderer(User::name)
        .onSelectItem(user -> open(user));
```

| Method | Description |
|---|---|
| `SelectList<T> setItems(List<T>)` | Replace the items. |
| `SelectList<T> renderer(Function<T,String>)` | Item-to-row text (default `String.valueOf`). |
| `T selectedItem()` | Selected item, or null. |
| `SelectList<T> onSelect(IntConsumer)` | Index callback on Enter. |
| `SelectList<T> onSelectItem(Consumer<T>)` | Item callback on Enter. |

### Table

A scrollable table with a fixed header and selectable rows. Column widths are computed from
each column's [Constraint](layout.md), so the header and rows align and tile the width.

```java
Table table = new Table()
        .column("Name", Constraint.fill())
        .column("Size", Constraint.fixed(8), Alignment.RIGHT)
        .addRow("a.txt", "1.2 KB");
```

| Method | Description |
|---|---|
| `Table column(String title, Constraint width)` | Add a left-aligned column. |
| `Table column(String title, Constraint width, Alignment)` | Add an aligned column. |
| `Table addRow(String...)` / `setRows(List<String[]>)` | Add or replace rows. |
| `Table showHeader(boolean)` / `headerStyle(Style)` | Header display. |
| `String[] selectedRow()` | Selected row's cells, or null. |

## Form controls

### Button

A focusable, activatable button. Enter or Space runs the action; reversed when focused.

| Method | Description |
|---|---|
| `Button onActivate(Runnable)` | Action. |
| `Button setLabel(String)` | Label. |
| `Button style(Style)` / `focusedStyle(Style)` | Styling. |

### Checkbox

| Method | Description |
|---|---|
| `Checkbox checked(boolean)` / `boolean isChecked()` | State. |
| `Checkbox onChange(Consumer<Boolean>)` | Toggle callback (Enter/Space). |

### RadioGroup

A single-choice option list (built on the list base). Arrows move the cursor; Enter or Space
chooses the option under it.

| Method | Description |
|---|---|
| `int chosenIndex()` / `RadioGroup choose(int)` | The chosen option. |
| `RadioGroup onChange(IntConsumer)` | Choice callback. |

### Form

A focusable field container with internal Tab traversal (implements
[FocusContainer](focus-and-input.md)). Tab/Shift-Tab cycle the fields; a nested `Form` (e.g.
a horizontal button row) is traversed depth-first. Esc fires `onCancel`.

| Method | Description |
|---|---|
| `static Form row()` | A horizontal form (e.g. a button row), traversed left-to-right. |
| `Form addField(Section)` / `addField(Section, int)` / `addField(Section, Constraint)` | Add a focusable field. |
| `Form addStatic(Section, int)` | Add non-focusable decoration. |
| `Form onCancel(Runnable)` | Esc handler. |

`new Form()` stacks fields vertically; `Form.row()` lays them out horizontally.

## Dialogs

`Dialogs` opens modal dialogs over [App.openOverlay](app.md), returning an `OverlayHandle`.
Each centers a bordered `Form`; Tab cycles the buttons and Esc cancels. The composing
widgets are public, so custom dialogs can be built the same way.

```java
Dialogs.confirm(app, "Delete file?", () -> deleteFile());
Dialogs.prompt(app, "Rename to", name -> rename(name));
```

| Method | Description |
|---|---|
| `static OverlayHandle alert(App, String message)` | Message + OK. |
| `static OverlayHandle confirm(App, String message, Runnable onYes)` | Yes/No. |
| `static OverlayHandle prompt(App, String title, Consumer<String> onSubmit)` | Text field + OK/Cancel. |

## Indicators

### ProgressBar

A horizontal bar (0..1) filled with block glyphs, with an optional centered percentage.

| Method | Description |
|---|---|
| `ProgressBar value(double)` / `progress(int, int)` | Set progress. |
| `ProgressBar showPercent(boolean)` | Overlay the percentage. |
| `ProgressBar filledStyle(Style)` / `emptyStyle(Style)` | Styling. |

### Spinner

An indeterminate indicator that cycles frames.

| Method | Description |
|---|---|
| `void tick()` | Advance one frame. |
| `Cancellable start(App)` | Animate on the app timer until cancelled. |
| `Spinner label(String)` / `style(Style)` | Display. |

### Sparkline

A one-row chart of a value series drawn with block glyphs, auto-scaled, newest on the right.

| Method | Description |
|---|---|
| `Sparkline setValues(double[])` / `setValues(List<? extends Number>)` | The series. |
| `Sparkline style(Style)` | Style. |

## TreeView

A scrollable, navigable tree of `TreeNode`s. Arrows move the cursor; Enter or Space toggles a
parent or selects a leaf; Left/Right collapse and expand.

```java
TreeNode root = new TreeNode("project")
        .add(new TreeNode("src").add(new TreeNode("Main.java")))
        .expanded(true);
TreeView tree = new TreeView(root).onSelectNode(node -> open(node));
```

| Method | Description |
|---|---|
| `TreeView onSelectNode(Consumer<TreeNode>)` | Leaf-activation callback. |
| `TreeNode selectedNode()` | Node under the cursor, or null. |
| `TreeView refresh()` | Re-flatten after mutating the tree. |

`TreeNode`: `add(TreeNode)`, `expanded(boolean)`, `label()`, `children()`, `isLeaf()`,
`isExpanded()`.

See [Rendering](rendering.md) for `Style` and `Color`.
